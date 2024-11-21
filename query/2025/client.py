import requests

REST_SERVER_ADDR="http://localhost:52923/benchmark"

TOKEN="testtesttesttest"
BENCHMARK_TYPE="test"
BENCHMARK_NAME="test"
QUERIES="q1"

CREATE_PARAMS={
    "token": TOKEN,
    "benchmarkType": BENCHMARK_TYPE,
    "benchmarkName": BENCHMARK_NAME,
    "queries": QUERIES
}

CREATE_BENCHMARK = "/create-benchmark"
START_BENCHMARK = "/start-benchmark/{}"
NEXT_BATCH = "/next-batch/{}"
RESULT = "/result/{}/{}/{}"
END_BENCHMARK = "/end-benchmark/{}"

benchmark = requests.get(REST_SERVER_ADDR+CREATE_BENCHMARK, params=CREATE_PARAMS)


print(benchmark.json())

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

    batch = requests.get(REST_SERVER_ADDR+NEXT_BATCH.format(benchamrk_id))


requests.post(REST_SERVER_ADDR+END_BENCHMARK.format(benchamrk_id))