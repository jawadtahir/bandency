import uuid
from gino import Gino
from quart_auth import AuthUser

db = Gino()

class Group(AuthUser):
    def __init__(self, auth_id):
        super().__init__(auth_id)
        self._resolved = False
        self._email = None

    async def _resolve(self):
        if not self._resolved:
            self._email = await db.fetch_email(self.auth_id)
            self._resolved = True

    @property
    async def email(self):
        await self._resolve()
        return self._email


class Group(db.Model):
    __tablename__ = 'groups'

    id = db.Column(db.(), primary_key=True)
    nickname = db.Column(db.Unicode(), default='noname')