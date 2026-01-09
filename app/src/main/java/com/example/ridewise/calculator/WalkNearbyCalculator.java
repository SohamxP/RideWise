package com.example.ridewise.calculator;

import com.example.ridewise.models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WalkNearbyCalculator {

    private Random random;

    public WalkNearbyCalculator() {
        this.random = new Random();
    }

    public List<WalkNearbyZone> findCheaperZones(TripRequest tripRequest, int maxWalkMeters) {
        List<WalkNearbyZone> zones = new ArrayList<>();

        // Generate 3 nearby zones at different distances/directions
        String[] directions = {"NE", "E", "N"};
        int[] distances = {200, 300, 250};

        for (int i = 0; i < directions.length; i++) {
            String direction = directions[i];
            int distance = distances[i];

            if (distance <= maxWalkMeters) {
                double[] newLocation = calculateNewLocation(
                        tripRequest.getPickupLat(),
                        tripRequest.getPickupLng(),
                        distance,
                        getDirectionAngle(direction)
                );

                // Estimate savings (2-6% typically)
                double savingsPercent = 2.0 + random.nextDouble() * 4.0;

                zones.add(new WalkNearbyZone(
                        newLocation[0],
                        newLocation[1],
                        distance,
                        direction,
                        savingsPercent,
                        0.65
                ));
            }
        }

        return zones;
    }

    public List<WalkNearbyZone> findCheaperZones(TripRequest tripRequest) {
        return findCheaperZones(tripRequest, 500);
    }

    private double[] calculateNewLocation(double lat, double lng, double distanceMeters, double bearingDegrees) {
        double earthRadius = 6371000.0; // meters
        double bearingRadians = Math.toRadians(bearingDegrees);

        double latRadians = Math.toRadians(lat);
        double lngRadians = Math.toRadians(lng);

        double newLatRadians = Math.asin(
                Math.sin(latRadians) * Math.cos(distanceMeters / earthRadius) +
                        Math.cos(latRadians) * Math.sin(distanceMeters / earthRadius) * Math.cos(bearingRadians)
        );

        double newLngRadians = lngRadians + Math.atan2(
                Math.sin(bearingRadians) * Math.sin(distanceMeters / earthRadius) * Math.cos(latRadians),
                Math.cos(distanceMeters / earthRadius) - Math.sin(latRadians) * Math.sin(newLatRadians)
        );

        return new double[] {
                Math.toDegrees(newLatRadians),
                Math.toDegrees(newLngRadians)
        };
    }

    private double getDirectionAngle(String direction) {
        switch (direction) {
            case "N": return 0.0;
            case "NE": return 45.0;
            case "E": return 90.0;
            case "SE": return 135.0;
            case "S": return 180.0;
            case "SW": return 225.0;
            case "W": return 270.0;
            case "NW": return 315.0;
            default: return 0.0;
        }
    }
}