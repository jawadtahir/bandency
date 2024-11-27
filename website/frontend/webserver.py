import asyncio
import logging
import signal
import os
import string
import random
import hashlib
import helper

from typing import Any
from hypercorn.asyncio import serve
from hypercorn.config import Config
from quart import Quart, render_template, redirect, url_for, request, flash, make_response
from quart_auth import QuartAuth, login_required, login_user, Unauthorized, AuthUser, logout_user, current_user
from pymongo import AsyncMongoClient, ReturnDocument
from pymongo.errors import DuplicateKeyError
from bson.objectid import ObjectId


from deployment import Deployment

shutdown_event = asyncio.Event()
app = Quart(__name__)
app.secret_key = os.environ.get("WEB_SECRET_KEY", "-9jMkQIvmUeoasdfwksWTtpih2w")
QuartAuth(app=app)

DB_CONNECTION_STRING = os.environ.get("DB_CONNECTION_STRING", "localhost:52925")
salt = os.environ.get("RANDOM_SALT", "akshdkashdhka")
upload_dir = os.environ.get("DEPLOYMENT_UPLOAD_DIR", os.path.abspath("upload"))
deploy_dir = os.environ.get("DEPLOYMENT_DEPLOY_DIR", os.path.abspath("deploy"))

db_client = AsyncMongoClient(DB_CONNECTION_STRING)
db = db_client.challenger

def _signal_handler(*_: Any) -> None:
    shutdown_event.set()

@app.route("/")
async def index():
    if await current_user.is_authenticated:
        return redirect(url_for('profile'))
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
        try:
            result = await groups.insert_one({
                "groupnick": groupname,
                "password": password,
                "groupemail": email,
                "groupapikey": api_key
            })
        except DuplicateKeyError:
            return await flash("Duplicate error")

        if (result.inserted_id != None):
            return  redirect(url_for("login"))
        else:
            flash("Unable to creat user")
    elif request.method == 'GET':
        return await render_template("register.html", name="Register")
    

@app.route('/login', methods=["GET", "POST"])
async def login():

    if request.method == "POST":
        form = await request.form
        group = form["group"].strip()
        password = hash_password(form["password"].strip())
        groups = db.groups

        group = await groups.find_one({
            "groupnick": group,
            "password": password
        })

        if group:
            
            login_user(AuthUser(str(group["_id"])))
            
            return redirect(url_for("profile"))
        else:
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
            "groupemail": email
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

@app.route("/deployment/", methods=["GET", "POST"])
@login_required
async def deployment():

    if request.method == "POST":
        
        file = await request.files
        deploy_file = file["file"]
        deployment = Deployment(deploy_file)
        deployment.process_constraints()
        deployment.deploy()

        return redirect(url_for("deployment"))
    elif request.method == "GET":
        deployment_coll = db.deployments
        deployments =  deployment_coll.find({
            "_id": ObjectId(current_user.auth_id)
        })

        return await render_template("deployment.html", name="Deployment", deployments=deployments, menu=helper.menu(deployment=True))

    pass

def run_event_loop():

    cfg = Config()
    cfg.bind = [os.environ.get("WEB_BIND","localhost:8000")]

    loop = asyncio.get_event_loop()
    loop.add_signal_handler(signal.SIGTERM, _signal_handler)
    loop.add_signal_handler(signal.SIGINT, _signal_handler)
    loop.run_until_complete(serve(app, cfg, shutdown_trigger=shutdown_event.wait))




if __name__ == "__main__":
    run_event_loop()

