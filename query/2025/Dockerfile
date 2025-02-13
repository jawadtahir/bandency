FROM python:3.12

WORKDIR /usr/src/app

RUN pip install --no-cache-dir requests umsgpack

COPY . .

ENV API_TOKEN=""

CMD [ "python", "./short_client.py" ]