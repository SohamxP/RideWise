import math

from datetime import (
    datetime,
    timedelta,
)

from zoneinfo import ZoneInfo

from fastapi import (
    FastAPI,
    HTTPException,
)

from app.schemas import (
    AnalyzeTripRequest,
    AnalyzeTripResponse,
    FarePredictionRequest,
    FarePredictionResponse,
    HealthResponse,
    RouteInfo,
    WaitAndSaveResponse,
    WaitOption,
    WalkNearbyOption,
    WalkNearbyResponse,
)

from app.services.predictor import (
    FarePredictor,
)

from app.services.route_service import (
    route_service,
)


app = FastAPI(
    title="RideWise Prediction API",
    version="0.4.0",
    description=(
        "Route-aware provider-specific "
        "historical fare predictions trained "
        "from public NYC TLC High Volume "
        "For-Hire Vehicle trip records."
    ),
)

predictor = FarePredictor()


@app.get(
    "/health",
    response_model=HealthResponse,
)
def health() -> HealthResponse:

    loaded = (
        predictor.loaded_providers
    )

    return HealthResponse(
        status=(
            "ok"
            if len(loaded) == 2
            else "degraded"
        ),
        models_loaded=loaded,
    )


@app.post(
    "/v1/predict-fares",
    response_model=FarePredictionResponse,
)
def predict_fares(
    request: FarePredictionRequest,
) -> FarePredictionResponse:

    try:
        predictions = (
            predictor.predict(
                request
            )
        )

    except RuntimeError as exc:
        raise HTTPException(
            status_code=503,
            detail=str(exc),
        ) from exc

    except ValueError as exc:
        raise HTTPException(
            status_code=422,
            detail=str(exc),
        ) from exc

    return FarePredictionResponse(
        predictions=predictions,
    )


@app.post(
    "/v1/analyze-trip",
    response_model=AnalyzeTripResponse,
)
async def analyze_trip(
    request: AnalyzeTripRequest,
) -> AnalyzeTripResponse:

    try:
        route = await (
            route_service.get_route(
                pickup_lat=(
                    request.pickup_lat
                ),
                pickup_lon=(
                    request.pickup_lon
                ),
                dropoff_lat=(
                    request.dropoff_lat
                ),
                dropoff_lon=(
                    request.dropoff_lon
                ),
            )
        )

    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=(
                f"Route lookup failed: "
                f"{exc}"
            ),
        ) from exc

    now_nyc = datetime.now(
        ZoneInfo(
            "America/New_York"
        )
    )

    prediction_request = (
        FarePredictionRequest(
            trip_miles=(
                route["trip_miles"]
            ),
            trip_minutes=(
                route["trip_minutes"]
            ),
            pickup_hour=(
                now_nyc.hour
            ),
            day_of_week=(
                now_nyc.weekday()
            ),
            pickup_lat=(
                request.pickup_lat
            ),
            pickup_lon=(
                request.pickup_lon
            ),
            dropoff_lat=(
                request.dropoff_lat
            ),
            dropoff_lon=(
                request.dropoff_lon
            ),
        )
    )

    try:
        predictions = (
            predictor.predict(
                prediction_request
            )
        )

    except RuntimeError as exc:
        raise HTTPException(
            status_code=503,
            detail=str(exc),
        ) from exc

    except ValueError as exc:
        raise HTTPException(
            status_code=422,
            detail=str(exc),
        ) from exc

    return AnalyzeTripResponse(
        route=RouteInfo(
            trip_miles=(
                route["trip_miles"]
            ),
            trip_minutes=(
                route["trip_minutes"]
            ),
        ),
        predictions=predictions,
    )


