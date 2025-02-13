import asyncio
import logging
import signal
import os
import string
import random
import hashlib
import helper
import subprocess


from typing import Any
from hypercorn.asyncio import serve
from hypercorn.config import Config
from quart import Quart, render_template, redirect, url_for, request, flash, make_response
from quart_auth import QuartAuth, login_required, login_user, Unauthorized, AuthUser, logout_user, current_user, Action
from pymongo import AsyncMongoClient, ReturnDocument
from pymongo.errors import DuplicateKeyError
from bson.objectid import ObjectId
from datetime import datetime, timezone
from yaml import safe_load_all, safe_dump_all, YAMLError


from deployment import Deployment

shutdown_event = asyncio.Event()
app = Quart(__name__)
app.secret_key = os.environ.get("WEB_SECRET_KEY", "-9jMkQIvmUeoasdfwksWTtpih2w")
auth_manager = QuartAuth(app=app)


DB_CONNECTION_STRING = os.environ.get("DB_CONNECTION_STRING", "0.0.0.0:52926")
salt = os.environ.get("RANDOM_SALT", "akshdkashdhka")
upload_dir = os.environ.get("DEPLOYMENT_UPLOAD_DIR", os.path.abspath("website/upload"))
deploy_dir = os.environ.get("DEPLOYMENT_DEPLOY_DIR", os.path.abspath("website/deploy"))
quotas_dir = os.environ.get("QUOTAS_DIR", os.path.abspath("website/quotas"))
k8s_template_dir = os.environ.get("K8S_TEMPLATE_DIR", os.path.abspath("website/kubernetes"))

db_client = AsyncMongoClient(DB_CONNECTION_STRING)
db = db_client.challenger

def _signal_handler(*_: Any) -> None:
    shutdown_event.set()


class LoggedInUser(AuthUser):

    def __init__(self, auth_id, action = Action.PASS):

        super().__init__(auth_id, action)
        self.namespace = None
        self.nick = None
        self.api_key = None

    async def load_user_data(self):

        logged_in = await db.groups.find_one({
            "_id": ObjectId(self.auth_id)
        })

        if logged_in:
            self.namespace = logged_in["namespace"]
            self.nick = logged_in["name"]
            self.api_key = logged_in["apikey"]

        


auth_manager.user_class = LoggedInUser
# auth_manager.init_app(app)

@app.before_request
@app.before_websocket
async def load_logged_in_user():
    await current_user.load_user_data()

@app.route("/")
async def index():
    if await current_user.is_authenticated:
        return redirect(url_for('leaderboard'))
    else:
        return await render_template('index.html', name="Welcome!")


@app.route('/logout')
async def logout():
    logout_user()
    return redirect(url_for("index"))


def generate_random_string(length):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))

def hash_password(password):
    db_password = salt + password
    h = hashlib.md5(db_password.encode())
    return h.hexdigest()

@app.route("/register/", methods=['GET', 'POST'])
async def register():
    if request.method == "POST":
        form = await request.form
        groupname = form['group'].strip()
        password = hash_password(form['password'].strip())
        email = form['email'].strip()
        api_key = generate_random_string(32)

        groups = db.groups
        
        existing_email = await groups.find_one({"email": email})
        if existing_email:
            await flash("Email address already in use. Please choose a different one.", "danger")
            return redirect(url_for("register"))
        
        num_groups = await groups.count_documents({})
        namespace = "group-{}".format(num_groups+1)

        try:
            result = await groups.insert_one({
                "name": groupname,
                "password": password,
                "email": email,
                "apikey": api_key,
                "namespace": namespace
            })
        except DuplicateKeyError:
            await flash("Group name already in use. Please choose a different one.", "danger")
            return redirect(url_for("register"))

        if (result.inserted_id != None):
            
            success = create_quotas_in_Kubernetes(namespace)
            
            if not success:
                await flash("Unable to create namespace. Contact admins.", "danger")
            
            return  redirect(url_for("login"))
        else:
            await flash("Unable to creat user. Contact admins.", "danger")
            return redirect(url_for("register"))
    
    elif request.method == 'GET':
        return await render_template("register.html", name="Register")
    
def create_quotas_in_Kubernetes(namespace):

    quotas_yaml_filepath = os.path.join(k8s_template_dir, "quotas.yaml")

    with open(quotas_yaml_filepath, "r") as quotas_yaml_file:

        quotas_yaml = safe_load_all(quotas_yaml_file.read().format(namespace=namespace))
        quotas_filename = "{}.yaml".format(namespace)
        quotas_filepath = os.path.join(quotas_dir, quotas_filename)

        with open(quotas_filepath, "w") as quotas_fd:
            
            safe_dump_all(quotas_yaml, quotas_fd)

    result = subprocess.run("kubectl create -f {}".format(os.path.basename(quotas_filepath)), 
                                capture_output=True, 
                                cwd=os.path.dirname(quotas_filepath), 
                                shell=True)
    
    return result.returncode == 0
    

