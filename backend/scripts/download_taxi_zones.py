from pathlib import Path
from urllib.request import urlretrieve
from zipfile import ZipFile
import shutil
import tempfile


ROOT = Path(__file__).resolve().parents[1]

DATA_DIR = ROOT / "data"
ZONE_DIR = DATA_DIR / "taxi_zones"

URL = "https://d37ci6vzurychx.cloudfront.net/misc/taxi_zones.zip"


def main():
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    ZONE_DIR.mkdir(parents=True, exist_ok=True)

    expected_shapefile = ZONE_DIR / "taxi_zones.shp"

    if expected_shapefile.exists():
        print(f"Taxi zones already installed at {ZONE_DIR}")
        return

    print("Downloading NYC TLC taxi zones...")

    with tempfile.TemporaryDirectory() as temp_dir:
        temp_dir = Path(temp_dir)

        zip_path = temp_dir / "taxi_zones.zip"
        extract_dir = temp_dir / "extracted"

        urlretrieve(URL, zip_path)

        extract_dir.mkdir(parents=True, exist_ok=True)

        with ZipFile(zip_path, "r") as archive:
            archive.extractall(extract_dir)

        shapefiles = list(
            extract_dir.rglob("taxi_zones.shp")
        )

        if not shapefiles:
            raise FileNotFoundError(
                "taxi_zones.shp was not found inside the downloaded archive."
            )

        source_shapefile = shapefiles[0]
        source_directory = source_shapefile.parent

        print(
            f"Found taxi-zone files in: {source_directory}"
        )

        required_extensions = [
            ".shp",
            ".shx",
            ".dbf",
            ".prj",
        ]

        optional_extensions = [
            ".cpg",
            ".xml",
        ]

        for extension in required_extensions:
            source = (
                source_directory
                / f"taxi_zones{extension}"
            )

            if not source.exists():
                raise FileNotFoundError(
                    f"Required taxi-zone file missing: {source.name}"
                )

            shutil.copy2(
                source,
                ZONE_DIR / source.name
            )

        for extension in optional_extensions:
            source = (
                source_directory
                / f"taxi_zones{extension}"
            )

            if source.exists():
                shutil.copy2(
                    source,
                    ZONE_DIR / source.name
                )

    if not expected_shapefile.exists():
        raise RuntimeError(
            "Taxi-zone installation failed."
        )

    print(
        f"Taxi zones installed successfully at {ZONE_DIR}"
    )


if __name__ == "__main__":
    main()