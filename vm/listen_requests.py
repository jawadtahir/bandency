

import asyncio
import os
from aio_pika import connect_robust, Message
from aio_pika.message import IncomingMessage
from sqlalchemy import and_
from vmman_create import createVM
from frontend.models import db, ChallengeGroup, VirtualMachines

async def make_vm(mesg: IncomingMessage):
    msg = eval(mesg.body.decode("utf8"))
    forwardingadrs = msg["forwardingadrs"]
    group_name = msg["groupname"]
    group_number = group_name.split("-")[1]
    ip_prefix = os.environ.get("IP_PREFIX", "10.21")
    group = await ChallengeGroup.query.where(ChallengeGroup.groupname == group_name).gino.first()
    db.func.count(ChallengeGroup.id).gino.scalar()

    
    vm_count = await (db.select([db.func.count()]).where(and_(VirtualMachines.group_id == group.id)).gino.scalar())
    vm_ip = "{}.{}.{}".format(ip_prefix, group_number, vm_count+1)
    await createVM(group_name, vm_ip, forwardingadrs)


async def listen_vm_reqs(loop):
    connection = os.environ['DB_CONNECTION']
    await db.set_bind(connection)
    await db.gino.create_all()

    con_str = os.environ["RABBIT_CONNECTION"]
    con = await connect_robust(con_str)
    channel = await con.channel()
    q = await channel.declare_queue("vm_requests")
    await q.consume(make_vm,no_ack=True)




if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.create_task(listen_vm_reqs(loop))
    loop.run_forever()