@app.route('/login', methods=["GET", "POST"])
async def login():

    if request.method == "POST":
        form = await request.form
        group = form["group"].strip()
        password = hash_password(form["password"].strip())
        groups = db.groups

        group = await groups.find_one({
            "name": group,
            "password": password
        })

        if group:
            
            login_user(LoggedInUser(str(group["_id"])))
            return redirect(url_for("leaderboard"))
        else:
            await flash("Invalid credentials.", "danger")
            return await render_template("login.html", name="Login")
            
    elif request.method == "GET":
        return await render_template("login.html", name="Login")
    

@app.route("/profile/", methods=["GET", "POST"])
@login_required
async def profile():
    if request.method == "POST":
        form = await request.form
        email = form["inputEmail"].strip()
        password = form["inputPassword"].strip()
        update = {
            "email": email
        }
        if len(password) > 0:
            update["password"] = hash_password(password)

        group = await db.groups.find_one_and_update(
            {"_id": ObjectId(current_user.auth_id)},
            update, return_document=ReturnDocument.AFTER)
        return await render_template("profile.html", name="Profile", group=group, menu=helper.menu(profile=True))
    elif request.method == "GET":

        group = await db.groups.find_one({
            "_id": ObjectId(current_user.auth_id)
        })
        return await render_template("profile.html", name="Profile", group=group, menu=helper.menu(profile=True))
    else:
        # handle error
        pass

@app.before_serving
async def ensure_directories():
    if not os.path.isdir(upload_dir):
        os.makedirs(upload_dir)
    if not os.path.isdir(deploy_dir):
        os.makedirs(deploy_dir)
    if not os.path.isdir(quotas_dir):
        os.makedirs(quotas_dir)

@app.route("/deployment/", methods=["GET", "POST"])
@login_required
async def deployment():

    if request.method == "POST":
        
        file = await request.files
        file_timestamp = datetime.now(timezone.utc)
        file_name = file["file"].filename.split(".")[0]
        file_extension = file["file"].filename.split(".")[1]
        file_save_path = os.path.join(upload_dir, current_user.namespace, "{}-{}.{}".format(file_name, file_timestamp, file_extension))

        if not os.path.exists(os.path.dirname(file_save_path)):
            os.makedirs(os.path.dirname(file_save_path))

        await file["file"].save(file_save_path)

        form = await request.form
        is_failure = "failure" in form.keys()
        timer_sec = form["timer"].strip()
        failure_type = form["failure_type"].strip()


        
        deployment =  Deployment(current_user.namespace, upload_file_path = file_save_path, is_failure = is_failure, failure_type = failure_type, timer = timer_sec)

        result = await db.deployments.insert_one({
            "_id": ObjectId(deployment._id),
            "group_id": ObjectId(current_user.auth_id),
            "timestamp": str(file_timestamp),
            "is_failure": is_failure,
            "is_active": False})
        
        err_msg = deployment.process_constraints()

        if err_msg == "":
            deployed = deployment.deploy()

            if deployed[0]:
                result = await db.deployments.update_one({"_id": ObjectId(deployment._id)}, {"$set": {
                "is_active": True}})

            else:

                await flash(deployed[1], "danger")
        else:
            await flash(err_msg, "danger")
            

        return redirect(url_for("deployment"))
    elif request.method == "GET":
        deployments = db.deployments.find({
            "group_id": ObjectId(current_user.auth_id)
        }).sort({"timestamp": -1})

        deployment_allowed = True
        deployment_list = []
        async for deployment in deployments:
            deployment_list.append(deployment)
            if deployment["is_active"] == True:
                deployment_allowed = False


        return await render_template("deployment.html", name="Deployment", deployments=deployment_list, deployment_allowed=deployment_allowed,  menu=helper.menu(deployment=True))

@app.route("/deployment/delete/<deployment_id>", methods=["POST"])
@login_required
async def deployment_delete(deployment_id):

    deploy_id = ObjectId(deployment_id)
    namespace = current_user.namespace

    deployment = Deployment(namespace, id = deploy_id)

    deleted = deployment.delete()

    if not deleted[0]:
        await flash(deleted[1], "danger")
    else:
        await db.deployments.update_one({"_id": ObjectId(deployment._id)}, {"$set": {
                "is_active": False}})

    return redirect(url_for("deployment"))


@app.route('/benchmarks/', methods=["GET"])
@login_required
async def benchmarks():
    
    benchmarks = db.benchmarks.find({
            "group_id": ObjectId(current_user.auth_id)
        }).sort({"creation_timestamp": -1})
    
    benchmark_list = []

    async for benchmark in benchmarks:
        
        # For buttons on UI
        benchmark["deactivate"] = True if benchmark["is_active"] else False
        benchmark["details"] = True if benchmark.get("results", None) else False
        
        benchmark_list.append(benchmark)


    return await render_template("benchmarks.html", name="Benchmarks", benchmarks=benchmark_list, menu=helper.menu(benchmarks=True))


