package com.example.optipond.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.optipond.Fragment.CleaningFragment;
import com.example.optipond.Model.CleaningModel;
import com.example.optipond.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
@RequiresApi(api = Build.VERSION_CODES.O)
public class CleaningAdapter extends RecyclerView.Adapter<CleaningAdapter.MyViewHolder> {
    ArrayList<CleaningModel> list;
    Context context;
    Dialog deleteDialog;

    public CleaningAdapter(ArrayList<CleaningModel> list, Context context){
        this.list = list;
        this.context = context;
    }
    @NonNull
    @Override
    public CleaningAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cleaning_date_list_layout, parent, false);
        return new CleaningAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CleaningAdapter.MyViewHolder holder, int position) {
        holder.startDate.setText(list.get(position).getStartDate());
        holder.endDate.setText(list.get(position).getEndDate());
        holder.startWaterLevel.setText(list.get(position).getStartWaterLevel());
        holder.endWaterLevel.setText(list.get(position).getEndWaterLevel());
        holder.startPhValue.setText(list.get(position).getStartPhValue());
        holder.endPhValue.setText(list.get(position).getEndPhValue());
        
        String docId = list.get(position).getDocId();
        holder.deleteBtn.setOnClickListener(v->{
            showDeleteDialog(docId);
        });
    }

    private void showDeleteDialog(String docId) {
        deleteDialog = new Dialog(context);
        deleteDialog.setContentView(R.layout.delete_dialog);
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(false);
        deleteDialog.show();

        AppCompatButton cancelBtn = deleteDialog.findViewById(R.id.cancel_Button);
        AppCompatButton deleteBtn = deleteDialog.findViewById(R.id.delete_Button);

        cancelBtn.setOnClickListener(v->{deleteDialog.dismiss();});
        deleteBtn.setOnClickListener(v->{deleteData(docId);});
    }

    private void deleteData(String docId) {
        FirebaseFirestore.getInstance().collection("cleaning")
                .document(docId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(context, "Deleted Successfully, Please reload the page", Toast.LENGTH_LONG).show();
                            deleteDialog.dismiss();

                        } else {
                            Toast.makeText(context, "Failed to delete, Please try again later", Toast.LENGTH_LONG).show();
                            Log.d("TAG", "Failed to delete " + docId + ": " + task.getException().getMessage());
                            deleteDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView startDate,
        endDate,
        startWaterLevel,
        endWaterLevel,
        startPhValue,
        endPhValue;

        ImageView deleteBtn;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            startDate = itemView.findViewById(R.id.startDate_TextView);
            endDate = itemView.findViewById(R.id.endDate_TextView);
            startWaterLevel = itemView.findViewById(R.id.waterPercentageFromStartDate_Textview);
            endWaterLevel = itemView.findViewById(R.id.waterPercentageFromEndDate_Textview);
            startPhValue = itemView.findViewById(R.id.phValueFromStartDate_Textview);
            endPhValue = itemView.findViewById(R.id.phValueFromEndDate_Textview);
            deleteBtn = itemView.findViewById(R.id.delete_ImageView);
        }
    }
}
