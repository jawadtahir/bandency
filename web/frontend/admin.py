import argparse
import asyncio
import hashlib
import logging
import os
import random
import string
# Ugly Hack, should not needed when this is fixed: https://github.com/marrow/mailer/issues/87
import sys
import uuid

sys.modules["cgi.parse_qsl"] = None
from marrow.mailer import Mailer, Message

from frontend.models import db, Group, ChallengeGroup

salt = 'qakLgEdhryvVyFHfR4vwQw'


def send_mail(send_to, subject, message_plain):
    mailer = Mailer({'manager.use': 'futures',
                     'transport.use': 'sendmail',
                     'message.author': 'Christoph Doblander <christoph.doblander@in.tum.de>'})
    mailer.start()

    message = Message(author="noreply-debs-challenge@in.tum.de", to=send_to)
    message.subject = subject
    message.plain = message_plain
    message.sendmail_f = False  # Another hack, reason: https://github.com/marrow/mailer/blob/3995ef98a3f7feb75f1aeb652e6afe40a5c94def/marrow/mailer/transport/sendmail.py#L29
    mailer.send(message)

    mailer.stop()


def hash_password(password):
    db_password = salt + password
    h = hashlib.md5(db_password.encode())
    return h.hexdigest()


async def admin_create_group(groupname, password, email):
    hashed_password = hash_password(password)
    return await ChallengeGroup.create(id=uuid.uuid4(),
                                       groupname=groupname,
                                       password=hashed_password,
                                       groupemail=email,
                                       groupnick="default")


def generate_random_string(length):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))


async def create_group(email, skipmail):
    group_cnt = await db.func.count(ChallengeGroup.id).gino.scalar()
    print("counted groups {}".format(group_cnt))
    default_pw = generate_random_string(8)
    group_name = "group-{}".format(group_cnt)
    await admin_create_group(group_name, hash_password(default_pw), email)

    if "true" in skipmail:
        print("New group: {}, email: {}, password: {}".format(group_name, email, default_pw))
    else:
        message = """
        Welcome to the DEBS 2021 - Challenge!
        
        You are now registered. Plz login here:
        https://challenge.msrg.in.tum.de/
        
        Group ID: {}
        Password: {}
        
        If you have any questions or problems, plz. contact: christoph.doblander@in.tum.de
        
        We look all forward to your submission!
        
        The DEBS Challenge 2021 Team
        """.format(group_name, default_pw)

        print("New group: {}, email: {}, password: {}".format(group_name, email, default_pw))
        send_mail(email, "DEBS2021 - Challenge: Group registration", message)
    return


async def main(parse_arguments):
    connection = os.environ['DB_CONNECTION']
    logging.debug("db-connection: {}".format(connection))
    await db.set_bind(connection)
    await db.gino.create_all()

    if parse_arguments.command == 'newgroup':
        await create_group(args.email, args.skipmail)


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)

    parser = argparse.ArgumentParser(description='Admin Util for DEBS Challenge')
    subparsers = parser.add_subparsers(help='sub-command help', dest="command")

    group_parser = subparsers.add_parser("newgroup", help='Creates a new group with e-mail')
    group_parser.add_argument('--email', type=str, action='store', help='email help', required=True)
    group_parser.add_argument('--skipmail', type=str, action='store', help='true flase', required=True)

    args = parser.parse_args()
    logging.info(args)
    asyncio.run(main(args))
