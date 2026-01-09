package com.example.ridewise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ridewise.models.RideHistory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.ViewHolder> {

    private List<RideHistory> rides;
    private SimpleDateFormat dateFormat;

    public RideHistoryAdapter(List<RideHistory> rides) {
        this.rides = rides;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideHistory ride = rides.get(position);

        // Date
        String date = dateFormat.format(new Date(ride.getDate()));
        holder.dateTextView.setText(date);

        // Provider
        holder.providerTextView.setText(ride.getProvider().toString());

        // Route details
        String details = ride.getPickupAddress() + " → " + ride.getDropoffAddress();
        holder.detailsTextView.setText(details);

        // Price
        holder.priceTextView.setText(String.format("Price: $%.2f", ride.getActualPrice()));

        // Savings
        if (ride.getSavings() > 0) {
            holder.savingsTextView.setText(String.format("Saved: $%.2f", ride.getSavings()));
            holder.savingsTextView.setVisibility(View.VISIBLE);
        } else {
            holder.savingsTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView providerTextView;
        TextView detailsTextView;
        TextView priceTextView;
        TextView savingsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.rideDateTextView);
            providerTextView = itemView.findViewById(R.id.rideProviderTextView);
            detailsTextView = itemView.findViewById(R.id.rideDetailsTextView);
            priceTextView = itemView.findViewById(R.id.ridePriceTextView);
            savingsTextView = itemView.findViewById(R.id.rideSavingsTextView);
        }
    }
}