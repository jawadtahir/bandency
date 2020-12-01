#!/usr/bin/env bash

python -m grpc_tools.protoc -I../challenger/src/main/proto --python_out=. --grpc_python_out=. ../challenger/src/main/proto/challenger.proto

echo "Regenerated Client API"
