import argparse
import logging
import requests
import umsgpack
import numpy as np
from scipy.ndimage import correlate
from sklearn.cluster import DBSCAN
from PIL import Image
#import torch
import io

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("demo_client")
np.set_printoptions(precision=3, floatmode="maxprec", linewidth=None)

def main():
    parser = argparse.ArgumentParser(description="Demo Client")
    parser.add_argument("endpoint", type=str, help="Endpoint URL")
    parser.add_argument("token", type=str, default="", help="API Token")
    parser.add_argument("--limit", type=int, default=None, help="Maximum number of batches to process")
    args = parser.parse_args()

    url = args.endpoint
    token = args.token
    limit = args.limit
    session = requests.Session()

    logger.info("Starting demo client")
    
    # Create bench
    create_response = session.post(
        f"{url}/api/create",
        json={"apitoken": token, "name": "opt", "test": True},
    )
    create_response.raise_for_status()
    bench_id = create_response.json()
    logger.info(f"Created bench {bench_id}")

    # Start bench
    start_response = session.post(f"{url}/api/start/{bench_id}")
    assert start_response.status_code == 200
    logger.info(f"Started bench {bench_id}")

    i = 0
    while not limit or i < limit:
        logger.info(f"Getting batch {i}")
        
        # Get next batch
        next_batch_response = session.get(f"{url}/api/next_batch/{bench_id}")
        if next_batch_response.status_code == 404:
            break
        next_batch_response.raise_for_status()
        batch_input = umsgpack.unpackb(next_batch_response.content)

        result = process(batch_input)

        logger.info(f"Sending batch result {i}")
        # Send result
        result_serialized = umsgpack.packb(result)
        result_response = session.post(
            f"{url}/api/result/0/{bench_id}/{i}",
            data=result_serialized
        )
        assert result_response.status_code == 200
        i += 1

    # End bench
    end_response = session.post(f"{url}/api/end/{bench_id}")
    end_response.raise_for_status()
    result = end_response.text
    logger.info(f"Completed bench {bench_id}")

    print(f"Result: {result}")



def make_kernel(distance_factor, depth):
    kernel_size = distance_factor * 4 + 1
    kernel = np.zeros((depth, kernel_size, kernel_size), dtype=np.float64)
    center = distance_factor * 2
    center_depth = depth - 1
    k1 = 0
    k2 = 0
    
    for x in range(kernel_size):
        for y in range(kernel_size):
            for d in range(depth):
                if abs(x - center) + abs(y - center) + abs(d - center_depth) <= distance_factor:
                    k1 += 1
                elif abs(x - center) + abs(y - center) + abs(d - center_depth) <= 2 * distance_factor:
                    k2 += 1
    for x in range(kernel_size):
        for y in range(kernel_size):
            for d in range(depth):
                if abs(x - center) + abs(y - center) + abs(d - center_depth) <= distance_factor:
                    kernel[d, x, y] = 1/k1
                elif abs(x - center) + abs(y - center) + abs(d - center_depth) <= 2 * distance_factor:
                    kernel[d, x, y] = -1/k2
    return kernel

def corr2d_scipy(data, kernel):
    conv = correlate(data, kernel, mode='constant', cval=0.0)
    return abs(conv[kernel.shape[0]//2])

def corr2d_torch(data, kernel):
    data_torch = torch.tensor(data).unsqueeze(0)  # Add batch and channel dimensions
    kernel_torch = torch.tensor(kernel).unsqueeze(0)

    result_torch = torch.nn.functional.conv2d(data_torch, kernel_torch, padding='same')
    
    return torch.abs(result_torch).squeeze().numpy()  # Convert back to NumPy

def compute_outliers_opt(image3d, empty_threshold, saturation_threshold, distance_threshold, outlier_threshold):
    """
    Computes outlier points from a 3D matrix using convolution to calculate neighborhood averages.

    Args:
        matrix_3d (np.ndarray): A 3D numpy array (depth, rows, cols).
        empty_threshold (int): The minimum value for a point to be considered.
        distance_threshold (int): The Manhattan distance threshold for neighbors.
        outlier_threshold (float): The minimum distance for a point to be considered an outlier.

    Returns:
        list of tuples: Each tuple contains (z, row, col, distance) for outlier points.
    """
    
    depth, width, height = image3d.shape
    image3d = image3d.astype(np.float64)

    kernel = make_kernel(distance_threshold, depth)

    print(image3d.shape)
    print(kernel.shape)

    dev = corr2d_scipy(image3d, kernel)
    # dev = corr2d_torch(image3d, kernel)

    # assert np.average(abs(dev-devt)) < 1.0


    # Identify valid points that exceed the thresholds
    outliers = []
    
    mask = (image3d[-1,:,:] > empty_threshold) & (image3d[-1,:,:] < saturation_threshold)
    for y in range(height):
        for x in range(width):
            if mask[x, y] and dev[x, y] > outlier_threshold:
                outliers.append((x, y, dev[x, y]))

    return outliers

def cluster_outliers_2d(outliers, eps=20, min_samples=5):
    """
    Applies DBSCAN clustering to outliers on a 2D plane.

    Args:
        outliers (list): A list of tuples, where each tuple is (z, row, col, distance).
        eps (float): The maximum distance between two samples for one to be considered as in the neighborhood of the other.
        min_samples (int): The number of samples (or total weight) in a neighborhood for a point to be considered a core point.

    Returns:
        TODO dict: A dictionary with cluster labels as keys and lists of outlier indices as values.
              Points labeled as -1 are considered noise.
    """
    if len(outliers) == 0:
        return []
    
    # Extract 2D positions (row, col) for clustering
    positions = np.array([(outlier[0], outlier[1]) for outlier in outliers])
    
    # Apply DBSCAN clustering
    clustering = DBSCAN(eps=eps, min_samples=min_samples).fit(positions)
    labels = clustering.labels_  # Cluster labels (-1 means noise)
    
    # Group points by cluster and calculate centroids and sizes
    clusters = []
    for label in set(labels):
        if label == -1:
            continue  # Skip noise points
        # Get all points in the current cluster
        cluster_points = positions[labels == label]
        # Calculate the centroid
        centroid = cluster_points.mean(axis=0)
        # Store the centroid and size of the cluster
        clusters.append({
            'x': centroid[0],
            'y': centroid[1],
            'count': len(cluster_points)
        })
    
    return clusters


tile_map = dict()

def process(batch):
    print_id = batch["print_id"]
    tile_id = batch["tile_id"]
    batch_id = batch["batch_id"]
    layer = batch["layer"]
    image = Image.open(io.BytesIO(batch["tif"]))

    if not (print_id, tile_id) in tile_map:
        tile_map[(print_id, tile_id)] = []

    window = tile_map[(print_id, tile_id)]
    if len(window) == 3:
        window.pop(0)

    window.append(image)

    if len(window) == 3:
        matrix_3d = np.stack(window, axis=0)

        outliers = compute_outliers_opt(matrix_3d, 5000, 65000, 2, 6000)
        centroids = cluster_outliers_2d(outliers, 20, 5)
    else:
        centroids = []

    saturated = np.count_nonzero(np.array(image) > 65000)

    result = {
        "batch_id": batch_id,
        "print_id": print_id,
        "tile_id": tile_id,
        "saturated": saturated,
        "centroids": centroids
    }

    print(result)
    return result



if __name__ == "__main__":
    main()