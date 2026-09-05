import os

import httpx
from dotenv import load_dotenv


load_dotenv()

GOOGLE_ROUTES_URL = (
    "https://routes.googleapis.com/directions/v2:computeRoutes"
)


class RouteService:
    def __init__(self) -> None:
        self.api_key = os.getenv("GOOGLE_ROUTES_API_KEY")

        if not self.api_key:
            raise RuntimeError(
                "GOOGLE_ROUTES_API_KEY is not configured."
            )

    async def get_route(
        self,
        pickup_lat: float,
        pickup_lon: float,
        dropoff_lat: float,
        dropoff_lon: float,
    ) -> dict:

        payload = {
            "origin": {
                "location": {
                    "latLng": {
                        "latitude": pickup_lat,
                        "longitude": pickup_lon,
                    }
                }
            },
            "destination": {
                "location": {
                    "latLng": {
                        "latitude": dropoff_lat,
                        "longitude": dropoff_lon,
                    }
                }
            },
            "travelMode": "DRIVE",
            "routingPreference": "TRAFFIC_AWARE",
            "computeAlternativeRoutes": False,
            "languageCode": "en-US",
            "units": "IMPERIAL",
        }

        headers = {
            "Content-Type": "application/json",
            "X-Goog-Api-Key": self.api_key,
            "X-Goog-FieldMask": (
                "routes.distanceMeters,"
                "routes.duration,"
                "routes.staticDuration"
            ),
        }

        async with httpx.AsyncClient(
            timeout=15.0
        ) as client:
            response = await client.post(
                GOOGLE_ROUTES_URL,
                json=payload,
                headers=headers,
            )

            response.raise_for_status()

        data = response.json()

        routes = data.get("routes", [])

        if not routes:
            raise ValueError(
                "Google Routes returned no drivable route."
            )

        route = routes[0]

        distance_meters = route["distanceMeters"]

        duration_seconds = self._parse_duration(
            route["duration"]
        )

        trip_miles = (
            distance_meters / 1609.344
        )

        trip_minutes = (
            duration_seconds / 60.0
        )

        return {
            "trip_miles": round(trip_miles, 2),
            "trip_minutes": round(trip_minutes, 1),
            "distance_meters": distance_meters,
            "duration_seconds": duration_seconds,
        }

    @staticmethod
    def _parse_duration(
        duration: str,
    ) -> float:
        # Google returns values such as "2134s"
        return float(duration.rstrip("s"))


route_service = RouteService()