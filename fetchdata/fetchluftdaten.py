import glob
import os
from datetime import datetime
from time import time
import urllib.request
import pandas as pd
from bs4 import BeautifulSoup
import random

from tqdm import tqdm

# from elasticsearch import Elasticsearch
# from elasticsearch.helpers import bulk

# define the initial values
resource_url = "https://archive.sensor.community/csv_per_month/"
data_directory = '/run/media/chris/debs2021/luftdaten'

# Initial version taken from here:
# https://github.com/ieservices/luftdaten-big-data-analysis.info
# Modified

def fetch_links(resource):
    urls = []
    try:
        resp = urllib.request.urlopen(resource)
        soup = BeautifulSoup(resp, "html5lib", from_encoding=resp.info().get_param('charset'))

        for link in soup.find_all('a', href=True):
            if link['href'] not in ['/', '../', 'temp/'] and "csv_per_month" not in link['href']:
                urls.append([resource + link['href'], link['href']])
    except Exception as e:
        print("Error occurred in fetching the data from: {}. Details:\n  {}".format(resource_url, e))

    return urls

def urls_every_month():
    download_urls = []
    for url, _ in tqdm(fetch_links(resource_url), desc="Fetching download URLs"):
        for fileurl, filename in fetch_links(url):
            local_filename = os.path.join(data_directory, filename)
            if not os.path.exists(local_filename):
                download_urls.append([fileurl, local_filename])
    return download_urls
    

def download_every_month():
    urls = urls_every_month()
    random.shuffle(urls)
    
    for fileurl, local_filename in tqdm(urls, desc="Downloading files"):
        if not os.path.exists(local_filename):
            try:
                with urllib.request.urlopen(fileurl) as req:
                    with open(local_filename, 'wb') as lf:
                        while True:
                            chunk = req.read(1*1000*1000) #Write 1MB chunks
                            if not chunk:
                                break;
                            lf.write(chunk)
            except KeyboardInterrupt:
                print("deleting unfinshed download: {}".format(local_filename))
                if not os.path.exists(local_filename):
                    os.remove(local_filename)
                print("Exiting")
                return

def prepare_data_directory():
    if not os.path.exists(data_directory):
        os.makedirs(data_directory)

if __name__ == "__main__":
    prepare_data_directory()
    # fetch the url of the last directory
    download_every_month()