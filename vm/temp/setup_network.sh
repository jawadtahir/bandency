#!/bin/bash

# Define interface names
INTERFACE_VETH0="veth0"
INTERFACE_VETH1="veth1"
INTERFACE_BRIDGE="br0"

# Set IP addresses
IP_VETH0="192.168.1.253/24"
IP_VETH1="192.168.1.252/24"
IP_BRIDGE="192.168.1.254/24"

# Create veth pairs
sudo ip link add $INTERFACE_VETH1 type veth peer name $INTERFACE_VETH0

# Create bridge
sudo brctl addbr $INTERFACE_BRIDGE

# Add interfaces to the bridge
sudo brctl addif $INTERFACE_BRIDGE $INTERFACE_VETH1

# Set IP addresses for veth1 and veth0
sudo ip addr add $IP_VETH0 dev $INTERFACE_VETH0
sudo ip addr add $IP_VETH1 dev $INTERFACE_VETH1

# Bring up interfaces
sudo ip link set dev $INTERFACE_VETH0 up
sudo ip link set dev $INTERFACE_VETH1 up
sudo ip link set dev $INTERFACE_BRIDGE up

# Set IP address for the bridge
sudo ip addr add $IP_BRIDGE dev $INTERFACE_BRIDGE

# Display interface configurations
echo "Interfaces and Bridge are configured:"
sudo ip a show $INTERFACE_VETH1
sudo ip a show $INTERFACE_VETH0
sudo ip a show $INTERFACE_BRIDGE
