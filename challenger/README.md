

# Install Azure JDK

Follow instruction here, take the latest:
https://docs.azul.com/core/zulu-openjdk/install/debian

# Install Postgresql

## Setup Postgresql

Follow instruction here, take latest
https://www.postgresql.org/download/linux/ubuntu/

## Create a new database for user bandency

'''bash
export DB_USER="bandency"
export DB_PASSWORD="bandency-high-5"
export DB_DBNAME="bandency"

sudo -u postgres psql -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';"

sudo -u postgres psql -c "CREATE DATABASE $DB_DBNAME;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE $DB_DBNAME TO $DB_USER;"
sudo -u postgres psql -c "GRANT ALL ON SCHEMA public TO '$DB_USER';"
sudo -u postgres psql -c "ALTER DATABASE $DB_DBNAME OWNER TO $DB_USER;"

sudo -u postgres psql -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"";

echo "User '$DB_USER' and database '$DB_DBNAME' created successfully."
'''

## Initialize DB

Install pyenv, using pyenv install python, create new environment, install dependencies in the environment


'''bash

# Install pyenv
curl https://pyenv.run | bash

# Install the dependencies to build a python version
sudo apt update
sudo apt install build-essential libssl-dev zlib1g-dev \
libbz2-dev libreadline-dev libsqlite3-dev curl \
libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev

# Needed for python postgresql driver and libvirt
sudo apt install libpq-dev libvirt-dev

# Install python and create virtualenv
pyenv install 3.11.5
pyenv virtualenv 3.11.5 bandency




'''


# Reverse proxy for website

## Install Caddy
https://caddyserver.com/docs/install#debian-ubuntu-raspbian

## Create a caddy file




# Reverse proxy for grpc

Follow instruction here, take latest
https://www.envoyproxy.io/docs/envoy/latest/start/install#install-envoy-on-ubuntu-linux

## Systemd unit file for envoy

sudo vim /etc/systemd/system/envoy.service

-------------
[Unit]
Description=Envoy Proxy

[Service]
ExecStart=/usr/local/bin/envoy -c /etc/envoy/envoy.yaml
Restart=always
User=envoy
Group=envoy
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target

-------------

## Enable it and autostart

'''bash
sudo mkdir /etc/envoy/
sudo vim /etc/envoy/envoy.yaml



sudo systemctl enable envoy
sudo systemctl start envoy


'''


