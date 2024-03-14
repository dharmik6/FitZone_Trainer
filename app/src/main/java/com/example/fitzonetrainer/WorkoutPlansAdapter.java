package com.example.fitzonetrainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class WorkoutPlansAdapter extends RecyclerView.Adapter<WorkoutPlansAdapter.ViewHolder> {
    private List<WorkoutPlansItemList> exercisesItemLists;
    Context context;

    public WorkoutPlansAdapter(Context context, List<WorkoutPlansItemList> exercisesItemLists){
        this.exercisesItemLists = exercisesItemLists;
        this.context=context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tra = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_plans_list_item, parent, false);
        return new ViewHolder(tra);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutPlansItemList member = exercisesItemLists.get(position);
        holder.exename.setText(member.getName());
        holder.exebody.setText(member.getGoal());

        // Check if the context is not null before loading the image
        if (context != null) {
            // Load image into CircleImageView using Glide library
            Glide.with(context)
                    .load(member.getImage()) // Assuming getImage() returns the URL of the image
                    .apply(RequestOptions.circleCropTransform()) // Apply circle crop transformation for CircleImageView
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache image to disk
                    .into(holder.exeimage); // Load image into CircleImageView
        }

        // Get the context from the parent view
        final Context context = holder.itemView.getContext();
        // Set OnClickListener for the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    WorkoutPlansItemList item = exercisesItemLists.get(position);

                    // Create an intent to start the MembersProfile activity
                    Intent intent = new Intent(context, WorkoutPlans.class);
                    // Pass data to the intent
                    intent.putExtra("image", item.getImage());
                    intent.putExtra("name", item.getName());
                    intent.putExtra("body", item.getGoal());

                    // Start the activity
                    context.startActivity(intent);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return exercisesItemLists.size();
    }

    public void filterList(List<WorkoutPlansItemList> filteredList) {
        exercisesItemLists = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView exename;
        public TextView exebody;
        public ImageView exeimage;

        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            exename = itemView.findViewById(R.id.plan_sho_name);
            exebody = itemView.findViewById(R.id.plan_sho_exercises);
            exeimage = itemView.findViewById(R.id.plan_sho_image);
        }
    }
}
