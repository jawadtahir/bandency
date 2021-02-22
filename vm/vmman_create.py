import os
import asyncio
import sys
from paramiko.rsakey import RSAKey
from paramiko.pkey import PKey
from paramiko.pkey import PKey
from frontend.models import db, ChallengeGroup, VirtualMachines

import subprocess
import uuid

OS_IMG_PATH = os.environ.get("OS_IMG_PATH", "focal-server-cloudimg-amd64.img")
TEMPLATE_DIR = os.environ.get("TEMPLATE_DIR", "temp")
PUBLIC_KEY_PATH = os.environ.get("PUBLIC_KEY_PATH", "cochairs.pub")

KEY_SIZE_BITS = 4096
PRIVATE_KEY_FILENAME = "{}_rsa"
PUBLIC_KEY_FILENAME = "{}_rsa.pub"
CLOUD_CONFIG_TEMPLATE_FILE = os.path.join(
    TEMPLATE_DIR, "cloud_init_template.cfg")
NETWORK_CONFIG_TEMPLATE_FILE = os.path.join(
    TEMPLATE_DIR, "network_config_template.cfg")
CREATE_SCRIPT_TEMPLATE_FILE = os.path.join(
    TEMPLATE_DIR, "vm_create_template.sh")
CLOUD_CONFIG_FILE = "cloud_init.cfg"
NETWORK_CONFIG_FILE = "network_config.cfg"
CREATE_SCRIPT_FILE = "vm_create.sh"


def make_dir(team_name: str) -> None:
    
    if os.path.exists(team_name):
        return

    os.mkdir(team_name)


def read_public_key():
    pubkey = None
    with open(PUBLIC_KEY_PATH, "r") as pubkey_file:
        pubkey = pubkey_file.readline()
    return pubkey


def create_key_pair(team_name: str) -> PKey:

    private_key_file_name = os.path.join(
        team_name, PRIVATE_KEY_FILENAME.format(team_name))
    public_key_file_name = os.path.join(
        team_name, PUBLIC_KEY_FILENAME.format(team_name))

    key = RSAKey.generate(KEY_SIZE_BITS)

    key.write_private_key_file(private_key_file_name)

    with open(public_key_file_name, "w") as public_key_file:
        public_key_file.write("{} {}".format(key.get_name(), key.get_base64()))
        os.chmod(public_key_file_name, 0o644)

    return key


def make_config_files(team_name: str, ip_adrs: str, pub_key) -> None:
    cloud_cfg_text = ""
    net_cfg_text = ""

    with open(CLOUD_CONFIG_TEMPLATE_FILE) as cloud_cfg_template_file:
        cloud_cfg_text = "".join(cloud_cfg_template_file.readlines())

    with open(NETWORK_CONFIG_TEMPLATE_FILE) as net_cfg_temp_file:
        net_cfg_text = "".join(net_cfg_temp_file.readlines())

    vm_no = ip_adrs.split(".")[-1]
    cloud_cfg_text = cloud_cfg_text.format(name=team_name, pub_key=pub_key, vm_number=vm_no)

    net_cfg_text = net_cfg_text.format(ip_adrs)

    cld_cfg = os.path.join(team_name, CLOUD_CONFIG_FILE)
    with open(cld_cfg, "w") as file:
        file.write(cloud_cfg_text)

    net_cfg = os.path.join(team_name, NETWORK_CONFIG_FILE)
    with open(net_cfg, "w") as file:
        file.write(net_cfg_text)


def run_cloud_init_cmds(team_name, ip_adrs):
    sh_script_text = ""
    with open(CREATE_SCRIPT_TEMPLATE_FILE) as sh_script_temp_file:
        sh_script_text = "".join(sh_script_temp_file.readlines())

    vm_no = ip_adrs.split(".")[-1]

    sh_script_text = sh_script_text.format(
        os_img_path=os.path.abspath(OS_IMG_PATH), team=team_name, vm_number=vm_no)

    script_path = os.path.join(team_name, CREATE_SCRIPT_FILE)
    with open(script_path, "w") as file:
        file.write(sh_script_text)

    os.chmod(script_path, 0o764)

    sh_proc = subprocess.Popen("./"+CREATE_SCRIPT_FILE, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True, text=True, cwd=team_name)

    while sh_proc.poll() is None:
        print(sh_proc.stdout.readline())



async def insert_in_db(team_name, ip_adrs, forwardingadrs):
    connection = os.environ['DB_CONNECTION']
    await db.set_bind(connection)
    await db.gino.create_all()
    group = await ChallengeGroup.query.where(ChallengeGroup.groupname == team_name).gino.first()
    await VirtualMachines.create(id=uuid.uuid4(),
                            group_id=group.id,
                            internaladrs=ip_adrs,
                            forwardingadrs=forwardingadrs)
    print ("value inserted")





async def createVM(team_name: str, ip_adrs: str, forwardingadrs:str) -> None:
    make_dir(team_name)
    # key = create_key_pair(team_name=team_name)
    pubkey = read_public_key()
    make_config_files(team_name, ip_adrs, pubkey)
    run_cloud_init_cmds(team_name, ip_adrs)
    await insert_in_db(team_name, ip_adrs,forwardingadrs)


if __name__ == "__main__":
    asyncio.run(createVM(sys.argv[1], sys.argv[2], sys.argv[3]))
    #asyncio.run(createVM("group-0", "192.168.122.28", ""))