@app.post(
    "/v1/wait-and-save",
    response_model=WaitAndSaveResponse,
)
async def wait_and_save(
    request: AnalyzeTripRequest,
) -> WaitAndSaveResponse:

    try:
        route = await (
            route_service.get_route(
                pickup_lat=(
                    request.pickup_lat
                ),
                pickup_lon=(
                    request.pickup_lon
                ),
                dropoff_lat=(
                    request.dropoff_lat
                ),
                dropoff_lon=(
                    request.dropoff_lon
                ),
            )
        )

    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=(
                f"Route lookup failed: "
                f"{exc}"
            ),
        ) from exc

    now_nyc = datetime.now(
        ZoneInfo(
            "America/New_York"
        )
    )

    base_request = (
        FarePredictionRequest(
            trip_miles=(
                route["trip_miles"]
            ),
            trip_minutes=(
                route["trip_minutes"]
            ),
            pickup_hour=(
                now_nyc.hour
            ),
            day_of_week=(
                now_nyc.weekday()
            ),
            pickup_lat=(
                request.pickup_lat
            ),
            pickup_lon=(
                request.pickup_lon
            ),
            dropoff_lat=(
                request.dropoff_lat
            ),
            dropoff_lon=(
                request.dropoff_lon
            ),
        )
    )

    wait_windows = [
        0,
        30,
        60,
        90,
    ]

    options: list[
        WaitOption
    ] = []

    for wait_minutes in wait_windows:

        future_time = (
            now_nyc
            + timedelta(
                minutes=wait_minutes
            )
        )

        try:
            predictions = (
                predictor.predict_for_time(
                    base_request,
                    pickup_hour=(
                        future_time.hour
                    ),
                    day_of_week=(
                        future_time.weekday()
                    ),
                )
            )

        except RuntimeError as exc:
            raise HTTPException(
                status_code=503,
                detail=str(exc),
            ) from exc

        except ValueError as exc:
            raise HTTPException(
                status_code=422,
                detail=str(exc),
            ) from exc

        uber = next(
            prediction
            for prediction in predictions
            if prediction.provider
            == "uber"
        )

        lyft = next(
            prediction
            for prediction in predictions
            if prediction.provider
            == "lyft"
        )

        if (
            uber.estimated_fare
            <= lyft.estimated_fare
        ):
            lowest_provider = "uber"
            lowest_fare = (
                uber.estimated_fare
            )

        else:
            lowest_provider = "lyft"
            lowest_fare = (
                lyft.estimated_fare
            )

        options.append(
            WaitOption(
                wait_minutes=(
                    wait_minutes
                ),
                uber_fare=(
                    uber.estimated_fare
                ),
                lyft_fare=(
                    lyft.estimated_fare
                ),
                lowest_fare=(
                    lowest_fare
                ),
                lowest_provider=(
                    lowest_provider
                ),
            )
        )

    current = options[0]

    best = min(
        options,
        key=lambda option:
            option.lowest_fare,
    )

    potential_savings = max(
        0.0,
        (
            current.lowest_fare
            - best.lowest_fare
        ),
    )

    if (
        best.wait_minutes == 0
        or potential_savings < 2.00
    ):
        recommendation = (
            "ride_now"
        )

        recommended_wait = 0

        recommended_fare = (
            current.lowest_fare
        )

        potential_savings = 0.0

    else:
        recommendation = "wait"

        recommended_wait = (
            best.wait_minutes
        )

        recommended_fare = (
            best.lowest_fare
        )

    return WaitAndSaveResponse(
        recommendation=(
            recommendation
        ),
        recommended_wait_minutes=(
            recommended_wait
        ),
        current_lowest_fare=round(
            current.lowest_fare,
            2,
        ),
        recommended_fare=round(
            recommended_fare,
            2,
        ),
        potential_savings=round(
            potential_savings,
            2,
        ),
        options=options,
    )


