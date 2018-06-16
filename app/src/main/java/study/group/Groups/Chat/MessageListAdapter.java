package study.group.Groups.Chat;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import study.group.R;
import study.group.Utilities.Group;
import study.group.Utilities.User;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_REQUEST = 3;

    private Context myContext;
    private List<UserMessage> MessageList;

    public MessageListAdapter(Context context, List<UserMessage> messageList) {
        myContext = context;
        MessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return MessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        UserMessage message = (UserMessage) MessageList.get(position);
        if(message.getType().equals("Request"))
        {
            return VIEW_TYPE_REQUEST;
        }
        else
        {
            if (message.getSender().getToken().equals(Profile.getCurrentProfile().getId())) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sender_message, parent, false);
            return new SentMessageHolder(view);
        }
        else
        {
            if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recieved_message, parent, false);
                return new ReceivedMessageHolder(view);
            }
            else
            {
                if (viewType == VIEW_TYPE_REQUEST) {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.request_message, parent, false);
                    return new RequestMessageHolder(view);
                }
            }
        }


        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserMessage message = (UserMessage) MessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_REQUEST:
                ((RequestMessageHolder) holder).bind(message);

        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body_sender);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time_sender);
        }

        void bind(UserMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(DateUtils.formatDateTime(myContext,message.getCreatedAt(),DateUtils.FORMAT_SHOW_TIME));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }

        void bind(UserMessage message) {
            Picasso.with(myContext).load(message.getSender().getProfileUrl()).into(profileImage);

            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(DateUtils.formatDateTime(myContext,message.getCreatedAt(),DateUtils.FORMAT_SHOW_TIME));

            nameText.setText(message.getSender().getName());

            // Insert the profile image from the URL into the ImageView.


        }
    }

    private class RequestMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        Button acceptButton, rejectButton;
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        RequestMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.RequestMessage);
            acceptButton = (Button) itemView.findViewById(R.id.RequestAcceptButton);
            rejectButton = (Button) itemView.findViewById(R.id.RequestRejectButton);
        }

        void bind(final UserMessage message) {
            messageText.setText(message.getSender().getName() + " has requested to join a group!");
            String groupAdmin = message.getAdminID();
            final User user = message.getSender();
            final String groupID = message.getGroupID();

            if(!groupAdmin.equals(Profile.getCurrentProfile().getId()))
            {
                acceptButton.setClickable(false);
                rejectButton.setClickable(false);
            }
            else
            {
                acceptButton.setClickable(true);
                rejectButton.setClickable(true);
            }

            final long[] currentParticipants = {0};
            final long[] maxParticipants = {0};
            database.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        if(ds.child("groupID").getValue().equals(groupID))
                        {
                            currentParticipants[0] = (long) ds.child("currentNumOfPart").getValue();
                            maxParticipants[0] = (long) ds.child("maxNumOfPart").getValue();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //checking ig the group is already full
                    if(currentParticipants[0] == maxParticipants[0])
                    {
                        Toast.makeText(myContext, R.string.request_group_full, Toast.LENGTH_LONG).show();
                        return;
                    }

                    database.child("Users").child(user.getToken()).
                            child("Requests").child(groupID).removeValue();
                    database.child("Users").child(user.getToken()).
                            child("interested").child(groupID).removeValue();
                    database.child("Groups").child(groupID).child("Requests").child(user.getToken()).removeValue();
                    database.child("Groups").child(groupID).child("participants")
                            .child(user.getToken()).setValue(user.getName());



                    database.child("Groups").child(groupID).child("currentNumOfPart").setValue(currentParticipants[0]+1);
                    database.child("Users").child(user.getToken()).child("Joined").child(groupID)
                            .setValue("");
                    deleteRequest(user.getToken(),groupID);
                    notifyDataSetChanged();
                }
            });

            rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.child("Users").child(user.getToken()).child("Requests").child(groupID).removeValue();
                    database.child("Groups").child(groupID).child("Requests").child(user.getToken()).removeValue();
                    deleteRequest(user.getToken(),groupID);
                    notifyDataSetChanged();
                }
            });

        }

        void deleteRequest (final String senderID, final String groupID)
        {
            database.child("Groups").child(groupID).child("Chat").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        if(ds.child("User").getValue().equals(senderID) && ds.child("Type").getValue().equals("Request"))
                        {
                            ds.getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
