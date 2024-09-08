package com.example.optipond.FirebaseHandler;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHandler {


    private DatabaseReference databaseReference;

    public FirebaseHandler() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        // Change "readings" to the name of your node in the database
        databaseReference = firebaseDatabase.getReference("Reading");
    }

    public void startListeningForNewReadings(final OnNewReadingListener listener) {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle new reading
                if (listener != null) {
                    String phValue = dataSnapshot.child("phValue").getValue().toString();
                    String waterPercentage = dataSnapshot.child("waterPercentage").getValue().toString();
                    String id = dataSnapshot.child("id").getValue().toString();
                    listener.onNewReading(phValue, waterPercentage, id);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle changes in readings (if needed)
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Handle removed readings (if needed)
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle moved readings (if needed)
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    // Define an interface to handle new readings
    public interface OnNewReadingListener {
        void onNewReading(String phValue, String waterPercentage, String id);

    }
}

