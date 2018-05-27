package com.example.studygroup;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupActivity extends AppCompatActivity {
    static boolean isExist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);


        final Context currentContext = this;

        String subject = getIntent().getExtras().getString("groupSubject");
        String date = getIntent().getExtras().getString("groupDate");
        String location = getIntent().getExtras().getString("groupLocation");
        final String groupID = getIntent().getExtras().getString("groupID");
        final Integer numOfParticipants = getIntent().getExtras().getInt("numOfParticipants");
        final String adminID = getIntent().getExtras().getString("adminID");
        final String groupName = getIntent().getExtras().getString("groupName");

        setTitle(subject);
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final String userID = Profile.getCurrentProfile().getId();
        final String userName = Profile.getCurrentProfile().getName();
        final Button joinRequest = (Button)  findViewById(R.id.Request);

        database.child("Users").child(userID).child("Requests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isExist = false;
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    if(d.getKey().equals(groupID))
                    {
                        isExist = true;
                    }
                }
                if(isExist == true) {
                    joinRequest.setText("Cancel Request");
                }
                else
                {
                    joinRequest.setText("Request to join");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TextView subjectTV = (TextView)findViewById(R.id.SubjectInGroupContent);
        TextView dateTV = (TextView)findViewById(R.id.DateInGroupContent);
        TextView locationTV = (TextView)findViewById(R.id.LocationInGroupContent);
        TextView currentNumOfParticipants = (TextView)findViewById(R.id.groupParticipants);

        joinRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.child("Users").child(userID).child("Requests").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        isExist = false;
                        for (DataSnapshot d : dataSnapshot.getChildren())
                        {
                            if(d.getKey().equals(groupID))
                            {
                                isExist = true;
                            }
                        }
                        if(isExist == true)
                        {
                            joinRequest.setText("Request to join");
                            database.child("Users").child(userID).child("Requests").child(groupID).removeValue();
                            database.child("Groups").child(groupID).child("Requests").child(userID).removeValue();
                        }
                        else
                        {
                            joinRequest.setText("Cancel Request");
                            database.child("Users").child(userID).child("Requests").child(groupID).setValue(adminID);
                            database.child("Groups").child(groupID).child("Requests").child(userID).setValue(userName);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        dateTV.setText(date);
        subjectTV.setText(subject);
        locationTV.setText(location);
        currentNumOfParticipants.setText(numOfParticipants.toString() + " Participants");

        final ArrayList<String> participants = new ArrayList<>();

        database.child("Groups").child(groupID).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RecyclerView participantsRecycler = (RecyclerView)findViewById(R.id.recyclerPaticipantsGroup);
                participantsRecycler.setLayoutManager(new LinearLayoutManager(currentContext));
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    participants.add(d.getValue().toString());
                }
                groupParticipantsAdapter participantsAdapter = new groupParticipantsAdapter(participants);
                participantsRecycler.setAdapter(participantsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
