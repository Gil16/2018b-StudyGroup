package com.example.studygroup;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_admin);

        final Context currentContext = this;

        String subject = getIntent().getExtras().getString("groupSubject");
        String date = getIntent().getExtras().getString("groupDate");
        String location = getIntent().getExtras().getString("groupLocation");
        final String groupID = getIntent().getExtras().getString("groupID");
        final Integer numOfParticipants = getIntent().getExtras().getInt("numOfParticipants");
        final String adminID = getIntent().getExtras().getString("adminID");

        EditText subjectET = findViewById(R.id.subjectAdminEdit);
        EditText dateET = findViewById(R.id.dateAdminEdit);
        EditText locationET = findViewById(R.id.locationAdminEdit);
        TextView currentNumOfParticipants = findViewById(R.id.participantsAdmin);


        subjectET.setText(subject);
        dateET.setText(date);
        locationET.setText(location);
        currentNumOfParticipants.setText(numOfParticipants.toString() + " Participants");

        final ArrayList<String> participants = new ArrayList<>();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("Groups").child(groupID).child("Requests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RecyclerView requestsRecycler = findViewById(R.id.requestAdminRecycler);
                requestsRecycler.setLayoutManager(new LinearLayoutManager(currentContext));
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    participants.add(d.getValue().toString());
                }
                adminRequestsAdapter requestsAdapter = new adminRequestsAdapter(participants);
                requestsRecycler.setAdapter(requestsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.child("Groups").child(groupID).child("Participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RecyclerView participantsRecycler = findViewById(R.id.recyclerPaticipantsGroup);
                participantsRecycler.setLayoutManager(new LinearLayoutManager(currentContext));
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    participants.add(d.getValue().toString());
                }

                adminParticipantsAdapter participantAdapter= new adminParticipantsAdapter(participants);
                participantsRecycler.setAdapter(participantAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
