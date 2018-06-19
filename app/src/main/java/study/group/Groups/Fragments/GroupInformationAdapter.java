package study.group.Groups.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import study.group.Groups.Chat.Chat;
import com.facebook.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import study.group.Groups.Admin.GroupAdminActivity;
import study.group.Groups.Participant.GroupActivity;
import study.group.R;
import study.group.Utilities.Group;
import study.group.Utilities.MyDatabaseUtil;

/*
 * GroupInformationAdapter.
 * This adapter contains all the relevant groups to show according to the user movements.
 */
public class GroupInformationAdapter extends RecyclerView.Adapter<GroupInformationAdapter.InfoHolder> {


    private ArrayList<Group> data;
    private int fragment;
    boolean isJoined;
    private Context context;

    public GroupInformationAdapter(ArrayList<Group> data, int fragment) {
        this.fragment = fragment;
        this.data = data;
    }

    @NonNull
    @Override
    public InfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_information_item, parent, false);
        context = view.getContext();
        return new InfoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final InfoHolder holder, int key) {
        Group group = data.get(key);
        holder.subject.setText(group.getSubject());
        holder.idAndName.setText(group.getId());
        holder.date.setText(group.getDate());
        String sb = String.valueOf(group.getCurrentNumOfPart()) + "/" + String.valueOf(group.getmaxNumOfPart());
        holder.participants.setText(sb);

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        StorageReference fileReference = mStorageRef.child(group.getGroupID());
        fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()) {
                    String downloadURL = task.getResult().toString();
                    Picasso.with(context).load(downloadURL).into(holder.groupPhoto);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void filterList(ArrayList<Group> filteredList) {
        this.data = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerItem;
        TextView subject;
        TextView idAndName;
        TextView date;
        TextView participants;
        Button interestedButton;
        ImageView groupPhoto;

        InfoHolder(final View itemView) {
            super(itemView);
            recyclerItem = itemView.findViewById(fragment);
            subject = itemView.findViewById(R.id.groupSubjectInfo);
            idAndName = itemView.findViewById(R.id.groupIdAndName);
            date = itemView.findViewById(R.id.groupMeetingDate);
            participants = itemView.findViewById(R.id.groupNumberOfParticipantsInfo);
            groupPhoto = itemView.findViewById(R.id.groupPhotoInfo);


            MyDatabaseUtil my = new MyDatabaseUtil();
            MyDatabaseUtil.getDatabase();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference();

            /*
             * On clickListener, if the user is the admin of the clicked group, the Admin activity will be started, otherwise,
             * The participant activity will be started (not an admin).
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Group group = data.get(getAdapterPosition());
                    myRef.child("Users").child(Profile.getCurrentProfile().getId()).child("Joined").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            isJoined = false;
                            participants.setText(group.getCurrentNumOfPart()+ "/" + group.getmaxNumOfPart());
                            for (DataSnapshot ds : dataSnapshot.getChildren())
                            {
                                if(ds.getKey().equals(group.getGroupID()))
                                {
                                    isJoined = true;
                                }
                            }

                            if (group.getAdminID().equals(Profile.getCurrentProfile().getId())) {
                                Intent adminGroup = new Intent(v.getContext(), Chat.class);
                                adminGroup.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Boolean nextAdminFlag = false;
                                if(group.getParticipants().size() > 1) {
                                    for(Map.Entry<String, String> user : group.getParticipants().entrySet()) {
                                        if(!user.getKey().equals(group.getAdminID())) {
                                            adminGroup.putExtra("nextAdmin", user.getKey());
                                            nextAdminFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if(!nextAdminFlag) {
                                    adminGroup.putExtra("nextAdmin", group.getAdminID());
                                }
                                adminGroup.putExtra("groupSubject", group.getSubject());
                                adminGroup.putExtra("groupDate", group.getDate());
                                adminGroup.putExtra("groupTime", group.getTime());
                                adminGroup.putExtra("groupID", group.getGroupID());
                                adminGroup.putExtra("groupLocation", group.getLocation());
                                adminGroup.putExtra("numOfParticipants", group.getmaxNumOfPart());
                                adminGroup.putExtra("groupCurrentParticipants", group.getCurrentNumOfPart());
                                adminGroup.putExtra("adminID", group.getAdminID());
                                adminGroup.putExtra("groupName", group.getName());

                                v.getContext().startActivity(adminGroup);
                            } else {
                                Intent userGroup;
                                if(isJoined)
                                {
                                    userGroup = new Intent(v.getContext(), Chat.class);
                                }
                                else
                                {
                                    userGroup = new Intent(v.getContext(), GroupActivity.class);
                                }
                                userGroup.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                userGroup.putExtra("groupSubject", group.getSubject());
                                userGroup.putExtra("groupDate", group.getDate());
                                userGroup.putExtra("groupTime", group.getTime());
                                userGroup.putExtra("groupID", group.getGroupID());
                                userGroup.putExtra("groupLocation", group.getLocation());
                                userGroup.putExtra("numOfParticipants", group.getCurrentNumOfPart());
                                userGroup.putExtra("adminID", group.getAdminID());
                                userGroup.putExtra("groupName", group.getName());
                                v.getContext().startActivity(userGroup);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            });
        }
    }




}
