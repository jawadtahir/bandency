import logging
import asyncio

#Main helper: https://fabianlee.org/2020/02/23/kvm-testing-cloud-init-locally-using-kvm-for-an-ubuntu-cloud-image/

# More script: https://github.com/fabianlee/local-kvm-cloudimage/blob/master/ubuntu-bionic/local-km-cloudimage.sh


class VirshWrapper:
    virsh_cmd = "/usr/bin/virsh"

    def __init__(self, uri):
        self.uri = uri

    async def _callVirsh(self, args):
        proc = await asyncio.create_subprocess_shell(
            cmd=self.virsh_cmd,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE)

        stdout, stderr = await proc.communicate()

        return proc.returncode, stdout, stderr

class QemuImgWrapper:
    cmd = "/usr/bin/qemu-img"

    async def resize_image(self, image_path, targetpath):



def list_running_vms():
    print("hello")

def start_vm():
    print("start vm")

if __name__ == "__main__":
    logging.basicConfig()

    loop = asyncio.get_event_loop()
    wrapper = VirshWrapper()
    loop.run_until_complete(wrapper.)

    logging.getLogger().setLevel(logging.DEBUG)
    #prepare_data_directory()
    # fetch the url of the last directory
    #download_every_month()