from gino import Gino
from quart_auth import AuthUser
from sqlalchemy import TIMESTAMP, INTEGER, BigInteger
from sqlalchemy.dialects.postgresql import UUID

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

class VirtualMachines(db.Model):
    __tablename__ = "virtualmachines"

    id = db.Column(UUID, primary_key=True)
    group_id = db.Column(UUID, db.ForeignKey("groups.id")) 
    internaladrs = db.Column(db.Unicode())
    forwardingadrs = db.Column(db.Unicode())
    sshpubkey = db.Column(db.Unicode())

class RecentChanges(db.Model):
    __tablename__ = 'recentchanges'

    id = db.Column(UUID, primary_key=True)
    timestamp = db.Column(TIMESTAMP)
    level = db.Column(INTEGER)
    description = db.Column(db.Unicode())


async def get_recent_changes():
    return await RecentChanges.query.order_by(RecentChanges.timestamp.desc()).gino.all()


class ChallengeGroup(db.Model):
    __tablename__ = 'groups'
    id = db.Column(UUID, primary_key=True)

    groupname = db.Column(db.Unicode())
    password = db.Column(db.Unicode())
    groupemail = db.Column(db.Unicode())
    groupnick = db.Column(db.Unicode())
    groupapikey = db.Column(db.String(255))

async def get_group_information(group_id):
    return await ChallengeGroup.get(group_id)


class ServerMonitorMetrics(db.Model):
    __tablename__ = 'servermonitormetrics'
    id = db.Column(UUID, primary_key=True)

    server_name=db.Column(db.Unicode())
    timestamp=db.Column(TIMESTAMP)
    cpu_percent=db.Column(db.Float)
    load1m=db.Column(db.Float)
    load5m=db.Column(db.Float)
    load15m=db.Column(db.Float)
    mem_total=db.Column(BigInteger)
    mem_available=db.Column(BigInteger)
    mem_used=db.Column(BigInteger)
    mem_free=db.Column(BigInteger)
    duration_millis=db.Column(BigInteger)
    read_count=db.Column(BigInteger)
    write_count=db.Column(BigInteger)
    read_bytes=db.Column(BigInteger)
    write_bytes=db.Column(BigInteger)


