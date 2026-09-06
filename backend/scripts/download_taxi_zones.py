from pathlib import Path
from urllib.request import urlretrieve
from zipfile import ZipFile


ROOT = Path(__file__).resolve().parents[1]

DATA_DIR = ROOT / "data"
ZONE_DIR = DATA_DIR / "taxi_zones"

ZIP_PATH = DATA_DIR / "taxi_zones.zip"

URL = (
    "https://d37ci6vzurychx.cloudfront.net/"
    "misc/taxi_zones.zip"
)


def main() -> None:
    DATA_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    if (
        ZONE_DIR.exists()
        and any(
            ZONE_DIR.glob("*.shp")
        )
    ):
        print(
            "Taxi zone files already exist."
        )
        return

    print(
        "Downloading NYC TLC taxi zones..."
    )

    urlretrieve(
        URL,
        ZIP_PATH,
    )

    ZONE_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    with ZipFile(
        ZIP_PATH,
        "r",
    ) as archive:
        archive.extractall(
            ZONE_DIR
        )

    ZIP_PATH.unlink(
        missing_ok=True
    )

    print(
        f"Taxi zones installed at "
        f"{ZONE_DIR}"
    )


if __name__ == "__main__":
    main()