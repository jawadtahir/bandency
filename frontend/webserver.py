import asyncio
import hashlib
import uuid

import frontend.helper as helper

from quart import Quart, websocket, render_template, redirect, url_for, request
from quart_auth import AuthManager, login_required, Unauthorized, login_user, AuthUser, logout_user

from frontend.models import db, Group

app = Quart(__name__)
app.secret_key = "-9jMkQIvmU2dksWTtpih2w"
AuthManager(app)
salt = 'qakLgEdhryvVyFHfR4vwQw'


@app.route('/')
async def index():
    return await render_template('index.html', menu=helper.menu())


@app.route('/login/', methods=['GET', 'POST'])
async def login():
    if request.method == 'POST':
        form = await request.form
        groupname = form['group'].strip()
        password = form['password'].strip()

        group = await Group.query.where(Group.groupname == groupname and Group.password == password).gino.first()
        if group:
            login_success = True
            login_user(AuthUser(str(group.id)))
            if login_success:
                return redirect(url_for('profile'))
        else:
            return await render_template('login.html', error='Invalid password')
    else:
        return await render_template('login.html')


@app.errorhandler(Unauthorized)
async def redirect_to_login(*_):
    return redirect(url_for("login"))


@app.route('/logout')
async def logout():
    logout_user()
    return "todo"


@app.route('/profile/')
@login_required
async def profile():
    return await render_template('profile.html')


@app.route('/systemstatus')
@login_required
async def systemstatus():
    return await render_template('systemstatus.html')


@app.route('/leaderboard')
@login_required
async def leaderboard():
    return await render_template('leaderboard.html')


@app.route('/feedback')
@login_required
async def feedback():
    return await render_template('feedback.html')


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


def hash_password(password):
    db_password = password + salt
    h = hashlib.md5(db_password.encode())
    return h.hexdigest()


async def admin_create_group(groupname, password):
    hashed_password = hash_password(password)
    return await Group.create(id=uuid.uuid4(), groupname=groupname, password=hashed_password)


@app.before_serving
async def db_connection():
    await db.set_bind('postgresql://bandency:bandeny@localhost:5432/bandency')
    await db.gino.create_all()


def prepare_interactive_get_event_loop():
    asyncio.get_event_loop().run_until_complete(db_connection())
    return asyncio.get_event_loop()


def main():
    print("Run Debug Version of webserver")
    asyncio.get_event_loop().run_until_complete(db_connection())
    app.run(port=8000, use_reloader=True, debug=True)


if __name__ == "__main__":
    main()
