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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DailyFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<ReadingModel> list;
    ReadingAdapter adapter;

    AppCompatButton nextDayBtn, previousDayBtn;
    TextView monthDayYearTV;

    private LineChart mChart;
    private ArrayList<Entry> entry;
    private List<String> xValues;
    FrameLayout lineChartData_Layout;
    RelativeLayout noData_Layout;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily, container, false);
        initWidgets(view);

        setUpDayButton();

        CalendarUtils.selectedDate = LocalDate.now();
        String dateId = DateAndTimeUtils.parseDateToDateId(CalendarUtils.selectedDate);
        monthDayYearTV.setText(CalendarUtils.monthDayYearFromDate(CalendarUtils.selectedDate));
        monthDayYearTV.setOnClickListener(v->{showDatePickerDialog();});
        setUpRecyclerview(dateId);
        setUpLineChart(dateId);
        return view;
    }

    private void setUpLineChart(String dateId) {

        entry = new ArrayList<>();

        entry = new ArrayList<>();
        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.setDragEnabled(true); // Enable dragging
        mChart.setScaleEnabled(true); // Enable scaling
        mChart.setScaleXEnabled(true); // Enable scaling on the X-axis only
        mChart.setScaleYEnabled(false); // Disable scaling on the Y-axis (optional)
        mChart.setVisibleXRangeMaximum(7); // Show only 7 entries at a time, adjust as needed
        mChart.setVisibleXRangeMinimum(1);
        // Enable pinch zoom and double-tap zoom
        mChart.setPinchZoom(true);
        mChart.setDoubleTapToZoomEnabled(true);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(7);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(23);
        xAxis.setGranularity(1);
        xValues = Arrays.asList("12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM"
                , "8AM", "9AM", "10AM", "11AM", "12NN", "1PM", "2PM"
                , "3PM", "4PM", "5PM", "6PM", "7PM", "9PM", "10PM"
                , "11PM", "12MN");
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(14);
        yAxis.setLabelCount(7);

        CollectionReference cr = FirebaseFirestore.getInstance().collection("data").document(dateId).collection(dateId);

        Query query =cr.orderBy("time", Query.Direction.ASCENDING);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot.isEmpty()){
                                noData_Layout.setVisibility(View.VISIBLE);
                                lineChartData_Layout.setVisibility(View.GONE);
                            } else {
                                noData_Layout.setVisibility(View.GONE);
                                lineChartData_Layout.setVisibility(View.VISIBLE);
                                int [] timeArray = new int[24];
                                int [] timeLengthArray = new int[24];
                                int [] timeAverageArray = new int[24];

                                for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                    if (documentSnapshot.exists()){
                                        String time = documentSnapshot.getString("time");
                                        String phValue = documentSnapshot.getString("phValue");
                                        double phValueInt = Double.parseDouble(phValue);

                                        String TIME = DateAndTimeUtils.getTimeForLineChart(time);
                                        int timeInt = Integer.parseInt(TIME);

                                        // Updating arrays
                                        timeArray[timeInt] += phValueInt;
                                        timeLengthArray[timeInt] = timeLengthArray[timeInt] + 1;
                                    }
                                }

                                //Compute the average glucose level for each time
                                for (int i = 0; i < 24; i++){

                                    if(timeLengthArray[i] != 0)
                                        timeAverageArray[i] = timeArray[i] / timeLengthArray[i];

                                }

                                //Input the data to line chart
                                for (int i = 0; i <24; i++){
                                    if(timeAverageArray[i] != 0)
                                        entry.add(new Entry(i,timeAverageArray[i]));

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
                        else{
                            Log.d("TAG", "Failed to fetch daily to for linechart");
                        }
                    }
                });
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
            dateId = DateAndTimeUtils.parseDateToDateId(CalendarUtils.selectedDate);
            setUpRecyclerview(dateId);

        }, year, month, day);

        // Display the date picker dialog
        datePickerDialog.show();

    }
    private void setUpDayView(){
        monthDayYearTV.setText(CalendarUtils.monthDayYearFromDate(CalendarUtils.selectedDate));
        String dateId = DateAndTimeUtils.parseDateToDateId(CalendarUtils.selectedDate);
        setUpRecyclerview(dateId);
        setUpLineChart(dateId);

    }

    private void setUpDayButton() {
        nextDayBtn.setOnClickListener(v->{nextDayAction();});
        previousDayBtn.setOnClickListener(v->{previousDayAction();});
    }

    private void previousDayAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusDays(1);
        setUpDayView();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void nextDayAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusDays(1);
        setUpDayView();

    }

    private void setUpRecyclerview(String dateId) {

        list = new ArrayList<>();
        adapter = new ReadingAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                            Log.d("TAG", "Failed to fetch daily data");
                        }
                    }
                });

        Collections.sort(list, new Comparator<ReadingModel>() {
            @Override
            public int compare(ReadingModel r1, ReadingModel r2) {
                // Example: Sort by date
                LocalDate date1 = LocalDate.parse(r1.getDate());
                LocalDate date2 = LocalDate.parse(r2.getDate());
                return date1.compareTo(date2);
            }
        });
    }

    private void initWidgets(View view) {
        recyclerView = view.findViewById(R.id.daily_Recyclerview);
        mChart = view.findViewById(R.id.lineChart);
        previousDayBtn = view.findViewById(R.id.previousDayAction_Button);
        nextDayBtn = view.findViewById(R.id.nextDayAction_Button);
        monthDayYearTV = view.findViewById(R.id.monthDayYear_Textview);
        noData_Layout = view.findViewById(R.id.noData_FrameLayout);
        lineChartData_Layout = view.findViewById(R.id.lineChart_FrameLayout);
    }


}