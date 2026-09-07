from pathlib import Path

import geopandas as gpd
from shapely.geometry import Point


ROOT = Path(__file__).resolve().parents[2]
SHAPEFILE = ROOT / "data" / "taxi_zones" / "taxi_zones.shp"


class TaxiZoneResolver:
    def __init__(self) -> None:
        if not SHAPEFILE.exists():
            raise FileNotFoundError(
                f"Taxi-zone shapefile not found at {SHAPEFILE}"
            )

        zones = gpd.read_file(SHAPEFILE)

        # Convert TLC geometry into standard GPS coordinates.
        self.zones = zones.to_crs("EPSG:4326")

    def resolve(self, latitude: float, longitude: float) -> int:
        point = Point(longitude, latitude)

        matches = self.zones[
            self.zones.geometry.covers(point)
        ]

        if matches.empty:
            raise ValueError(
                "Coordinates are outside supported NYC TLC taxi zones."
            )

        return int(matches.iloc[0]["LocationID"])


taxi_zone_resolver = TaxiZoneResolver()