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
        """
        Traffic-aware driving route.
        """

        return await self._compute_route(
            pickup_lat=pickup_lat,
            pickup_lon=pickup_lon,
            dropoff_lat=dropoff_lat,
            dropoff_lon=dropoff_lon,
            travel_mode="DRIVE",
            traffic_aware=True,
        )

    async def get_walking_route(
        self,
        pickup_lat: float,
        pickup_lon: float,
        dropoff_lat: float,
        dropoff_lon: float,
    ) -> dict:
        """
        Walking route between two nearby points.
        """

        return await self._compute_route(
            pickup_lat=pickup_lat,
            pickup_lon=pickup_lon,
            dropoff_lat=dropoff_lat,
            dropoff_lon=dropoff_lon,
            travel_mode="WALK",
            traffic_aware=False,
        )

    async def _compute_route(
        self,
        pickup_lat: float,
        pickup_lon: float,
        dropoff_lat: float,
        dropoff_lon: float,
        travel_mode: str,
        traffic_aware: bool,
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
            "travelMode": travel_mode,
            "computeAlternativeRoutes": False,
            "languageCode": "en-US",
            "units": "IMPERIAL",
        }

        if traffic_aware:
            payload["routingPreference"] = "TRAFFIC_AWARE"

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
                f"Google Routes returned no {travel_mode.lower()} route."
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
            "trip_miles": round(
                trip_miles,
                2,
            ),
            "trip_minutes": round(
                trip_minutes,
                1,
            ),
            "distance_meters": int(
                distance_meters
            ),
            "duration_seconds": round(
                duration_seconds,
                1,
            ),
        }

    @staticmethod
    def _parse_duration(
        duration: str,
    ) -> float:
        return float(
            duration.rstrip("s")
        )


route_service = RouteService()