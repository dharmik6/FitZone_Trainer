package com.example.fitzonetrainer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AcceptedBookingAdapter extends RecyclerView.Adapter<AcceptedBookingAdapter.ViewHolder> {
    private List<BookingItemList> acceptedBookingItems;
    private Context context;

    public AcceptedBookingAdapter(Context context, List<BookingItemList> acceptedBookingItems) {
        this.context = context;
        this.acceptedBookingItems = acceptedBookingItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_appointments_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingItemList acceptedBookingItem = acceptedBookingItems.get(position);
        holder.nameTextView.setText(acceptedBookingItem.getName());
        holder.emailTextView.setText(acceptedBookingItem.getEmail());
        holder.statusTextView.setText(acceptedBookingItem.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    BookingItemList item = acceptedBookingItems.get(position);

                    // Create an intent to start the AcceptedBookingDetail activity
                    Intent intent = new Intent(context, AcceptedBookingDetail.class);
                    // Pass data to the intent
                    intent.putExtra("email", item.getEmail());
                    intent.putExtra("name", item.getName());
                    intent.putExtra("status", item.getStatus());
                    intent.putExtra("id", item.getId());
                    Log.d("id adapter", item.getId());
                    // Start the activity
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return acceptedBookingItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView statusTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            emailTextView = itemView.findViewById(R.id.email);
            statusTextView = itemView.findViewById(R.id.status);
        }
    }
}
