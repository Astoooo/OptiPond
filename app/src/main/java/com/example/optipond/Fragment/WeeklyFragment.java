package com.example.optipond.Fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.optipond.Adapter.ReadingAdapter;
import com.example.optipond.Model.ReadingModel;
import com.example.optipond.R;
import com.example.optipond.Utils.CalendarUtils;
import com.example.optipond.Utils.DateAndTimeUtils;
import com.example.optipond.Utils.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WeeklyFragment extends Fragment {

    TextView monthDayYearTV;
    AppCompatButton nextWeekBtn, prevWeekBtn;
    ReadingAdapter adapter;
    ArrayList<ReadingModel> list;
    RecyclerView recyclerView;

     com.github.mikephil.charting.charts.LineChart mChart;
     ArrayList<Entry> entry;
     List<String> xValues;
    FrameLayout lineChartData_Layout;
    RelativeLayout noData_Layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weekly, container, false);
        initWidgets(view);
        setUpButtons();
        CalendarUtils.selectedDate = LocalDate.now();


        LocalDate startOfWeek = CalendarUtils.selectedDate.with(DayOfWeek.MONDAY);

        ArrayList<String> weekArr = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        for (int i = 0; i < 7 ; i++){
            LocalDate date = startOfWeek.plusDays(i);
            weekArr.add(i,String.valueOf(date.format(formatter)));
        }

        
        monthDayYearTV.setText(CalendarUtils.monthDayYearFromDateForWeek(CalendarUtils.selectedDate));
        monthDayYearTV.setOnClickListener(v->{showDatePickerDialog();});
        setUpRecyclerview(weekArr);
        setUpLineChart(weekArr);
        return view;
    }

    private void setUpButtons() {
        nextWeekBtn.setOnClickListener(v-> nextWeekAction());
        prevWeekBtn.setOnClickListener(v-> previousWeekAction());
    }


    private void previousWeekAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setUpWeekView();

    }


    private void nextWeekAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setUpWeekView();

    }

    public void setUpRecyclerview(ArrayList<String> weekArr){
        list = new ArrayList<>();
        adapter = new ReadingAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        for (int i = 0; i < 7; i++ ){
            String dateId = weekArr.get(i);
            String date = DateAndTimeUtils.convertToDateWordFormat(dateId);
            FirebaseFirestore.getInstance().collection("data").document(dateId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                String tds = "";
                                String oxygen = "";
                                String phValue = "";
                                String waterPercentage = "";

                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()){

                                     tds = documentSnapshot.getString("tds");
                                     oxygen = documentSnapshot.getString("oxygen");
                                     phValue = documentSnapshot.getString("phValue");
                                     waterPercentage = documentSnapshot.getString("waterPercentage");


                                     list.add(new ReadingModel(waterPercentage,phValue,tds,oxygen, false,date));
                                }
                                else{
                                    list.add(new ReadingModel(waterPercentage,phValue,tds,oxygen, false,date));
                                }

                                if (adapter!= null)
                                    adapter.notifyDataSetChanged();
                                list.sort(new Comparator<ReadingModel>() {
                                    @Override
                                    public int compare(ReadingModel r1, ReadingModel r2) {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
                                        LocalDate date1 = LocalDate.parse(r1.getDate(), formatter);
                                        LocalDate date2 = LocalDate.parse(r2.getDate(), formatter);
                                        return date1.compareTo(date2);
                                    }
                                });
                            }
                            else{
                                Log.d("TAG", "Failed to fetch weekly data");
                            }
                        }
                    });
        }

    }

    private void setUpWeekView(){
        monthDayYearTV.setText(CalendarUtils.monthDayYearFromDateForWeek(CalendarUtils.selectedDate));
        
        ArrayList<String> weekArr = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        LocalDate startOfWeek = CalendarUtils.selectedDate.with(DayOfWeek.MONDAY);
        for (int i = 0; i < 7 ; i++){
            LocalDate date = startOfWeek.plusDays(i);
            weekArr.add(i,String.valueOf(date.format(formatter)));
        }


        setUpRecyclerview(weekArr);
        setUpLineChart(weekArr);

    }

    private void setUpLineChart(ArrayList<String> weekArr) {

        entry = new ArrayList<>();

        xValues = new ArrayList<>();
        mChart.getAxisRight().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(7);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(7);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setGranularity(1);

        for (int i = 0; i < weekArr.size(); i++){
            String date = DateAndTimeUtils.convertToDateWordFormat(weekArr.get(i));
            xValues.add(i, date);
        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(14);
        yAxis.setLabelCount(7);


        float[] phValueArr = new float[7];
        AtomicInteger counter = new AtomicInteger(0);

        LineChart.isExist = false;

        for (int i = 0; i < weekArr.size(); i++) {
            String dateId = weekArr.get(i);

            int finalI = i;
            FirebaseFirestore.getInstance().collection("data").document(dateId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    LineChart.isExist = true;
                                    String phValue = documentSnapshot.getString("phValue");
                                    float phValueFloat = Float.parseFloat(phValue);
                                    phValueArr[finalI] = phValueFloat;
                                } else {
                                    Log.d("TAG", "Document does not exist for date: " + dateId);
                                }
                            } else {
                                Log.d("TAG", "Failed to fetch data for date: " + dateId);
                            }

                            // Increment the counter when this task is complete
                            int completedTasks = counter.incrementAndGet();

                            // Check if all tasks are completed
                            if (completedTasks == weekArr.size()) {

                                setIfDataForLineChartExist();

                                for (int i = 0; i < phValueArr.length; i++){
                                    Log.d("TAG", String.valueOf(phValueArr[i]));
                                }

                                //Input the data to line chart
                                for (int i = 0; i < phValueArr.length; i++){
                                    if (phValueArr[i] != 0)
                                        entry.add(new Entry(i, phValueArr[i]));
                                }

                                LineDataSet set1 = new LineDataSet(entry, "phLevel");
                                set1.setLineWidth(3);
                                set1.setCircleRadius(8);
                                set1.setValueTextSize(10);
                                set1.setCircleHoleRadius(5);
                                if (getContext() != null){
                                    set1.setColor(ContextCompat.getColor(getContext(), R.color.primary));
                                    set1.setCircleColor(ContextCompat.getColor(getContext(), R.color.primary));
                                }

                                LineData data = new LineData(set1);

                                mChart.setData(data);
                                mChart.animateX(1);
                            }
                        }
                    });
        }

    }

    private void setIfDataForLineChartExist() {
        if (LineChart.isExist){
            noData_Layout.setVisibility(View.GONE);
            lineChartData_Layout.setVisibility(View.VISIBLE);
        } else {
            noData_Layout.setVisibility(View.VISIBLE);
            lineChartData_Layout.setVisibility(View.GONE);
        }
    }

    private void initWidgets(View view) {
        monthDayYearTV = view.findViewById(R.id.monthDayYear_Textview);
        nextWeekBtn = view.findViewById(R.id.nextWeekAction_Button);
        prevWeekBtn = view.findViewById(R.id.previousWeekAction_Button);
        recyclerView = view.findViewById(R.id.weekly_Recyclerview);
        mChart = view.findViewById(R.id.lineChart);
        noData_Layout = view.findViewById(R.id.noData_FrameLayout);
        lineChartData_Layout = view.findViewById(R.id.lineChart_FrameLayout);

    }

    private void showDatePickerDialog() {
        // Show a date picker dialog to select a date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        @SuppressLint("SetTextI18n")
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            // Set the selected date to the Textview
            monthDayYearTV.setText(String.format(new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(new Date(year1 - 1900, monthOfYear, dayOfMonth))));

            String dateId = String.format(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(year1 - 1900, monthOfYear, dayOfMonth)));
            CalendarUtils.selectedDate = DateAndTimeUtils.getLocalDate(dateId);
            setUpWeekView();

        }, year, month, day);

        // Display the date picker dialog
        datePickerDialog.show();

    }
}