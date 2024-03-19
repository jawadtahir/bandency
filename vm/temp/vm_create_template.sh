#! /bin/sh

echo printing current directory...
pwd

echo creating disk image...
qemu-img create -f qcow2 -b {os_img_path} -F qcow2 snapshot-focal-server_{team}_{vm_number}.qcow2 10G

echo opening ssh tunnel..

chmod u+w /home/ubuntu/bandency/{team}_{vm_number}/

echo assigning port for external communication with the corresponding virtual machine.
sudo iptables -t nat -A PREROUTING -p tcp --dport {forwardingport} -j DNAT --to-destination {ip}:22

echo setting up a nat rule to forward packets comming from virtual machine
sudo iptables -t nat -A POSTROUTING -o eth0 -s {ip} -j MASQUERADE


echo creating seed image...
cloud-localds --network-config=./network_config.cfg seed.iso cloud_init.cfg

echo installing VM...
sudo virt-install --check all=off --name vm_{team}_{vm_number} --vcpus 4 --memory 8192 --disk snapshot-focal-server_{team}_{vm_number}.qcow2,device=disk,bus=virtio,size=2048 --disk seed.iso,device=cdrom --os-type linux --os-variant ubuntu20.04  --graphics none --network bridge=br0,model=virtio --import --console pty,target_type=serial
