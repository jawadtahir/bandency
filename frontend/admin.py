import hashlib
import uuid
import argparse
import logging

# Ugly Hack, should not needed when this is fixed: https://github.com/marrow/mailer/issues/87
import sys

sys.modules["cgi.parse_qsl"] = None
from marrow.mailer import Mailer, Message

from frontend.models import db, Group, get_group_information, get_recent_changes

salt = 'qakLgEdhryvVyFHfR4vwQw'

mailer = Mailer({'manager.use': 'futures',
                 'transport.use': 'sendmail',
                 'message.author': 'Christoph Doblander <christoph.doblander@in.tum.de>',
                 'message.subject': "Test subject."})


def send_mail(subject, message_plain):
    mailer = Mailer({'manager.use': 'futures',
                     'transport.use': 'sendmail',
                     'message.author': 'Christoph Doblander <christoph.doblander@in.tum.de>'})
    mailer.start()

    message = Message(author="noreply-debs-challenge@in.tum.de", to="doblande@in.tum.de")
    message.subject = subject
    message.plain = message_plain
    mailer.send(message)

    mailer.stop()


def hash_password(password):
    db_password = salt + password
    h = hashlib.md5(db_password.encode())
    return h.hexdigest()


async def admin_create_group(groupname, password):
    hashed_password = hash_password(password)
    return await Group.create(id=uuid.uuid4(), groupname=groupname, password=hashed_password)


def __main__():
    logging.basicConfig(level=logging.INFO)

    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('creategroup',
                        help='sum the integers (default: find the max)',
                        required=False)

    parser.add_argument('--email',
                        help='sets ',
                        required=False)

    args = parser.parse_args()
    print(args.accumulate(args.integers))
