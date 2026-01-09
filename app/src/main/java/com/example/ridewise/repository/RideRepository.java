package com.example.ridewise.repository;

import com.example.ridewise.models.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class RideRepository {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public interface SaveCallback {
        void onSuccess(String rideId);
        void onError(String error);
    }

    public interface LoadCallback {
        void onSuccess(List<RideHistory> rides);
        void onError(String error);
    }

    public RideRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void saveRideHistory(RideHistory ride, SaveCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            callback.onError("Not authenticated");
            return;
        }

        ride.setUserId(userId);
        String rideId = db.collection("rides").document().getId();
        ride.setId(rideId);

        db.collection("rides").document(rideId)
                .set(ride)
                .addOnSuccessListener(aVoid -> callback.onSuccess(rideId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getRideHistory(int limit, LoadCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            callback.onError("Not authenticated");
            return;
        }

        db.collection("rides")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<RideHistory> rides = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        RideHistory ride = doc.toObject(RideHistory.class);
                        rides.add(ride);
                    });
                    callback.onSuccess(rides);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getTotalSavings(LoadCallback callback) {
        getRideHistory(1000, new LoadCallback() {
            @Override
            public void onSuccess(List<RideHistory> rides) {
                callback.onSuccess(rides);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}