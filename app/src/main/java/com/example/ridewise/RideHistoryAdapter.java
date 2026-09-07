package com.example.ridewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridewise.models.RideHistory;
import com.example.ridewise.models.RideProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideHistoryAdapter
        extends RecyclerView.Adapter<RideHistoryAdapter.ViewHolder> {

    private final List<RideHistory> rides;
    private final SimpleDateFormat dateFormat;

    public RideHistoryAdapter(
            List<RideHistory> rides
    ) {

        this.rides = rides;

        this.dateFormat =
                new SimpleDateFormat(
                        "MMM dd, yyyy",
                        Locale.US
                );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_ride_history,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        RideHistory ride =
                rides.get(position);

        String date =
                dateFormat.format(
                        new Date(
                                ride.getDate()
                        )
                );

        holder.dateTextView.setText(date);

        holder.providerTextView.setText(
                formatProvider(
                        ride.getProvider()
                )
        );

        String pickup =
                safeAddress(
                        ride.getPickupAddress(),
                        "Pickup"
                );

        String dropoff =
                safeAddress(
                        ride.getDropoffAddress(),
                        "Destination"
                );

        holder.detailsTextView.setText(
                pickup
                        + " → "
                        + dropoff
        );

        holder.priceTextView.setText(
                String.format(
                        Locale.US,
                        "$%.2f",
                        ride.getActualPrice()
                )
        );

        if (ride.getSavings() > 0.0) {

            holder.savingsTextView.setText(
                    String.format(
                            Locale.US,
                            "$%.2f",
                            ride.getSavings()
                    )
            );

            holder.savingsContainer.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.savingsContainer.setVisibility(
                    View.GONE
            );
        }
    }

    private String formatProvider(
            RideProvider provider
    ) {

        if (provider == null) {
            return "Provider";
        }

        switch (provider) {

            case UBER:
                return "Uber";

            case LYFT:
                return "Lyft";

            default:
                return provider
                        .toString()
                        .toLowerCase(Locale.US);
        }
    }

    private String safeAddress(
            String address,
            String fallback
    ) {

        if (address == null
                || address.trim().isEmpty()) {

            return fallback;
        }

        return address.trim();
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView providerTextView;
        TextView detailsTextView;
        TextView priceTextView;
        TextView savingsTextView;

        LinearLayout savingsContainer;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            dateTextView =
                    itemView.findViewById(
                            R.id.rideDateTextView
                    );

            providerTextView =
                    itemView.findViewById(
                            R.id.rideProviderTextView
                    );

            detailsTextView =
                    itemView.findViewById(
                            R.id.rideDetailsTextView
                    );

            priceTextView =
                    itemView.findViewById(
                            R.id.ridePriceTextView
                    );

            savingsTextView =
                    itemView.findViewById(
                            R.id.rideSavingsTextView
                    );

            savingsContainer =
                    itemView.findViewById(
                            R.id.savingsContainer
                    );
        }
    }
}