@app.route('/benchmarks/details/<benchmarkid>/', methods=["GET"])
@login_required
async def benchmarkdetails(benchmarkid):
    
    benchmark = await db.benchmarks.find_one({"_id": ObjectId(benchmarkid)})
    

    return await render_template("benchmarkdetails.html", benchmark=benchmark, menu=helper.menu(benchmarks=True))


@app.route('/benchmarks/deactivate/<benchmarkid>/', methods=["POST"])
@login_required
async def deactivatebenchmark(benchmarkid):
    
    time_instant = datetime.now(timezone.utc)
    benchmark = await db.benchmarks.find_one({"_id": ObjectId(benchmarkid)})
    benchmark["is_active"] = False
    benchmark["activation_timestamp"] = benchmark.get("activation_timestamp", time_instant)
    benchmark["finished_timestamp"] = benchmark.get("finished_timestamp", time_instant)

    await db.benchmarks.find_one_and_replace({"_id": ObjectId(benchmarkid)}, benchmark, return_document=ReturnDocument.AFTER)

    return redirect("/benchmarks/")



@app.route('/faq/')
@login_required
async def faq():

    return await render_template("faq.html", name="FAQ", menu=helper.menu(faq=True))


@app.route('/docs/')
@login_required
async def docs():
    return await render_template("docs.html", name="Docs", menu=helper.menu(documentation=True))

@app.route('/recentchanges/')
@login_required
async def recentchanges():

    #TODO: Get recent changes
    changes = {}
    return await render_template("recentchanges.html", name="Recent changes", changes=changes, menu=helper.menu(recentchanges=True))

@app.route('/leaderboard/')
@login_required
async def leaderboard():

    #TODO: Get leaderboard results
    latency_pipeline = [
        { "$match": { 
            "type": 'Test' 
            } 
        },
        {"$group": {
            "_id": '$group_id',
            "latency": { "$min": '$results.q0.p90' },
            "throughput": {
                "$max": '$results.q0.throughput'
                }
            }
        },
        {
            "$match": {
                "latency": { "$ne": None }
            }
        },
        {
            "$lookup": {
                "from": 'groups',
                "localField": '_id',
                "foreignField": '_id',
                "as": 'group'
            }
        },
        { "$sort": { "latency": 1, "throughput": -1, "_id": 1 } }
    ]
    throughput_pipeline = [
        { "$match": { 
            "type": 'Test' 
            } 
        },
        {"$group": {
            "_id": '$group_id',
            "latency": { "$min": '$results.q0.p90' },
            "throughput": {
                "$max": '$results.q0.throughput'
                }
            }
        },
        {
            "$match": {
                "throughput": { "$ne": None }
            }
        },
        {
            "$lookup": {
                "from": 'groups',
                "localField": '_id',
                "foreignField": '_id',
                "as": 'group'
            }
        },
        { "$sort": { "throughput": -1, "latency": 1, "_id": 1 } }
    ]


    lat_ranks = await db.benchmarks.aggregate(latency_pipeline)
    tp_ranks = await db.benchmarks.aggregate(throughput_pipeline)
    throughput = []
    latencies = []
    lat_rank = 1
    tp_rank = 1
    async for rank in lat_ranks:
        temp_lat = {}
        temp_lat["rank"] = lat_rank
        temp_lat["name"] = rank["group"][0]["name"]
        temp_lat["latency"] = rank["latency"]
        temp_lat["throughput"] = rank["throughput"]
        latencies.append(temp_lat)
        lat_rank += 1

    async for rank in tp_ranks:
        temp_throughput = {}
        temp_throughput["rank"] = tp_rank
        temp_throughput["name"] = rank["group"][0]["name"]
        temp_throughput["latency"] = rank["latency"]
        temp_throughput["throughput"] = rank["throughput"]
        throughput.append(temp_throughput)
        tp_rank += 1

    return await render_template("leaderboard.html", name="Leaderboard", latencies=latencies, throughput=throughput, menu=helper.menu(leaderboard=True))

@app.route('/feedback/')
@login_required
async def feedback():
    return await render_template("feedback.html", name="Feedback", menu=helper.menu(feedback=True))


@app.route('/rawdata/')
@login_required
async def rawdata():

    return await render_template('rawdata.html', name="Rawdata", menu=helper.menu(rawdata=True))


def run_event_loop():

    cfg = Config()
    cfg.bind = [os.environ.get("WEB_BIND","localhost:8000")]

    loop = asyncio.get_event_loop()
    loop.add_signal_handler(signal.SIGTERM, _signal_handler)
    loop.add_signal_handler(signal.SIGINT, _signal_handler)
    loop.run_until_complete(serve(app, cfg, shutdown_trigger=shutdown_event.wait))




if __name__ == "__main__":
    run_event_loop()

