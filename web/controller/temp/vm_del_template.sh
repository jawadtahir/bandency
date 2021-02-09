#! /bin/sh

echo changing directory...
cd {team}

echo undefining vm from VM manager
virsh undefine vm_{team}

echo deleting disk images
sudo rm -rf seed.iso snapshot-focal-server_{team}.qcow2