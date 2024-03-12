package com.example.fitzonetrainer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.fitzonetrainer.CertificateList;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {
    private List<CertificateList> certificateLists;
    private Context context;

    public CertificateAdapter(Context context, List<CertificateList> certificateLists){
        this.certificateLists = certificateLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.achievements_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CertificateList certificate = certificateLists.get(position);
        holder.certificateName.setText(certificate.getName());

        // Load image into ImageView using Glide library
        Glide.with(context)
                .load(certificate.getImageUrl()) // Assuming getImageUrl() returns the URL of the image
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache image to disk
                .into(holder.certificateImage); // Load image into ImageView

        // Set OnClickListener for the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CertificateList item = certificateLists.get(position);

                    // Create an intent to start the CertificateDetails activity
                    Intent intent = new Intent(context, Achievements.class);
                    // Pass data to the intent
                    intent.putExtra("imageUrl", item.getImageUrl());
                    intent.putExtra("name", item.getName());
                    intent.putExtra("description", item.getDescription());

                    // Start the activity
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return certificateLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView certificateName;
        public ImageView certificateImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            certificateName = itemView.findViewById(R.id.certificate_item_name);
            certificateImage = itemView.findViewById(R.id.certificate_item_image);
        }
    }
}
