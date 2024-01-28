#! /bin/sh

vm_number=$2
team=$1

echo changing directory...
cd /home/ubuntu/bandency/${team}_${vm_number}

echo undefining vm from VM manager
virsh undefine vm_${team}_${vm_number}

echo deleting disk images
sudo rm -rf seed.iso snapshot-focal-server_${team}.qcow2
