package com.company.calendar.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.company.calendar.R;
import com.company.calendar.models.User;

import java.util.ArrayList;

/**
 * Created by abdul on 18-Jun-17.
 */

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.UserItemViewHolder> {

    private ArrayList<User> userList;
    private SparseBooleanArray selectedItems;

    public UserRecyclerViewAdapter(ArrayList<User> userList) {
        this.userList = userList;
        selectedItems = new SparseBooleanArray();
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

        holder.checkBox.setText(singleUser.getName() + "  ( " + singleUser.getEmail() + " ) ");

        if(selectedItems.get(position, false)) {
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

    static class UserItemViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox;

        public UserItemViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.addUser);
        }
    }

    public ArrayList<String> getSelectedUsers() {

        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < selectedItems.size(); i++) {
            list.add(userList.get(selectedItems.keyAt(i)).getEmail());
        }
        return list;
    }

    public int getSelectedUsersCount() {
        return selectedItems.size();
    }

    public void toggleSelection(int position) {

        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        }
        else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }
}
