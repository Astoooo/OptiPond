package com.example.optipond.Adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.optipond.Model.ReadingModel;
import com.example.optipond.R;

import java.util.ArrayList;
@RequiresApi(api = Build.VERSION_CODES.O)
public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.MyViewHolder> {

    Context context;
    ArrayList<ReadingModel> readingModelList;


    public ReadingAdapter(Context context, ArrayList<ReadingModel> readingModelList){
        this.context = context;
        this.readingModelList = readingModelList;
    }
    @NonNull
    @Override
    public ReadingAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reading_list_recyclerview, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ReadingAdapter.MyViewHolder holder, int position) {
        if (readingModelList.get(position).getWaterPercentage().isEmpty()){
            holder.dataContainer.setVisibility(View.GONE);
            holder.noDataContainer.setVisibility(View.VISIBLE);
        } else {
            holder.dataContainer.setVisibility(View.VISIBLE);
            holder.noDataContainer.setVisibility(View.GONE);
        }
        holder.waterLevelPercentage.setText(readingModelList.get(position).getWaterPercentage());
        holder.phLevel.setText(readingModelList.get(position).getPhValue());
        holder.tds.setText((readingModelList.get(position).getTds()));
        holder.oxygen.setText(readingModelList.get(position).getOxygen());
        holder.date.setText(readingModelList.get(position).getIsHome() ? "As of " + readingModelList.get(position).getDate(): readingModelList.get(position).getDate());


    }

    @Override
    public int getItemCount() {
        return readingModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView  waterLevelPercentage, phLevel, oxygen, tds, date;
        LinearLayout noDataContainer, dataContainer;
        public MyViewHolder(@NonNull View itemView) {

            super(itemView);


            waterLevelPercentage = itemView.findViewById(R.id.waterPercentage_Textview);
            phLevel = itemView.findViewById(R.id.phValue_Textview);
            oxygen = itemView.findViewById(R.id.oxygen_Textview);
            tds = itemView.findViewById(R.id.tds_Textview);
            date = itemView.findViewById(R.id.date_TextView);
            noDataContainer = itemView.findViewById(R.id.noDataContainer);
            dataContainer = itemView.findViewById(R.id.dataContainer);


        }
    }
}
