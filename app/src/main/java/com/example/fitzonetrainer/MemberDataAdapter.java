package com.example.fitzonetrainer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberDataAdapter extends RecyclerView.Adapter<MemberDataAdapter.ViewHolder> {
    private List<MemberList> memberList;
    Context context;

    public MemberDataAdapter(Context context, List<MemberList> memberList){
        this.memberList = memberList;
        this.context=context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberList member = memberList.get(position);
        holder.textDataName.setText(member.getName());
        holder.textDataEmail.setText(member.getEmail());

        // Check if the context is not null before loading the image
        if (context != null) {
            // Load image into CircleImageView using Glide library
            Glide.with(context)
                    .load(member.getImage()) // Assuming getImage() returns the URL of the image
                    .apply(RequestOptions.circleCropTransform()) // Apply circle crop transformation for CircleImageView
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache image to disk
                    .into(holder.textDataImage); // Load image into CircleImageView
        }

        // Get the context from the parent view
        final Context context = holder.itemView.getContext();
        // Set OnClickListener for the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MemberList item = memberList.get(position);

                    // Create an intent to start the MembersProfile activity
                    Intent intent = new Intent(context, MemberProfile.class);
                    // Pass data to the intent
                    intent.putExtra("image", item.getImage());
                    intent.putExtra("uid", item.getId());
                    intent.putExtra("name", item.getName());
                    intent.putExtra("username", item.getUsername());
                    intent.putExtra("email", item.getEmail());

                    // Start the activity
                    context.startActivity(intent);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return memberList.size();
    }


    public void filterList1(List<MemberList> filteredList) {
        memberList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textDataName;
        public CircleImageView textDataImage;
        public TextView textDataEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDataName = itemView.findViewById(R.id.name_data_member);
            textDataEmail = itemView.findViewById(R.id.email_data_member);
            textDataImage = itemView.findViewById(R.id.image_data_member);
        }
    }
}


