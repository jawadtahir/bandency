import asyncio
import logging
import paramiko
import os
import traceback
from signal import SIGTERM, SIGINT
from typing import Any, Callable, Awaitable

from hypercorn.asyncio import serve
from hypercorn.config import Config
from quart import Quart, websocket, render_template, redirect, url_for, request, flash
from quart_auth import AuthManager, login_required, Unauthorized, login_user, AuthUser, logout_user, current_user

from sshpubkeys import SSHKey, InvalidKeyError
import frontend.helper as helper
import frontend.worker as worker
from frontend.admin import hash_password
from frontend.models import db, ChallengeGroup, get_group_information, get_recent_changes, \
    get_benchmarks_by_group, get_benchmark, get_benchmarkresults, VirtualMachines, get_vms_of_group
from shared.util import raise_shutdown, Shutdown

app = Quart(__name__)
app.secret_key = "-9jMkQIvmU2dksWTtpih2w"
AuthManager(app)

shutdown_event = asyncio.Event()

PRIVATE_KEY_PATH = os.environ.get("PRIVATE_KEY_PATH", "cochairs")


def signal_handler(*_: Any) -> None:
    shutdown_event.set()


logging.basicConfig(level=logging.DEBUG)


@app.route('/')
async def index():
    if await current_user.is_authenticated:
        return redirect(url_for('profile'))
    else:
        return await render_template('index.html', name="Welcome!")


@app.route('/login/', methods=['GET', 'POST'])
async def login():
    if request.method == 'POST':
        form = await request.form
        groupname = form['group'].strip()
        password = hash_password(form['password'].strip())

        group = await ChallengeGroup.query.where(
            ChallengeGroup.groupname == groupname and ChallengeGroup.password == password).gino.first()
        if group:
            login_success = True
            login_user(AuthUser(str(group.id)))
            if login_success:
                return redirect(url_for('profile'))
        else:
            return await render_template('login.html', name="Login", error='Invalid password')
    else:
        return await render_template('login.html', name="Login")


@app.route('/logout')
async def logout():
    logout_user()
    return redirect(url_for("index"))


@app.errorhandler(Unauthorized)
async def redirect_to_login(*_):
    return redirect(url_for("login"))


async def upload_pub_key(pubkey: str, vm_adrs: str, username, groupid, port: int = 22):

    ssh = SSHKey(pubkey, strict=True)
    try:
        ssh.parse()
    except InvalidKeyError:
        await flash('Invalid key')
        print("Invalid key")
        traceback.print_exc()
        return
    except NotImplementedError:
        await flash('Invalid key type')
        print("Invalid key type")
        traceback.print_exc()
        return

    pkey = paramiko.RSAKey.from_private_key_file(PRIVATE_KEY_PATH)
    with paramiko.SSHClient() as client:
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        try:
            client.connect(vm_adrs, port, username, pkey=pkey, timeout=3.0)  # three seconds timeout
        except:
            print("Could not connect")
            return await flash("Could not connect to VM to set the key")

        with client.open_sftp() as sftpclient:
            try:
                filepath = '.ssh/authorized_keys'
                print("accessing authorized_keys")
                with sftpclient.open(filepath) as authorized_keys:
                    print("opened file")
                    for authorized in authorized_keys:
                        if pubkey in authorized:
                            print("key already added")
                            return await flash("Key already added")
            except IOError:
                traceback.print_exc()
                print('authorized does not exist, continue') #file does not exist, also ok

        try:
            client.exec_command('mkdir -p ~/.ssh/', timeout=3.0)
            client.exec_command('echo "%s" >> ~/.ssh/authorized_keys' % pubkey, timeout=3.0)
            client.exec_command('chmod 644 ~/.ssh/authorized_keys', timeout=3.0)
            client.exec_command('chmod 700 ~/.ssh/', timeout=3.0)
            await flash("Key successful added")
        except:
            print("Error while setting ssh key")
            traceback.print_exc()
            return await flash("Error while setting ssh key")

    vms = await VirtualMachines.query.where(VirtualMachines.group_id == groupid).gino.all()
    for vm in vms:
        if vm.internaladrs == vm_adrs or (vm_adrs in vm.forwardingadrs):
            await vm.update(sshpubkey=pubkey).apply()


@app.route('/profile/', methods=['GET', 'POST'])
@login_required
async def profile():
    if request.method == 'POST':
        group = await get_group_information(current_user.auth_id)
        form = await request.form

        if 'profile' in form:
            print("profile")
            groupnick = form['groupnick'].strip()
            groupemail = form['groupemail'].strip()

            if len(groupnick) > 32:
                await flash('Nickname should be below 32 chars')
                return redirect(url_for('profile'))

            await group.update(groupnick=groupnick, groupemail=groupemail).apply()
            await flash('Profile saved')

            return redirect(url_for('profile'))
        elif 'sshkey' in form:
            print("sshkey")
            err = False
            if 'VMAdrs' not in form:
                await flash('No VM selected')
                err = True
            if 'sshpubkey' not in form or len(form['sshpubkey'].strip()) <= 30:
                await flash('No sshpukey or invalid sshpubkey added')
                err = True

            if err:
                return redirect(url_for('profile'))

            vmadrs = form['VMAdrs'].strip()
            sshkey = form['sshpubkey'].strip()
            groupname = group.groupname
            vmadrs = vmadrs.split("/")[1]
            try:
                await upload_pub_key(sshkey, vmadrs, groupname, group.id, 22)
            except Exception as e:
                print(e)
                print(traceback.format_exc())
                await flash(
                    "Error connecting to VM. Please inform the challenge organizers, debschallenge2021@gmail.com")

            return redirect(url_for('profile'))
    else:
        group = await get_group_information(current_user.auth_id)
        vms = await get_vms_of_group(group.id)
        return await render_template('profile.html', name="Profile", group=group, vms=vms,
                                     menu=helper.menu(profile=True))


