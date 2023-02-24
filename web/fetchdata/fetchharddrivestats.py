import asyncio
import os
from os.path import isfile

import aiohttp as ai
import tqdm


async def download(url: str, target: str) -> None:
    temp_name = f"downloading_{target}.zip"
    final_name = f"{target}.zip"
    if os.path.isfile(final_name):
        print(f"Already downloaded - {final_name}")
    else:
        if os.path.isfile(temp_name):
            os.remove(temp_name)
        async with ai.ClientSession() as session:
            async with session.get(url) as resp:
                size = int(resp.headers.get('Content-Length', 0)) or None
                pb = tqdm.tqdm(desc=f"Download for {target}", total=size)

                with open(temp_name, mode="wb") as f, pb:
                    async for chunk in resp.content.iter_chunked(1024):
                        # to bad there is still no async file io library in python in 2023 ...
                        f.write(chunk)
                        pb.update(len(chunk))

                # we don't want halve files, only rename after everything downloaded
                os.rename(temp_name, final_name)


async def main():
    baseurl = "https://f001.backblazeb2.com/file/Backblaze-Hard-Drive-Data/"

    urls = [(baseurl + "data_Q1_2022.zip", "202201"),
            (baseurl + "data_Q2_2022.zip", "202202"),
            (baseurl + "data_Q3_2022.zip", "202203"),
            (baseurl + "data_Q4_2022.zip", "202204")
            ]
    for url, target in urls:
        await download(url, target)


if __name__ == "__main__":
    asyncio.run(main())
