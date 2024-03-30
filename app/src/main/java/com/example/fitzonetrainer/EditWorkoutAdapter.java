package com.example.fitzonetrainer;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EditWorkoutAdapter extends RecyclerView.Adapter<EditWorkoutAdapter.ViewHolder> {
    private List<WorExercisesItemList> worExercisesItemLists;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String wid; // Add this variable to hold the wid value

    public EditWorkoutAdapter(EditWorkout context, List<WorExercisesItemList> worExercisesItemLists, String wid) {
        this.worExercisesItemLists = worExercisesItemLists;
        this.context = context;
        this.wid = wid;
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
        holder.exenamee.setText(item.getName());
        holder.exebody.setText(item.getBody());

        // Load image into ImageView using Glide library
        Glide.with(context)
                .load(item.getImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.exeimage);

        // OnClickListener for the delete button
        holder.add_exe_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                WorExercisesItemList itemToDelete = worExercisesItemLists.get(position);

                // Get the reference to the document
                db.collection("workout_plans")
                        .document(wid)
                        .update("exename", FieldValue.arrayRemove(itemToDelete.getName()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                // Remove the item from the list
                                worExercisesItemLists.remove(position);
                                // Notify adapter about data change
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, worExercisesItemLists.size());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                                // Handle failure
                            }
                        });
            }
        });
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
        public TextView exenamee;
        public TextView exebody;
        public ImageView exeimage;
        public Button add_exe_delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            exenamee = itemView.findViewById(R.id.add_exe_name);
            exebody = itemView.findViewById(R.id.add_exe_focus);
            exeimage = itemView.findViewById(R.id.add_exe_image);
            add_exe_delete = itemView.findViewById(R.id.add_exe_delete);
        }
    }
}