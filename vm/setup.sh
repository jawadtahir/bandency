#!/usr/bin/env bash

sudo apt install python3-pip
sudo apt-get install libvirt-dev
sudo apt install libpq-dev python3-dev
sudo pip install -r requirements.txt 

source .envrc
sudo apt install qemu qemu-kvm libvirt-clients libvirt-daemon-system virtinst bridge-utils
sudo systemctl enable libvirtd
sudo service libvirtd start
sudo apt-get install cloud-utils
source /tmp/
