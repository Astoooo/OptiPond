package com.example.optipond.Fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.optipond.Adapter.CleaningAdapter;
import com.example.optipond.Model.CleaningModel;
import com.example.optipond.Model.ReadingModel;
import com.example.optipond.R;
import com.example.optipond.Utils.CalendarUtils;
import com.example.optipond.Utils.DateAndTimeUtils;
import com.example.optipond.Utils.MathUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CleaningFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<CleaningModel> list;
    CleaningAdapter adapter;
    AppCompatButton addCleaningDateBtn;
    Dialog dateResultDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_cleaning, container, false);
        initWidgets(view);
        setUpRecyclerView();
        setUpButtons();
        return  view;
    }

    private void setUpButtons() {
        addCleaningDateBtn.setOnClickListener(v->{showDatePickerDialog();});
    }

    public void setUpRecyclerView() {
        list = new ArrayList<>();
        adapter = new CleaningAdapter(list, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseFirestore.getInstance().collection("cleaning")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            QuerySnapshot querySnapshot = task.getResult();

                            if (!querySnapshot.isEmpty() && querySnapshot != null){
                                for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                    String startDate = documentSnapshot.getString("startDate");
                                    String startWaterLevel = documentSnapshot.get("startWaterPercentage").toString();
                                    String startPhValue = documentSnapshot.get("startPhValue").toString();
                                    String endDate = documentSnapshot.getString("endDate");
                                    String endWaterLevel = documentSnapshot.get("endWaterPercentage").toString();
                                    String endPhValue = documentSnapshot.get("endPhValue").toString();
                                    String docId = documentSnapshot.getString("docId");


                                    startPhValue = MathUtils.roundOff(Double.parseDouble(startPhValue));
                                    endPhValue = MathUtils.roundOff(Double.parseDouble(endPhValue));

                                    list.add(new CleaningModel(startDate,endDate, startWaterLevel,endWaterLevel,startPhValue,endPhValue, docId));
                                }

                                if (adapter != null)
                                    adapter.notifyDataSetChanged();

                                list.sort(new Comparator<CleaningModel>() {
                                    @Override
                                    public int compare(CleaningModel r1, CleaningModel r2) {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
                                        LocalDate date1 = LocalDate.parse(r1.getStartDate(), formatter);
                                        LocalDate date2 = LocalDate.parse(r2.getStartDate(), formatter);
                                        return date1.compareTo(date2);
                                    }
                                });


                            }
                        } else {
                            Log.d("TAG", "Fetching cleaning failed: " + task.getException().getMessage());
                        }
                    }
                });
    }



    private void initWidgets(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        addCleaningDateBtn = view.findViewById(R.id.addCleaningDate_Button);
    }

    public void showDateResult (){
        dateResultDialog = new Dialog(getContext());
        dateResultDialog.setContentView(R.layout.date_picked_dialog);
        dateResultDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateResultDialog.setCancelable(false);
        dateResultDialog.show();

        ImageView editBtn = dateResultDialog.findViewById(R.id.edit_ImageView);
        TextView startDateTV = dateResultDialog.findViewById(R.id.startDate_TextView);
        AppCompatButton submitButton = dateResultDialog.findViewById(R.id.submit_Button);
        AppCompatButton discardButton = dateResultDialog.findViewById(R.id.discard_Button);

        editBtn.setOnClickListener(v->{
            showDatePickerDialog();
            dateResultDialog.dismiss();});
        
        discardButton.setOnClickListener(v->{dateResultDialog.dismiss();});

        String dateId = DateAndTimeUtils.parseDateToDateId(CalendarUtils.selectedDate);
        String startDate = DateAndTimeUtils.convertToDateWordFormat(dateId);

        startDateTV.setText(startDate);

        submitButton.setOnClickListener(v->{setUpData();});

    }

    private void setUpData() {

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Reading");

        db.child("-nmL-12312mn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("waterPercentage") && dataSnapshot.hasChild("phValue")){


                        String waterPercentageString = dataSnapshot.child("waterPercentage").getValue().toString();
                        String phValueString = dataSnapshot.child("phValue").getValue().toString();

                        int waterPercentage = Integer.parseInt(waterPercentageString);
                        float phValue = Float.parseFloat(phValueString);

                        Log.d("TAG", "WaterLevel: " + waterPercentage);
                        Log.d("TAG", "PhValue: " + phValue);


                        saveData(waterPercentage,phValue);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Failed to get Data: " + error.getMessage());
            }
        });
    }

    private void saveData(int waterPercentage, float phValue) {

        float endPhValue = (float) (phValue + 0.2);
        int endWaterPercentage = waterPercentage - 10;


        //Get date today
        LocalDate dateToday = LocalDate.now();
        String todayDateId = DateAndTimeUtils.parseDateToDateId(dateToday);
        String dateSubmitted = DateAndTimeUtils.convertToDateWordFormat(todayDateId);

        String startDateId = DateAndTimeUtils.parseDateToDateId(CalendarUtils.selectedDate);
        String START_DATE = DateAndTimeUtils.convertToDateWordFormat(startDateId);

        LocalDate aWeekAfterSelectedDateLocalDate = CalendarUtils.selectedDate.plusWeeks(1);
        String aWeekAfterSelectedDateId = DateAndTimeUtils.parseDateToDateId(aWeekAfterSelectedDateLocalDate);
        String END_DATE = DateAndTimeUtils.convertToDateWordFormat(aWeekAfterSelectedDateId);

        String charSet = "abcdefghijklmnopqrstuvwxyz" +
                "1234567890" +
                "_-";
        String randomStr = "";
        Random rand = new Random();
        int length = 15;

        for(int i  = 0; i < length; i++){
            randomStr +=
                    charSet.charAt(rand.nextInt(charSet.length()));
        }

        String docId = todayDateId + "_" + randomStr;

        HashMap<String, Object> map = new HashMap<>();
        map.put("startDate", START_DATE);
        map.put("startWaterPercentage", waterPercentage);
        map.put("startPhValue", phValue);
        map.put("endDate", END_DATE);
        map.put("endWaterPercentage", endWaterPercentage);
        map.put("endPhValue", endPhValue);
        map.put("dateSubmitted", dateSubmitted);
        map.put("docId", docId);
        map.put("endDateId", aWeekAfterSelectedDateId);


        FirebaseFirestore.getInstance().collection("cleaning")
                .document(docId)
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getContext(), "Successfully save", Toast.LENGTH_LONG).show();
                            dateResultDialog.dismiss();
                            setUpRecyclerView();
                        } else {
                            Toast.makeText(getContext(), "Failed to save", Toast.LENGTH_LONG).show();
                            dateResultDialog.dismiss();
                            Log.d("TAG", "Failed to save: " +  task.getException().getMessage());
                        }
                    }
                });


    }

    private void showDatePickerDialog() {
        // Show a date picker dialog to select a date
        final Calendar c = Calendar.getInstance();

        if (CalendarUtils.selectedDate != null) {
            // Set the custom date as the initial date in the DatePicker
            c.set(CalendarUtils.selectedDate.getYear(), CalendarUtils.selectedDate.getMonthValue() - 1, CalendarUtils.selectedDate.getDayOfMonth());
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        @SuppressLint("SetTextI18n")
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {

            String dateId = String.format(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(year1 - 1900, monthOfYear, dayOfMonth)));
            CalendarUtils.selectedDate = DateAndTimeUtils.getLocalDate(dateId);
            dateId = DateAndTimeUtils.parseDateToDateId(CalendarUtils.selectedDate);

            showDateResult();
        }, year, month, day);

        // Display the date picker dialog
        datePickerDialog.show();

    }
}