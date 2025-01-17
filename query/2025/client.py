import requests
import json

REST_SERVER_ADDR="http://localhost:52923/benchmark"

TOKEN="gfbmznvoywwogmwepbvvnbjbrxacvded"
BENCHMARK_TYPE="test"
BENCHMARK_NAME="test"


CREATE_PARAMS={
    "token": TOKEN,
    "benchmarkType": BENCHMARK_TYPE,
    "benchmarkName": BENCHMARK_NAME
}

CREATE_BENCHMARK = "/create-benchmark"
START_BENCHMARK = "/start-benchmark/{}"
NEXT_BATCH = "/next-batch/{}"
RESULT = "/result/{}/{}/{}"
END_BENCHMARK = "/end-benchmark/{}"

benchmark = requests.post(REST_SERVER_ADDR+CREATE_BENCHMARK, json=CREATE_PARAMS, headers={"Content-Type": "application/json"})
# benchmark = requests.post(REST_SERVER_ADDR+CREATE_BENCHMARK, headers={"Content-Type": "application/json"})

if benchmark.ok:
    print(benchmark.json())
else:
    print(benchmark.content.decode("utf8"))

benchamrk_id = benchmark.json()["benchmark_id"]

start = requests.post(REST_SERVER_ADDR+START_BENCHMARK.format(benchamrk_id))

batch = requests.get(REST_SERVER_ADDR+NEXT_BATCH.format(benchamrk_id))

while(batch.json()["last"] != True):
    result_q1 = []
    result_q2 = []
    seq_id = batch.json()["seq_id"]
    print(batch.json()["seq_id"])

    response_q1 = {"result": result_q1}
    response_q2 = {"result": result_q2}

    requests.post(REST_SERVER_ADDR+RESULT.format(benchamrk_id, seq_id, 1), response_q1, headers={"content-type": "application/json"})
    requests.post(REST_SERVER_ADDR+RESULT.format(benchamrk_id, seq_id, 2), response_q2, headers={"content-type": "application/json"})

    batch = requests.post(REST_SERVER_ADDR+NEXT_BATCH.format(benchamrk_id))


requests.get(REST_SERVER_ADDR+END_BENCHMARK.format(benchamrk_id))