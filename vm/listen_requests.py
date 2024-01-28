import argparse
import nest_asyncio
from gino import Gino

nest_asyncio.apply()
import logging
import asyncio
import os
from aio_pika import connect_robust, Message
from aio_pika.message import IncomingMessage
from sqlalchemy import and_
from vmman_create import createVM
from frontend.models import db, ChallengeGroup, VirtualMachines
from vmenvsetup import setup_directories


async def make_vm(msg: IncomingMessage):
    msg = eval(msg.body.decode("utf8"))
    forwardingadrs = msg["forwardingadrs"]
    group_name = msg["groupname"]
    group_number = group_name.split("-")[1]
    ip_prefix = os.environ.get("IP_PREFIX", "192.168.1")
    group = await ChallengeGroup.query.where(ChallengeGroup.groupname == group_name).gino.first()
    print(group)
    vm_count = int(forwardingadrs.split(":")[1]) - 10000
    vm_ip = "{}.{}".format(ip_prefix, vm_count + 1)
    print("forwardingadress1 : "+forwardingadrs)
    await createVM(group_name, vm_ip, forwardingadrs)


async def bind_db_connection(connection_str):
    await db.set_bind(connection_str)


async def listen_vm_reqs(loop, rabbit_str):
    con = await connect_robust(rabbit_str)
    channel = await con.channel()
    q = await channel.declare_queue("vm_requests")
    await q.consume(make_vm, no_ack=False)

async def main(parse_arguments):
    db_connection_str = os.environ['DB_CONNECTION']
    rabbit_connection_str = os.environ["RABBIT_CONNECTION"]

    loop = asyncio.get_event_loop()
    loop.create_task(bind_db_connection(db_connection_str))
    if parse_arguments.command == 'process':
        await loop.create_task(listen_vm_reqs(loop, rabbit_connection_str))
        loop.run_forever()
    if parse_arguments.command == "vmenvsetup":
        setup_directories()


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)

    parser = argparse.ArgumentParser(description='Admin Util for Virtual Machine Manager - DEBS Challenge')
    subparsers = parser.add_subparsers(help='sub-command help', dest="command")
    group_parser = subparsers.add_parser("newgroup", help='Creates a new group with e-mail')
    group_parser = subparsers.add_parser("process", help='Connect to Rabbitmq and start updating VMs')

    args = parser.parse_args()
    logging.info(args)
    if args.command is None:
        parser.print_help()
    else:
        asyncio.run(main(args))
