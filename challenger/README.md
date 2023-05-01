

# Install Azure JDK

Follow instruction here, take the latest:
https://docs.azul.com/core/zulu-openjdk/install/debian

# Install Postgresql

## Setup Postgresql

Follow instruction here, take latest
https://www.postgresql.org/download/linux/ubuntu/

## Create a new database for user bandency

export DB_USER="bandency"
export DB_PASSWORD="bandency-high-5"
export DB_DBNAME="bandency"

sudo -u postgres psql -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';"

sudo -u postgres psql -c "CREATE DATABASE $DB_DBNAME;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE $DB_DBNAME TO $DB_USER;"

echo "User '$DB_USER' and database '$DB_DBNAME' created successfully."

# Reverse proxy for caddy




# Install envoy proxy

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



