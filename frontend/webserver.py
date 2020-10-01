import asyncio
import frontend.helper as helper

from quart import Quart, websocket, render_template, redirect, url_for, flash, request
from quart_auth import AuthManager, login_required, Unauthorized

from frontend.model import model

app = Quart(__name__)
app.secret_key = "-9jMkQIvmU2dksWTtpih2w"
AuthManager(app)


@app.route('/')
async def index():
    return await render_template('index.html', menu=helper.menu())


@app.route('/login/', methods=['GET', 'POST'])
async def login():
    error = None

    if request.method == 'POST':
        form = await request.form
        groupid = form['group']
        password = form['password']

        error = 'Invalid password'
        login_success = True
        if login_success:
            return redirect(url_for('profile'))

    return await render_template('login.html', error=error)

@app.errorhandler(Unauthorized)
async def redirect_to_login(*_):
    return redirect(url_for("login"))


@app.route('/logout')
async def logout():
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


if __name__ == "__main__":
    print("Run Debug Version of webserver")
    await model.model.db.set_bind('postrgresql://localhost/bandeny')
    app.run(port=8000, use_reloader=True, debug=True)
