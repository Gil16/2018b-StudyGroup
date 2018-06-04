package study.group.Groups.Chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import study.group.R;
import study.group.Utilities.User;

public class Chat extends AppCompatActivity {

    private TextView Messages;
    private EditText messageToSend;
    private Button Send;
    private DatabaseReference dataBase;
    private LinearLayoutManager lay;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private ArrayList<UserMessage> messages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //local variables
        final String groupID = getIntent().getExtras().getString("groupID");
        final String groupName = getIntent().getExtras().getString("groupName");

        setTitle(groupName);

        messages = new ArrayList<>();

        //all the messages in the chat group
        //Messages = findViewById(R.id.messages);
        //the message that the current user wants to send
        messageToSend = findViewById(R.id.messageToSend);
        // the send button
        Send = findViewById(R.id.sendBtn);
        dataBase = FirebaseDatabase.getInstance().getReference();

        mMessageAdapter= new MessageListAdapter(this,messages);
        mMessageRecycler = findViewById(R.id.messagesRecycler);
        mMessageRecycler.setAdapter(mMessageAdapter);
        lay = new LinearLayoutManager(this);
        lay.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(lay);


        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //generate a random key for the new message
                String key = dataBase.child("Groups").child(groupID).child("Chat").push().getKey();
                //getting the content of the message to send
                String message = messageToSend.getText().toString();
                //adding the message to the database:
                //fist we enter the username and the the message
                dataBase.child("Groups").child(groupID).child("Chat").child(key).child("user").setValue(Profile.getCurrentProfile().getId());
                dataBase.child("Groups").child(groupID).child("Chat").child(key).child("Message").setValue(message);
                dataBase.child("Groups").child(groupID).child("Chat").child(key).child("TimeStamp").setValue(new Date());
                dataBase.child("Groups").child(groupID).child("Chat").child(key).child("name").setValue(Profile.getCurrentProfile().getName());
                messageToSend.setText("");

            }
        });

        //adding a listener to the group chat
        dataBase.child("Groups").child(groupID).child("Chat").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
                mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount()+1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
                mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount()+1);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //a helper function that creates the TextView that holds all of the group chat
    private void append_chat_conversation(DataSnapshot ds)
    {


        if(ds.getChildrenCount() == 4) {
            //TODO: fix things here

            HashMap<String, Object> data = (HashMap<String, Object>) ds.getValue();
            HashMap<String,Long> timeStamp = (HashMap<String, Long>)data.get("TimeStamp");

            String userID = (String) data.get("user");
            String chatMessage;

            String userName = (String) data.get("name");
            User currentUser = new User(userID,userName,
                    Profile.getCurrentProfile().getProfilePictureUri(30,30));

            long time =timeStamp.get("time");
            chatMessage = (String)data.get("Message");
            UserMessage um = new UserMessage(chatMessage,currentUser,time);
            messages.add(um);
            mMessageAdapter.notifyDataSetChanged();
        }

    }


}
