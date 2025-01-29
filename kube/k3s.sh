#! /bin/bash
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--https-listen-port 10031 --service-node-port-range 10000-12767" sh -
curl -sfL https://get.k3s.io | K3S_URL=https://<SERVER_IP>:6443 K3S_TOKEN=<TOKEN> sh -