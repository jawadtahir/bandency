import aio_pika
import logging
import json
import uuid
from datetime import datetime

from aio_pika.exceptions import QueueEmpty

from frontend.models import ServerMonitorMetrics
from shared.constants import get_config
from shared.util import raise_shutdown


async def process_server_monitor_metrics(loop, shutdown_trigger, rabbit_connection_str):
    logging.info("start process_server_monitor_metrics")

    connection = await aio_pika.connect_robust(rabbit_connection_str, loop=loop)
    queue_name = get_config()["servermonitor_topic"]

    logging.debug("connected to rabbitmq")

    async with connection:
        channel = await connection.channel()
        queue = await channel.declare_queue(queue_name, durable=True)

        while not shutdown_trigger.is_set():
            try:
                message = await queue.get(timeout=5)
            except QueueEmpty:
                continue

            if message:
                async with message.process(): #Acknowledges the message
                    logging.debug("received message - topic: {}, body: {}".format(queue_name, message.body))
                    d = json.loads(message.body)

                    metric = ServerMonitorMetrics(id=uuid.uuid4(),
                                                  server_name=d['server_name'],
                                                  timestamp=datetime.fromisoformat(d['timestamp']),
                                                  cpu_percent=d['cpu_percent'],
                                                  load1m=d['load1m'],
                                                  load5m=d['load5m'],
                                                  load15m=d['load15m'],
                                                  mem_total=d['mem_total'],
                                                  mem_available=d['mem_available'],
                                                  mem_used=d['mem_used'],
                                                  mem_free=d['mem_free'],
                                                  duration_millis=d['duration_millis'],
                                                  read_count=d['read_count'],
                                                  write_count=d['write_count'],
                                                  read_bytes=d['read_bytes'],
                                                  write_bytes=d['write_bytes'])

                    await metric.create()
                    logging.debug("inserted new ServerMonitorMetrics {}".format(metric.id))