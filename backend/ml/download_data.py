"""Download an official NYC TLC High Volume FHV monthly Parquet file.

Example:
    python -m ml.download_data --year 2025 --month 1
"""

from __future__ import annotations

import argparse
import urllib.request
from pathlib import Path


BASE_URL = "https://d37ci6vzurychx.cloudfront.net/trip-data"
ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--year", type=int, required=True)
    parser.add_argument("--month", type=int, choices=range(1, 13), required=True)
    args = parser.parse_args()

    filename = f"fhvhv_tripdata_{args.year}-{args.month:02d}.parquet"
    url = f"{BASE_URL}/{filename}"
    output = DATA_DIR / filename
    DATA_DIR.mkdir(parents=True, exist_ok=True)

    print(f"Downloading {url}")
    urllib.request.urlretrieve(url, output)
    print(f"Saved to {output}")


if __name__ == "__main__":
    main()
