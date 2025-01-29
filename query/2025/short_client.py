import requests
import os

# PARAMS = {
#     "apitoken": "<API TOKEN>",
#     "test": True,
#     "name": "<benchmark name>"
# }

# REST_SRVR="http://localhost:52923/api"
# PARAMS = {        localhost                           localcluster
#     "apitoken": "gfbmznvoywwogmwepbvvnbjbrxacvded", "vynyonxizjnizuphrurbuylnlxryufjc"
#     "test": True,
#     "name": "Test1"
# }

REST_SRVR = os.environ.get("REST_SERVER_ADDRESS", "http://localhost:52923/api") # Change the dafault to challenge2025.debs.org
PARAMS = {
    "apitoken": os.environ.get("API_TOKEN", ""),
    "test": True,
    "name": "Test1"
}

bench_id = requests.post(REST_SRVR+"/create", json=PARAMS, headers={"Content-Type": "application/json"}).json()["bench_id"]
requests.post(REST_SRVR+"/start/{}".format(bench_id))
batch = requests.get(REST_SRVR+"/next_batch/{}".format(bench_id))

while(batch.status_code == 200):
    results = {} # solution(batch)
    requests.post(REST_SRVR+"/result/{}/{}".format(bench_id, batch.json()["batch_number"]))
    batch = requests.get(REST_SRVR+"/next_batch/{}".format(bench_id))

requests.post(REST_SRVR+"/end/{}".format(bench_id))
