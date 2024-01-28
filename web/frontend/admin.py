import argparse
import asyncio
import hashlib
import logging
from operator import and_
import os
import random
import string
import sys
import uuid

import smtplib
from email.mime.text import MIMEText
from aio_pika import connect, Message
from aio_pika.robust_connection import connect_robust

from frontend.models import db, Group, ChallengeGroup, VirtualMachines

salt = 'qakLgEdljdsljertVyFHfR4vwQw'

async def get_port():
    try:
        with open('counter.txt', 'r+') as file:
            count_all = await db.select([db.func.count()]).select_from(VirtualMachines).gino.scalar()
    except FileNotFoundError:
        count_all = 0
    except ValueError:
        count_all = 0

    with open('counter.txt','w') as file:
        file.write(str(count_all) + '\n')
        #dont use magic numbers
        print("++++++++count"+str(count_all+10000))
    return count_all+10000



def create_msg(send_to, subject, message_text):
    message = MIMEText(message_text)
    message['to'] = send_to
    message['from'] = "debschallange2023@gmail.com"
    message['subject'] = subject

    return message

def send_mail_gmail(send_to, subject, message_plain):
    msg = create_msg(send_to, subject, message_plain)

    server = smtplib.SMTP('smtp.gmail.com', 587)
    server.set_debuglevel(1)
    server.ehlo()
    server.starttls()
    server.login("debsgc22@gmail.com", os.environ['EMAIL_PASSWORD'])
    server.sendmail("debsgc22@gmail.com", send_to, msg.as_string())
    server.quit()


def hash_password(password):
    db_password = salt + password
    h = hashlib.md5(db_password.encode())
    return h.hexdigest()


async def admin_create_group(groupname, password, email, apikey):
    hashed_password = hash_password(password)
    return await ChallengeGroup.create(id=uuid.uuid4(),
                                       groupname=groupname,
                                       password=hashed_password,
                                       groupemail=email,
                                       groupnick="default",
                                       groupapikey=apikey)


def generate_random_string(length):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))


async def create_group(group_name, email, skipmail):
    
    
    default_pw = generate_random_string(8)
    apikey = generate_random_string(32)
    await admin_create_group(group_name, hash_password(default_pw), email, apikey)

    if "true" in skipmail:
        print("New group: {}, email: {}, password: {}".format(group_name, email, default_pw))
    else:
        message = """
        Welcome to the DEBS 2024 - Challenge!
        
        You are now registered. Please continue here:
        https://challenge2024.debs.org/
        
        Group ID: {}
        Password: {}
        
        Problems or Questions? Reply to this e-mail.
        
        We look all forward to your submission!
        
        The DEBS Challenge 2024 Team
        """.format(group_name, default_pw)

        print("New group: {}, email: {}, password: {}".format(group_name, email, default_pw))
        send_mail_gmail(email, "DEBS2022 - Challenge: Group registration", message)
    return

async def send_vm_request(group_name, forwarding_adrs):
    con_str = os.environ["RABBIT_CONNECTION"]
    con = await connect_robust(con_str)

    channel = await con.channel()

    msg = {"groupname":group_name, "forwardingadrs":forwarding_adrs}

    await channel.default_exchange.publish(Message(str(msg).encode("utf8")), routing_key="vm_requests")

    print("sent request for VM for {}".format(group_name))

    await con.close()

async def main(parse_arguments):
    connection = os.environ['DB_CONNECTION']
    logging.debug("db-connection: {}".format(connection))
    await db.set_bind(connection)
    await db.gino.create_all()

    if parse_arguments.command == 'newgroup':
        group_cnt = await db.func.count(ChallengeGroup.id).gino.scalar()
        forwardingadrs=  os.environ["FORWARDING_VM_IP"]+":"+str(await get_port())
        print("counted groups {}".format(group_cnt))
        group_name = "group-{}".format(group_cnt)
        await create_group(group_name, args.email, args.skipmail)
        adr=forwardingadrs.split(":")
        print("forwarding adr : " + forwardingadrs)
        print(adr)
        if args.makevm == "true":
            await send_vm_request(group_name, forwardingadrs)



if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)

    parser = argparse.ArgumentParser(description='Admin Util for DEBS Challenge')
    subparsers = parser.add_subparsers(help='sub-command help', dest="command")

    group_parser = subparsers.add_parser("newgroup", help='Creates a new group with e-mail')
    group_parser.add_argument('--email', type=str, action='store', help='email help', required=True)
    group_parser.add_argument('--skipmail', type=str, action='store', help='true false', required=True)
    group_parser.add_argument('--makevm', type=str, action='store', help='true false', default="true")

    args = parser.parse_args()
    logging.info(args)
    asyncio.run(main(args))
