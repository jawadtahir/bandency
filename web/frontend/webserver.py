import asyncio
import logging
import os
from signal import SIGTERM, SIGINT
from typing import Any, Callable, Awaitable

from hypercorn.asyncio import serve
from hypercorn.config import Config
from quart import Quart, websocket, render_template, redirect, url_for, request
from quart_auth import AuthManager, login_required, Unauthorized, login_user, AuthUser, logout_user, current_user

import frontend.helper as helper
import frontend.worker as worker
from frontend.admin import hash_password
from frontend.models import db, ChallengeGroup, get_group_information, get_recent_changes
from shared.util import raise_shutdown, Shutdown

app = Quart(__name__)
app.secret_key = "-9jMkQIvmU2dksWTtpih2w"
AuthManager(app)

shutdown_event = asyncio.Event()


def signal_handler(*_: Any) -> None:
    shutdown_event.set()


logging.basicConfig(level=logging.DEBUG)


@app.route('/')
async def index():
    if current_user.is_authenticated:
        return redirect(url_for('profile'))
    else :
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


@app.route('/profile/')
@login_required
async def profile():
    group = await get_group_information(current_user.auth_id)
    return await render_template('profile.html', name="Profile", group=group, menu=helper.menu(profile=True))


@app.route('/documentation/')
@login_required
async def documentation():
    group = await get_group_information(current_user.auth_id)
    return await render_template('documentation.html', name="Documentation", group=group, menu=helper.menu(documentation=True))


@app.route('/rawdata/')
@login_required
async def rawdata():
    group = await get_group_information(current_user.auth_id)
    d = os.environ["DATASET_DIR"]
    files = os.listdir(d)
    filesandsize = map(lambda f: [f, (os.path.getsize(os.path.join(d, f))/(1024*1024))], files)

    return await render_template('rawdata.html', name="Rawdata", group=group, files=filesandsize, menu=helper.menu(rawdata=True))


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


@app.route('/feedback')
@login_required
async def feedback():
    return await render_template('feedback.html', menu=helper.menu(feedback=True), name="Feedback")


@app.route('/scheduledbenchmarks')
@login_required
async def scheduledbenchmarks():
    return await render_template('scheduledbenchmarks.html', menu=helper.menu(scheduled_benchmarks=True),
                                 name="Scheduled benchmarks")


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
    monitor_task = worker.process_server_monitor_metrics(loop, shutdown_event, os.environ['RABBIT_CONNECTION'])
    tasks.append(monitor_task)

    if debug:
        cfg = Config()
        cfg.debug = True
        cfg.use_reloader = True
        webserver_task = serve(app, cfg, shutdown_trigger=shutdown_event.wait)
    else:
        cfg = Config()
        webserver_task = serve(app, cfg, shutdown_trigger=shutdown_event.wait)

    tasks.append(webserver_task)

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
