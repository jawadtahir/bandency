# How to run Challenger 2.0

## Prereq
Kubernetes cluster

## Set up

1. Make one node as server to host web interface, challenger and DB and one as agent to run solutions by setting labels to nodes.
```bash
k label nodes/<nodeName> org.debs/type=server
k label nodes/<nodeName> org.debs/type=agent
```

2. Install [Chaos Mesh](https://chaos-mesh.org/docs/quick-start/). TL;DR (on server)
```
curl -sSL https://mirrors.chaos-mesh.org/v2.7.0/install.sh | bash -s -- --k3s
```