def calculate_nearby_point(
    latitude: float,
    longitude: float,
    distance_meters: float,
    bearing_degrees: float,
) -> tuple[float, float]:

    earth_radius = 6_371_000.0

    bearing = math.radians(
        bearing_degrees
    )

    lat1 = math.radians(
        latitude
    )

    lon1 = math.radians(
        longitude
    )

    angular_distance = (
        distance_meters
        / earth_radius
    )

    lat2 = math.asin(
        math.sin(lat1)
        * math.cos(
            angular_distance
        )
        + math.cos(lat1)
        * math.sin(
            angular_distance
        )
        * math.cos(
            bearing
        )
    )

    lon2 = (
        lon1
        + math.atan2(
            math.sin(bearing)
            * math.sin(
                angular_distance
            )
            * math.cos(lat1),
            math.cos(
                angular_distance
            )
            - math.sin(lat1)
            * math.sin(lat2),
        )
    )

    return (
        math.degrees(lat2),
        math.degrees(lon2),
    )


@app.post(
    "/v1/walk-nearby",
    response_model=WalkNearbyResponse,
)
async def walk_nearby(
    request: AnalyzeTripRequest,
) -> WalkNearbyResponse:

    now_nyc = datetime.now(
        ZoneInfo(
            "America/New_York"
        )
    )

    try:
        current_route = await (
            route_service.get_route(
                pickup_lat=(
                    request.pickup_lat
                ),
                pickup_lon=(
                    request.pickup_lon
                ),
                dropoff_lat=(
                    request.dropoff_lat
                ),
                dropoff_lon=(
                    request.dropoff_lon
                ),
            )
        )

        current_request = (
            FarePredictionRequest(
                trip_miles=(
                    current_route[
                        "trip_miles"
                    ]
                ),
                trip_minutes=(
                    current_route[
                        "trip_minutes"
                    ]
                ),
                pickup_hour=(
                    now_nyc.hour
                ),
                day_of_week=(
                    now_nyc.weekday()
                ),
                pickup_lat=(
                    request.pickup_lat
                ),
                pickup_lon=(
                    request.pickup_lon
                ),
                dropoff_lat=(
                    request.dropoff_lat
                ),
                dropoff_lon=(
                    request.dropoff_lon
                ),
            )
        )

        current_predictions = (
            predictor.predict(
                current_request
            )
        )

    except RuntimeError as exc:
        raise HTTPException(
            status_code=503,
            detail=str(exc),
        ) from exc

    except ValueError as exc:
        raise HTTPException(
            status_code=422,
            detail=str(exc),
        ) from exc

    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=(
                f"Current route analysis failed: "
                f"{exc}"
            ),
        ) from exc

    current_lowest = min(
        prediction.estimated_fare
        for prediction
        in current_predictions
    )

    candidates = [
        ("N", 0.0),
        ("E", 90.0),
        ("S", 180.0),
        ("W", 270.0),
        ("NE", 45.0),
        ("NW", 315.0),
    ]

    options: list[
        WalkNearbyOption
    ] = []

    candidate_distance = 250.0

    for (
        direction,
        bearing,
    ) in candidates:

        (
            candidate_lat,
            candidate_lon,
        ) = calculate_nearby_point(
            latitude=(
                request.pickup_lat
            ),
            longitude=(
                request.pickup_lon
            ),
            distance_meters=(
                candidate_distance
            ),
            bearing_degrees=(
                bearing
            ),
        )

        try:
            walking_route = await (
                route_service
                .get_walking_route(
                    pickup_lat=(
                        request.pickup_lat
                    ),
                    pickup_lon=(
                        request.pickup_lon
                    ),
                    dropoff_lat=(
                        candidate_lat
                    ),
                    dropoff_lon=(
                        candidate_lon
                    ),
                )
            )

            walking_meters = (
                walking_route[
                    "distance_meters"
                ]
            )

            walking_minutes = (
                walking_route[
                    "trip_minutes"
                ]
            )

            # Reject points that require
            # unreasonable detours.
            if (
                walking_meters > 650
                or walking_minutes > 10
            ):
                continue

            driving_route = await (
                route_service.get_route(
                    pickup_lat=(
                        candidate_lat
                    ),
                    pickup_lon=(
                        candidate_lon
                    ),
                    dropoff_lat=(
                        request.dropoff_lat
                    ),
                    dropoff_lon=(
                        request.dropoff_lon
                    ),
                )
            )

            candidate_request = (
                FarePredictionRequest(
                    trip_miles=(
                        driving_route[
                            "trip_miles"
                        ]
                    ),
                    trip_minutes=(
                        driving_route[
                            "trip_minutes"
                        ]
                    ),
                    pickup_hour=(
                        now_nyc.hour
                    ),
                    day_of_week=(
                        now_nyc.weekday()
                    ),
                    pickup_lat=(
                        candidate_lat
                    ),
                    pickup_lon=(
                        candidate_lon
                    ),
                    dropoff_lat=(
                        request.dropoff_lat
                    ),
                    dropoff_lon=(
                        request.dropoff_lon
                    ),
                )
            )

            predictions = (
                predictor.predict(
                    candidate_request
                )
            )

        except Exception:
            # One bad candidate should not
            # fail the entire recommendation.
            continue

        uber = next(
            prediction
            for prediction
            in predictions
            if prediction.provider
            == "uber"
        )

        lyft = next(
            prediction
            for prediction
            in predictions
            if prediction.provider
            == "lyft"
        )

        if (
            uber.estimated_fare
            <= lyft.estimated_fare
        ):
            lowest_provider = (
                "uber"
            )

            lowest_fare = (
                uber.estimated_fare
            )

        else:
            lowest_provider = (
                "lyft"
            )

            lowest_fare = (
                lyft.estimated_fare
            )

        savings = max(
            0.0,
            current_lowest
            - lowest_fare,
        )

        options.append(
            WalkNearbyOption(
                pickup_lat=round(
                    candidate_lat,
                    6,
                ),
                pickup_lon=round(
                    candidate_lon,
                    6,
                ),
                direction=(
                    direction
                ),
                walking_distance_meters=(
                    walking_meters
                ),
                walking_minutes=round(
                    walking_minutes,
                    1,
                ),
                driving_miles=(
                    driving_route[
                        "trip_miles"
                    ]
                ),
                driving_minutes=(
                    driving_route[
                        "trip_minutes"
                    ]
                ),
                uber_fare=(
                    uber.estimated_fare
                ),
                lyft_fare=(
                    lyft.estimated_fare
                ),
                lowest_fare=(
                    lowest_fare
                ),
                lowest_provider=(
                    lowest_provider
                ),
                predicted_savings=round(
                    savings,
                    2,
                ),
            )
        )

    if not options:
        return WalkNearbyResponse(
            recommendation="stay",
            current_lowest_fare=round(
                current_lowest,
                2,
            ),
            recommended_fare=round(
                current_lowest,
                2,
            ),
            potential_savings=0.0,
            best_option=None,
            options=[],
        )

    options.sort(
        key=lambda option:
            option.predicted_savings,
        reverse=True,
    )

    best = options[0]

    # Walking is not worth recommending
    # for tiny changes that may simply
    # reflect normal model error.
    minimum_savings = 2.00

    if (
        best.predicted_savings
        < minimum_savings
    ):
        return WalkNearbyResponse(
            recommendation="stay",
            current_lowest_fare=round(
                current_lowest,
                2,
            ),
            recommended_fare=round(
                current_lowest,
                2,
            ),
            potential_savings=0.0,
            best_option=best,
            options=options,
        )

    return WalkNearbyResponse(
        recommendation="walk",
        current_lowest_fare=round(
            current_lowest,
            2,
        ),
        recommended_fare=round(
            best.lowest_fare,
            2,
        ),
        potential_savings=round(
            best.predicted_savings,
            2,
        ),
        best_option=best,
        options=options,
    )