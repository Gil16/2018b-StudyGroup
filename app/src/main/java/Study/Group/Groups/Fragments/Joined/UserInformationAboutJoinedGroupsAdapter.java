package Study.Group.Groups.Fragments.Joined;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import Study.Group.R;
import Study.Group.Utilities.Group;
import Study.Group.Utilities.MyDatabaseUtil;

public class UserInformationAboutJoinedGroupsAdapter extends RecyclerView.Adapter<UserInformationAboutJoinedGroupsAdapter.InfoHolder> {
    private ArrayList<Group> data;

    public UserInformationAboutJoinedGroupsAdapter(ArrayList<Group> newJoined) {
        this.data = newJoined;
    }


    @NonNull
    @Override
    public InfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_information_item, parent, false);
        return new UserInformationAboutJoinedGroupsAdapter.InfoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoHolder holder, int position) {
        Group group = data.get(position);
        holder.subject.setText(group.getSubject());
        holder.idAndName.setText(group.getId());
        holder.date.setText(group.getDate());
        String sb = String.valueOf(group.getCurrentNumOfPart()) + "/" + String.valueOf(group.getmaxNumOfPart());
        holder.participants.setText(sb);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerItem;
        TextView subject;
        TextView idAndName;
        TextView date;
        TextView participants;
  //      Button interestedButton;

        InfoHolder(final View itemView) {
            super(itemView);
            recyclerItem = itemView.findViewById(R.id.requestsRecyclerView);
            subject = itemView.findViewById(R.id.groupSubjectInfo);
            idAndName = itemView.findViewById(R.id.groupIdAndName);
            date = itemView.findViewById(R.id.groupMeetingDate);
            participants = itemView.findViewById(R.id.groupNumberOfParticipantsInfo);

            MyDatabaseUtil my = new MyDatabaseUtil();
            MyDatabaseUtil.getDatabase();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(itemView.getContext());
                    alertDialog.setTitle(subject.getText());
                    alertDialog.setPositiveButton(R.string.leave_group, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            myRef.child("Users")
                                    .child(Profile.getCurrentProfile().getId())
                                    .child("Joined").child((data.get(getAdapterPosition()).getGroupID())).removeValue();
                            myRef.child("Groups").child((data.get(getAdapterPosition()).getGroupID()))
                                    .child("participants").child(Profile.getCurrentProfile().getId()).removeValue();
                            myRef.child("Groups").child((data.get(getAdapterPosition()).getGroupID()))
                                    .child("currentNumOfPart").setValue((data.get(getAdapterPosition()).getCurrentNumOfPart()-1));
//                                (data.get(getAdapterPosition()).getCurrentNumOfPart()-1)
                            data.remove(getAdapterPosition());
                            notifyDataSetChanged();
                        }
                    }).show();
                }
            });

        }
    }
}
