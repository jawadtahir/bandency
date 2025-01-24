import requests

PARAMS = {
    "apitoken": "<API TOKEN>",
    "test": True,
    "name": "<benchmark name>"
}

bench_id = requests.post("/create", {PARAMS}).json()["bench_id"]
requests.post("/start/{}".format(bench_id))
batch = requests.get("/next_batch/{}".format(bench_id))

while(batch.status_code == 200):
    results = {} # solution(batch)
    requests.post("/result/{}/{}".format(bench_id, batch.json()["batch_number"]))
    batch = requests.get("/next_batch/{}".format(bench_id))

requests.post("/end/{}".format(bench_id))
