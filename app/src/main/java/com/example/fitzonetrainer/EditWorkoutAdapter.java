package com.example.fitzonetrainer;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class EditWorkoutAdapter extends RecyclerView.Adapter<EditWorkoutAdapter.ViewHolder> {
    private List<WorExercisesItemList> worExercisesItemLists;
    private Context context;

    public EditWorkoutAdapter(EditWorkout context, List<WorExercisesItemList> worExercisesItemLists) {
        this.worExercisesItemLists = worExercisesItemLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_name_delete_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorExercisesItemList item = worExercisesItemLists.get(position);
        holder.exename.setText(item.getName());
        holder.exebody.setText(item.getBody());

        // Load image into ImageView using Glide library
        Glide.with(context)
                .load(item.getImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.exeimage);

        // OnClickListener for the delete button
//        holder.add_exe_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position = holder.getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    WorExercisesItemList item = worExercisesItemLists.get(position);
//                    // Remove the item from the list
//                    worExercisesItemLists.remove(position);
//                    // Update Firestore document to remove the exercise ID from the array
//                    db.collection("workout_plans")
//                            .document(wid)
//                            .update("exename", FieldValue.arrayRemove(item.getId()))
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Log.d(TAG, "Exercise ID removed from the array");
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.e(TAG, "Error removing exercise ID from the array", e);
//                                    // If removal fails, add the item back to the list to maintain consistency
//                                    worExercisesItemLists.add(position, item);
//                                    notifyItemInserted(position);
//                                }
//                            });
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        if (worExercisesItemLists != null) {
            return worExercisesItemLists.size();
        } else {
            return 0; // or handle null case appropriately
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView exename;
        public TextView exebody;
        public ImageView exeimage;
        public Button add_exe_delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            exename = itemView.findViewById(R.id.add_exe_name);
            exebody = itemView.findViewById(R.id.add_exe_focus);
            exeimage = itemView.findViewById(R.id.add_exe_image);
            add_exe_delete = itemView.findViewById(R.id.add_exe_delete);
        }
    }
}
