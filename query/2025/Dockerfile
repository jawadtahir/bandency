FROM python:3.12

WORKDIR /usr/src/app

RUN pip install --no-cache-dir requests

COPY . .

ENV REST_SERVER_ADDRESS=http://localhost:52923/api

CMD [ "python", "./client.py" ]