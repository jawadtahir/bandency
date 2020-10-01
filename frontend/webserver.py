import asyncio
import helper

from quart import Quart, websocket, render_template

app = Quart(__name__)


@app.route('/')
async def index():
    return await render_template('index.html', menu=helper.menu())


@app.route('/login')
async def login():
    return await render_template('login.html')


@app.route('/profile')
async def profile():
    return await render_template('profile.html')


@app.route('/systemstatus')
async def systemstatus():
    return await render_template('systemstatus.html')


@app.route('/leaderboard')
async def leaderboard():
    return await render_template('leaderboard.html')


@app.route('/feedback')
async def feedback():
    return await render_template('feedback.html')


@app.websocket('/ws')
async def notifications():
    while True:
        await asyncio.sleep(1)
        await websocket.send('hello')


if __name__ == "__main__":
    print("Run Debug Version of webserver")
    app.run(port=8000, use_reloader=True, debug=True)
