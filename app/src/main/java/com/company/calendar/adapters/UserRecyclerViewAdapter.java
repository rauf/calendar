package com.company.calendar.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.company.calendar.R;
import com.company.calendar.models.EventSubscription;
import com.company.calendar.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by abdul on 18-Jun-17.
 */

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.UserItemViewHolder> {

    private ArrayList<User> userList;

    private SparseBooleanArray selectedUsers;

    public UserRecyclerViewAdapter(ArrayList<User> userList, boolean editMode, final String eventId) {
        this.userList = userList;
        this.selectedUsers = new SparseBooleanArray();

        if (editMode) {
            DatabaseReference events = FirebaseDatabase.getInstance().getReference().child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);

            for (int i = 0; i < userList.size(); ++i) {
                final User user = userList.get(i);
                final int finalI = i;
                preselectUsers(eventId, events, user, finalI);
            }
        }
    }

    private void preselectUsers(final String eventId, DatabaseReference events, final User user, final int finalI) {
        events.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    EventSubscription sub = snap.getValue(EventSubscription.class);

                    if (user.getEmail().equals(sub.getUserEmail()) &&
                            sub.getEventId().equals(eventId)) {
                        toggleSelection(finalI);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public UserItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.listitem_add_user, parent, false);
        return new UserItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserItemViewHolder holder, final int position) {
        final User singleUser = userList.get(position);

        holder.checkBox.setText(singleUser.getName() + "  ( " + User.decodeString(singleUser.getEmail()) + " ) ");

        if (selectedUsers.get(position, false)) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserItemViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;

        public UserItemViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.addUser);
        }
    }

    public ArrayList<String> getSelectedUsers() {

        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < selectedUsers.size(); i++) {
            list.add(userList.get(selectedUsers.keyAt(i)).getEmail());
        }
        return list;
    }

    public int getSelectedUsersCount() {
        return selectedUsers.size();
    }

    public void toggleSelection(int position) {

        if (selectedUsers.get(position, false)) {
            selectedUsers.delete(position);
        } else {
            selectedUsers.put(position, true);
        }
        notifyItemChanged(position);
    }
}
