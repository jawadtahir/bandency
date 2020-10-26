import aio_pika
import logging
from shared.constants import get_config

async def process_server_monitor_metrics(loop, rabbit_connection_str):
    connection = await aio_pika.connect_robust(rabbit_connection_str, loop=loop)
    queue_name = get_config()["servermonitor_topic"]
    logging.debug("connected to rabbitmq")

    async with connection:
        channel = await connection.channel()
        queue = await channel.declare_queue(queue_name, durable=True)

        async with queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    print(message.body)
                    if queue.name in message.body.decode():
                        break


