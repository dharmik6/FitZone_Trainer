package com.example.fitzonetrainer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PendingBookingAdapter extends RecyclerView.Adapter<PendingBookingAdapter.ViewHolder> {
    private List<BookingItemList> pendingBookingItems;

    private Context context;
    public PendingBookingAdapter(Context context, List<BookingItemList> pendingBookingItems) {
        this.context = context;
        this.pendingBookingItems = pendingBookingItems;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_appointments_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingItemList pendingBookingItem = pendingBookingItems.get(position);
        holder.nameTextView.setText(pendingBookingItem.getName());
        holder.emailTextView.setText(pendingBookingItem.getEmail());
        holder.statusTextView.setText(pendingBookingItem.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    BookingItemList item = pendingBookingItems.get(position);

                    // Create an intent to start the MembersProfile activity
                    Intent intent = new Intent(context, PendingBookingDetail.class);
                    // Pass data to the intent
                    intent.putExtra("email", item.getEmail());
                    intent.putExtra("name", item.getName());
                    intent.putExtra("status", item.getStatus());
                    intent.putExtra("id", item.getId());
                    Log.d("id adpter" , item.getId());
                    // Start the activity
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingBookingItems.size();
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
