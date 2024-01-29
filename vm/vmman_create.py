#!/usr/bin/python3 python3
#move getting automated forwarding adresses right here
import os
import asyncio
import sys
from shutil import copy
from paramiko.rsakey import RSAKey
from paramiko.pkey import PKey
from paramiko.pkey import PKey
from frontend.models import db, ChallengeGroup, VirtualMachines

import subprocess
import uuid

OS_IMG_PATH = os.environ.get("OS_IMG_PATH", "focal-server-cloudimg-amd64.img")
TEMPLATE_DIR = os.environ.get("TEMPLATE_DIR", "temp")
PUBLIC_KEY_PATH = os.environ.get("PUBLIC_KEY_PATH", "cochairs.pub")
VM_DIR = os.environ.get("VM_DIR", "/home/ubuntu/bandency")

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


def make_dir(team_name: str, ip_adrs:str) -> None:
    
    vm_no = ip_adrs.split(".")[-1]
    dir_name = "{}_{}".format(team_name, vm_no)
    dir_name = os.path.join(VM_DIR, dir_name)
    if os.path.exists(dir_name):
        return dir_name

    os.mkdir(dir_name)
    return dir_name


def read_public_key():
    pubkey = None
    with open(PUBLIC_KEY_PATH, "r") as pubkey_file:
        pubkey = pubkey_file.readline()
    return pubkey


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
        print("++++++++count"+str(count_all+10000))
    return count_all+10000

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


def make_config_files(team_name: str, ip_adrs: str, pub_key, dir_name) -> None:
    cloud_cfg_text = ""
    net_cfg_text = ""
    vm_no = ip_adrs.split(".")[-1]
    # dir_name = "{}_{}".format(team_name, vm_no)

    with open(CLOUD_CONFIG_TEMPLATE_FILE) as cloud_cfg_template_file:
        cloud_cfg_text = "".join(cloud_cfg_template_file.readlines())

    with open(NETWORK_CONFIG_TEMPLATE_FILE) as net_cfg_temp_file:
        net_cfg_text = "".join(net_cfg_temp_file.readlines())

    
    cloud_cfg_text = cloud_cfg_text.format(name=team_name, pub_key=pub_key, vm_number=vm_no)

    net_cfg_text = net_cfg_text.format(ip_adrs)

    cld_cfg = os.path.join(dir_name, CLOUD_CONFIG_FILE)
    with open(cld_cfg, "w") as file:
        file.write(cloud_cfg_text)

    net_cfg = os.path.join(dir_name, NETWORK_CONFIG_FILE)
    with open(net_cfg, "w") as file:
        file.write(net_cfg_text)


def run_cloud_init_cmds(team_name, ip_adrs, dir_name, forwardingadrs):
    # last block of IP adrs is the VM number (in case of multiple VMs by one group)
    vm_no = ip_adrs.split(".")[-1]

    # dir_name = "{}_{}".format(team_name, vm_no)

    # Copy the base image to the directory
    new_os_img_path = copy(OS_IMG_PATH, dir_name)


    sh_script_text = ""
    with open(CREATE_SCRIPT_TEMPLATE_FILE) as sh_script_temp_file:
        sh_script_text = "".join(sh_script_temp_file.readlines())

    

    sh_script_text = sh_script_text.format(

        os_img_path=os.path.abspath(new_os_img_path), team=team_name, vm_number=vm_no, ip=ip_adrs, forwardingport=forwardingadrs.split(":")[1])

    script_path = os.path.join(dir_name, CREATE_SCRIPT_FILE)
    with open(script_path, "w") as file:
        file.write(sh_script_text)

    os.chmod(script_path, 0o764)

    sh_proc = subprocess.Popen("./"+CREATE_SCRIPT_FILE, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True, text=True, cwd=dir_name)

    while sh_proc.poll() is None:
        print(sh_proc.stdout.readline())



async def insert_in_db(team_name, ip_adrs, forwardingadrs):
   # connection = os.environ['DB_CONNECTION']
    #await db.set_bind(connection)
    #await db.gino.create_all()
    group = await ChallengeGroup.query.where(ChallengeGroup.groupname == team_name).gino.first()
    
    await VirtualMachines.create(id=uuid.uuid4(),
                                     group_id=group.id,
                                     internaladrs=ip_adrs,
                                     forwardingadrs=forwardingadrs)
    #logging.info("Value inserted")
   


async def createVM(team_name: str) -> None:
    #Generates available ips
    connection = os.environ['DB_CONNECTION']
    await db.set_bind(connection)
    await db.gino.create_all()
    group_cnt = await db.func.count(ChallengeGroup.id).gino.scalar()
    forwardingadrs=  os.environ["FORWARDING_VM_IP"]+":"+str(await get_port())
    print("forwarding adr : " + forwardingadrs)
    vm_count = int(forwardingadrs.split(":")[1]) - 10000
    ip_prefix = os.environ.get("IP_PREFIX", "192.168.1")
    vm_ip = "{}.{}".format(ip_prefix, vm_count+1)
    print("vm_ip = "+vm_ip)
    dir_name = make_dir(team_name, vm_ip)
     #key = create_key_pair(team_name=team_name)
    pubkey = read_public_key()
    make_config_files(team_name, vm_ip, pubkey, dir_name)
    run_cloud_init_cmds(team_name, vm_ip, dir_name, forwardingadrs)
    await insert_in_db(team_name, vm_ip,forwardingadrs)


if __name__ == "__main__":
    asyncio.run(createVM(sys.argv[1]))
    #asyncio.run(createVM("group-0"))
