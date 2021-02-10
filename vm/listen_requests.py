

import asyncio
import os
from aio_pika import connect_robust, Message
from aio_pika.message import IncomingMessage
from vmman_create import createVM

def make_vm(mesg: IncomingMessage):
    group_name = mesg.body.decode("utf8")
    group_number = group_name.split("-")[1]
    ip_prefix = os.environ.get("IP_PREFIX", "10.21")
    vm_ip = "{}.{}.{}".format(ip_prefix, group_number, 1)
    createVM(group_name, vm_ip)


async def listen_vm_reqs(loop):
    con_str = os.environ["RABBIT_CONNECTION"]
    con = await connect_robust(con_str)
    channel = await con.channel()
    q = await channel.declare_queue("vm_requests")
    await q.consume(make_vm,no_ack=True)




if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.create_task(listen_vm_reqs(loop))
    loop.run_forever()