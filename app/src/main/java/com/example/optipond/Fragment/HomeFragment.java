package com.example.optipond.Fragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.optipond.Adapter.ReadingAdapter;
import com.example.optipond.FirebaseHandler.FirebaseHandler;
import com.example.optipond.MainActivity;
import com.example.optipond.Model.ReadingModel;
import com.example.optipond.R;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeFragment extends Fragment {

    RecyclerView recyclerView;

    ReadingAdapter myAdapter;
    ArrayList<ReadingModel> list;
    FirebaseHandler firebaseHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_home, container, false);

         setUpRecyclerview(view);
         setUpCleaningNotification();

        firebaseHandler = new FirebaseHandler();


        //If version is greater than version oreo, notification proceeds

        firebaseHandler.startListeningForNewReadings(new FirebaseHandler.OnNewReadingListener() {
            @Override
            public void onNewReading(String phValue, String waterPercentage, String id) {
                getNotify(phValue, waterPercentage, id);
            }

        });
         return view;
    }

    private void setUpCleaningNotification() {
        FirebaseFirestore.getInstance().collection("cleaning")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                           QuerySnapshot querySnapshot = task.getResult();
                           if (!querySnapshot.isEmpty() && querySnapshot != null){
                               for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                   String endDateId = documentSnapshot.getString("endDateId");
                                   String endPhValue = documentSnapshot.get("endPhValue").toString();
                                   String endWaterLevel = documentSnapshot.get("endWaterPercentage").toString();

                                   endPhValue = MathUtils.roundOff(Double.parseDouble(endPhValue));

                                   checkForCleaningNotification(endDateId, endPhValue, endWaterLevel);
                               }
                           }
                        } else {
                            Log.d("TAG", "Failed to fetch cleaning data " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void checkForCleaningNotification(String endDateId, String endPhValue, String endWaterLevel) {
        LocalDate endLocalDate = DateAndTimeUtils.getLocalDateUsingDateId(endDateId);
        LocalDate now = LocalDate.now();


        if (endLocalDate.isEqual(now)){
            getNotifyUsingCleaningData(endPhValue, endWaterLevel, endDateId);
        }

    }

    public  void getNotifyUsingCleaningData(String endPhValue, String endWaterLevel, String id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("Cleaning new reading",
                    "Cleaning new reading", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100,1000,200,340});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =  getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Context context = getContext();
        if (context != null){
            Intent notificationIntent = new Intent(getContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0,notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "Cleaning new reading");
            builder.setContentTitle("Time to clean");
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            builder.setAutoCancel(true);
            builder.setContentText("Water percentage: " + endWaterLevel + "%" + "\nPH Value: " + endPhValue);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setVibrate(new long[] {100,1000,200,340});
            builder.setContentIntent(pendingIntent);


            NotificationManagerCompat manager = NotificationManagerCompat.from(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.notify(id.hashCode(), builder.build());
        }

    }

    public void getNotify(String phValue, String waterPercentage, String id){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("OptiPond new reading",
                    "OptiPond new reading", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100,1000,200,340});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =  getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Context context = getContext();
        if (context != null){
            Intent notificationIntent = new Intent(getContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0,notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "OptiPond new reading");
            builder.setContentTitle("New reading");
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            builder.setAutoCancel(true);
            builder.setContentText("Water percentage: " + waterPercentage + "%" + "\nPH Value: " + phValue);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setVibrate(new long[] {100,1000,200,340});
            builder.setContentIntent(pendingIntent);


            NotificationManagerCompat manager = NotificationManagerCompat.from(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.notify(id.hashCode(), builder.build());
        }


    }
    private void setUpRecyclerview(View view) {
        recyclerView = view.findViewById(R.id.reading_Recyclerview);
        list = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new ReadingAdapter(getContext(), list);
        recyclerView.setAdapter(myAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Reading");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    list.clear();
                    int num = 0;
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){


                        if (dataSnapshot.hasChild("waterPercentage") && dataSnapshot.hasChild("phValue")){
                            num++;

                            String waterPercentage = dataSnapshot.child("waterPercentage").getValue().toString() + "%";
                            String phValue = dataSnapshot.child("phValue").getValue().toString();
                            String number = Integer.toString(num);
                            String tds = dataSnapshot.child("TDS").getValue().toString();
                            String oxygen = dataSnapshot.child("oxygen").getValue().toString();
                            String date = DateAndTimeUtils.getDateWithWordFormat();
                            String time = DateAndTimeUtils.getTime24HrsFormat();
                            String timeId = DateAndTimeUtils.getTimeId();




                            Log.d("TAG", waterPercentage + phValue);

                            list.add(new ReadingModel(waterPercentage, phValue, tds, oxygen, true, date));
                            setData(waterPercentage,phValue,tds,oxygen,date, DateAndTimeUtils.getDateId(), timeId, time);
                        }
                    }


                    if (myAdapter != null){
                        myAdapter.notifyDataSetChanged();
                    }
                }
                else{
                    Log.d("Database", "Snapshot does not exist");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Database", "Database error " + error.getMessage());
            }
        });
    }

    private void setData(String waterPercentage, String phValue, String tds, String oxygen, String date, String dateId,
                         String timeId, String time) {
        HashMap<String, String> data = new HashMap<>();

        data.put("waterPercentage", waterPercentage);
        data.put("phValue", phValue);
        data.put("tds", tds);
        data.put("oxygen", oxygen);
        data.put("id", dateId);
        data.put("date", date);



        FirebaseFirestore.getInstance().collection("data").document(dateId)
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("TAG", "Data successfully set");
                        }
                        else {
                            Log.d("TAG","Failed to set data");
                        }
                    }
                });


        HashMap<String, String> dailyData = new HashMap<>();

        dailyData.put("waterPercentage", waterPercentage);
        dailyData.put("phValue", phValue);
        dailyData.put("tds", tds);
        dailyData.put("oxygen", oxygen);
        dailyData.put("time", time);
        dailyData.put("timeId", timeId);

        FirebaseFirestore.getInstance().collection("data").document(dateId)
                .collection(dateId).document(timeId)
                .set(dailyData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("TAG", "Daily Data successfully set");
                        }
                        else {
                            Log.d("TAG","Failed to set Daily data");
                        }
                    }
                });
    }
}