@app.route('/faq/')
@login_required
async def faq():
    group = await get_group_information(current_user.auth_id)
    return await render_template('faq.html', name="FAQ", group=group,
                                 menu=helper.menu(faq=True))


@app.route('/documentation/')
@login_required
async def documentation():
    group = await get_group_information(current_user.auth_id)
    return await render_template('documentation.html', name="Documentation", group=group,
                                 menu=helper.menu(documentation=True))


@app.route('/benchmarks/')
@login_required
async def benchmarks():
    group = await get_group_information(current_user.auth_id)
    benchmarks = await get_benchmarks_by_group(group.id)
    return await render_template('benchmarks.html',
                                 name="Benchmarks",
                                 group=group,
                                 benchmarks=benchmarks,
                                 menu=helper.menu(benchmarks=True))


@app.route('/benchmarkdetails/<int:benchmarkid>/')
@login_required
async def benchmarkdetails(benchmarkid):
    benchmark = await get_benchmark(benchmarkid)
    benchmarkresults = await get_benchmarkresults(benchmarkid)
    if benchmark:
        group = await get_group_information(current_user.auth_id)
        if group.id == benchmark.group_id:
            return await render_template('benchmarkdetails.html',
                                         name="Benchmark",
                                         group=group,
                                         benchmark=benchmark,
                                         benchmarkresults=benchmarkresults,
                                         menu=helper.menu(benchmarks=True))

    return redirect_to_login()


@app.route('/rawdata/')
@login_required
async def rawdata():
    group = await get_group_information(current_user.auth_id)
    d = os.environ["DATASET_DIR"]
    files = os.listdir(d)
    filesandsize = map(lambda f: [f, (os.path.getsize(os.path.join(d, f)) / (1024 * 1024))], files)

    return await render_template('rawdata.html', name="Rawdata", group=group, files=filesandsize,
                                 menu=helper.menu(rawdata=True))


@app.route('/recentchanges/')
async def recentchanges():
    changes = await get_recent_changes()
    return await render_template('recentchanges.html', name="Recent changes", changes=changes,
                                 menu=helper.menu(recentchanges=True))


@app.route('/systemstatus')
@login_required
async def systemstatus():
    return await render_template('systemstatus.html', menu=helper.menu(system_status=True), name="System status")


@app.route('/leaderboard')
@login_required
async def leaderboard():
    return await render_template('leaderboard.html', menu=helper.menu(leaderboard=True), name="Leaderboard")


@app.route('/testruns')
@login_required
async def testruns():
    return await render_template('testruns.html', menu=helper.menu(testruns=True), name="Test runs")


@app.route('/feedback')
@login_required
async def feedback():
    return await render_template('feedback.html', menu=helper.menu(feedback=True), name="Feedback")


@app.websocket('/ws')
@login_required
async def notifications():
    try:
        while True:
            await asyncio.sleep(1)
            await websocket.send('hello')
    except asyncio.CancelledError:
        # Handle disconnect here
        raise


@app.before_serving
async def db_connection():
    print("start db_connection")
    connection = os.environ['DB_CONNECTION']
    logging.debug("db-connection: {}".format(connection))
    await db.set_bind(connection)
    await db.gino.create_all()


def prepare_interactive_get_event_loop():
    asyncio.get_event_loop().run_until_complete(db_connection())
    return asyncio.get_event_loop()


async def mainloop(debug, loop):
    print("Run Debug Version of webserver")
    tasks = []
    # monitor_task = worker.process_server_monitor_metrics(loop, shutdown_event, os.environ['RABBIT_CONNECTION'])

    bind_adrs = os.environ.get("WEB_BIND", "localhost:8000")

    cfg = Config()
    cfg.bind = [bind_adrs]

    if debug:

        cfg.debug = True
        print("starting with debugging enabled")
        cfg.use_reloader = True
        webserver_task = serve(app, cfg, shutdown_trigger=shutdown_event.wait)
    else:

        webserver_task = serve(app, cfg, shutdown_trigger=shutdown_event.wait)

    tasks.append(webserver_task)
    # tasks.append(monitor_task)

    # create the database connect, without starting doesn't make sense
    await db_connection()

    # await asyncio.gather(monitor_task, webserver_task, wakeup(shutdown_event))
    try:
        gathered_tasks = asyncio.gather(*tasks)
        await gathered_tasks
    except (Shutdown, KeyboardInterrupt):
        pass


def main():
    loop = asyncio.get_event_loop()

    loop.add_signal_handler(SIGTERM, signal_handler)
    loop.add_signal_handler(SIGINT, signal_handler)

    # loop.create_task(raise_shutdown(shutdown_event.wait, loop, "general"))

    loop.run_until_complete(mainloop(True, loop))


if __name__ == "__main__":
    main()
