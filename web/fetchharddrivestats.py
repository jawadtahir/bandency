import asyncio
import os
from os.path import isfile

import aiohttp as ai
from aiofile import async_open

import tqdm


async def download(url: str, target: str) -> None:
    temp_name = f"downloading_{target}.zip"
    final_name = f"fetchdata/{target}.zip"
    if os.path.isfile(final_name):
        print(f"Already downloaded - {final_name}")
    else:
        if os.path.isfile(temp_name):
            os.remove(temp_name)
        async with ai.ClientSession() as session:
            async with session.get(url) as resp:
                size = int(resp.headers.get('Content-Length', 0)) or None
                with tqdm.tqdm(desc=f"Download for {target}", total=size) as pb:
                    async with async_open(temp_name, mode="wb") as f:
                        async for chunk in resp.content.iter_chunked(1024*10):
                            await f.write(chunk)
                            pb.update(len(chunk))

                # we don't want halve files, only rename after everything downloaded
                os.rename(temp_name, final_name)


async def main():
    baseurl = "https://f001.backblazeb2.com/file/Backblaze-Hard-Drive-Data/"

    urls = [(baseurl + "data_Q1_2023.zip", "202301"),
            (baseurl + "data_Q2_2023.zip", "202302"),
            (baseurl + "data_Q3_2023.zip", "202303"),
            (baseurl + "data_Q4_2023.zip", "202304"),
            ]
    for url, target in urls:
        await download(url, target)


if __name__ == "__main__":
    asyncio.run(main())
