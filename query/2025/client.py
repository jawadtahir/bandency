import requests
import json

REST_SERVER_ADDR="http://localhost:52923/api"

TOKEN="gfbmznvoywwogmwepbvvnbjbrxacvded"
BENCHMARK_TYPE=True
BENCHMARK_NAME="test"


CREATE_PARAMS={
    "apitoken": TOKEN,
    "test": BENCHMARK_TYPE,
    "name": BENCHMARK_NAME
}

CREATE_BENCHMARK = "/create"
START_BENCHMARK = "/start/{}"
NEXT_BATCH = "/next_batch/{}"
RESULT = "/result/{}/{}/{}"
END_BENCHMARK = "/end/{}"

benchmark = requests.post(REST_SERVER_ADDR+CREATE_BENCHMARK, json=CREATE_PARAMS, headers={"Content-Type": "application/json"})
# benchmark = requests.post(REST_SERVER_ADDR+CREATE_BENCHMARK, headers={"Content-Type": "application/json"})

if benchmark.ok:
    print(benchmark.json())
else:
    print(benchmark.content.decode("utf8"))

benchamrk_id = benchmark.json()["bench_id"]

start = requests.post(REST_SERVER_ADDR+START_BENCHMARK.format(benchamrk_id))

batch = requests.get(REST_SERVER_ADDR+NEXT_BATCH.format(benchamrk_id))

while(batch.status_code == 200):
    result_q1 = []
    result_q2 = []
    seq_id = batch.json()["batch_number"]
    print(batch.json()["batch_number"])

    response_q1 = {"result": result_q1}
    response_q2 = {"result": result_q2}

    requests.post(REST_SERVER_ADDR+RESULT.format(0, benchamrk_id, seq_id), response_q1, headers={"content-type": "application/json"})
    # requests.post(REST_SERVER_ADDR+RESULT.format(benchamrk_id, seq_id, 2), response_q2, headers={"content-type": "application/json"})

    batch = requests.get(REST_SERVER_ADDR+NEXT_BATCH.format(benchamrk_id))


requests.post(REST_SERVER_ADDR+END_BENCHMARK.format(benchamrk_id))