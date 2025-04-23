# How to run Challenger 2.0

## Prereq
Dataset
Python3
Kubernetes cluster

1. Download the dataset.
2. Install [k3d](https://k3d.io/stable/)
3. Create a cluster
```bash
k3d cluster create challenger2 -s 1 -a 1 -v TEST_DATA_DIR:/dataDir@server:0 -v EVAL_DATA_DIR:/dataDirEval@server:0 -v MONGO_DATA_DIR:/data/db@server:0 -v /home/foobar/PhD/Data/C3/pv:/pv@agent:0 -p 52928:30000@server:0 -p 52929:30001@server:0 -p 52930:30000@agent:0 --k3s-node-label "org.debs/type=server@server:0" --k3s-node-label "org.debs/type=agent@agent:0"
```
4. Install [Chaos Mesh](https://chaos-mesh.org/docs/quick-start/). TL;DR 
```bash
curl -sSL https://mirrors.chaos-mesh.org/v2.7.0/install.sh | bash -s -- --k3s
```

## Start MongoDB and REST Server

Run
```bash
kubectl create -f deploy/mongodb.yaml
```

## Start the frontend

Run
```bash
cd website
export DB_CONNECTION_STRING="mongodb://0.0.0.0:52928"
pip3 install -r frontend/requirements.txt
python3 webserver.py
```

Go to http://localhost:8000 and start using the system
