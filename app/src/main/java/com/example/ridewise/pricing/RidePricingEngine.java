package com.example.ridewise.pricing;

import com.example.ridewise.models.*;
import java.util.Calendar;
import java.util.Random;

public class RidePricingEngine {

    // Base pricing - updated to be more realistic
    private static final double UBER_BASE_RATE = 2.50;
    private static final double UBER_PER_MILE = 1.15;
    private static final double UBER_PER_MINUTE = 0.25;
    private static final double UBER_MIN_FARE = 7.00;
    private static final double UBER_BOOKING_FEE = 2.00;

    private static final double LYFT_BASE_RATE = 2.00;
    private static final double LYFT_PER_MILE = 1.08;
    private static final double LYFT_PER_MINUTE = 0.22;
    private static final double LYFT_MIN_FARE = 6.50;
    private static final double LYFT_SERVICE_FEE = 1.75;

    private Random random;

    public RidePricingEngine() {
        this.random = new Random();
    }

    public RideEstimate estimateRide(TripRequest request, RideProvider provider) {
        double distance = calculateDistance(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng()
        );

        double timeMinutes = estimateTime(distance);
        double surgeMultiplier = calculateSurge();

        double basePrice = 0;
        double fees = 0;
        double minFare = 0;

        switch (provider) {
            case UBER:
                basePrice = UBER_BASE_RATE + (distance * UBER_PER_MILE) + (timeMinutes * UBER_PER_MINUTE);
                fees = UBER_BOOKING_FEE;
                minFare = UBER_MIN_FARE;
                break;
            case LYFT:
                basePrice = LYFT_BASE_RATE + (distance * LYFT_PER_MILE) + (timeMinutes * LYFT_PER_MINUTE);
                fees = LYFT_SERVICE_FEE;
                minFare = LYFT_MIN_FARE;
                // Lyft tends to be slightly cheaper
                basePrice *= 0.95;
                break;
            case TAXI:
                basePrice = 2.50 + (distance * 2.50) + (timeMinutes * 0.50);
                fees = 0;
                minFare = 8.00;
                break;
        }

        // Apply minimum fare
        basePrice = Math.max(basePrice, minFare);

        // Add fees
        double priceBeforeSurge = basePrice + fees;

        // Apply surge
        double finalPrice = priceBeforeSurge * surgeMultiplier;

        // Add ±10% variance for price range
        double variance = finalPrice * 0.10;

        int eta = calculateETA(surgeMultiplier);

        return new RideEstimate(
                provider,
                "Standard",
                finalPrice - variance,
                finalPrice + variance,
                eta,
                surgeMultiplier,
                "USD"
        );
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.8; // miles
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private double estimateTime(double distanceMiles) {
        // Variable speed based on distance
        double avgSpeed;
        if (distanceMiles < 5) {
            avgSpeed = 20; // City traffic
        } else if (distanceMiles < 15) {
            avgSpeed = 30; // Mixed traffic
        } else {
            avgSpeed = 45; // Highway speeds
        }
        return (distanceMiles / avgSpeed) * 60.0;
    }

    private double calculateSurge() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        double baseSurge = 1.0;

        // Morning rush: 7-9 AM
        if (hour >= 7 && hour <= 9) {
            baseSurge = 1.2 + random.nextDouble() * 0.5; // 1.2 - 1.7x
        }
        // Evening rush: 5-8 PM
        else if (hour >= 17 && hour <= 20) {
            baseSurge = 1.3 + random.nextDouble() * 0.7; // 1.3 - 2.0x
        }
        // Late night: 10 PM - 2 AM on weekends
        else if ((dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) &&
                (hour >= 22 || hour <= 2)) {
            baseSurge = 1.5 + random.nextDouble() * 1.0; // 1.5 - 2.5x
        }
        // Normal times
        else {
            baseSurge = 1.0 + random.nextDouble() * 0.2; // 1.0 - 1.2x
        }

        return baseSurge;
    }

    private int calculateETA(double surgeMultiplier) {
        // Higher surge = fewer available drivers = longer ETA
        int baseETA = 3 + random.nextInt(5); // 3-7 minutes

        if (surgeMultiplier > 2.0) {
            baseETA += random.nextInt(5); // Add 0-4 minutes
        } else if (surgeMultiplier > 1.5) {
            baseETA += random.nextInt(3); // Add 0-2 minutes
        }

        return baseETA;
    }
}