import requests
import umsgpack
import os

REST_SRVR =  "http://challenge2025.debs.org:52923/api"
PARAMS = {
    "apitoken": os.environ.get("API_TOKEN"),
    "test": True,
    "name": "Test1"
}

bench_id = requests.post(REST_SRVR+"/create", json=PARAMS).json()
requests.post(REST_SRVR+"/start/{}".format(bench_id))
batch = requests.get(REST_SRVR+"/next_batch/{}".format(bench_id))

while(batch.status_code == 200):
    batch = umsgpack.unpackb(batch.content)
    print(batch["batch_id"])
    results = {} # solution(batch)
    requests.post(REST_SRVR+"/result/0/{}/{}".format(bench_id, batch["batch_id"]))
    batch = requests.get(REST_SRVR+"/next_batch/{}".format(bench_id))

requests.post(REST_SRVR+"/end/{}".format(bench_id))
