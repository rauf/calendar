package com.company.calendar.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.company.calendar.R;
import com.company.calendar.adapters.UserRecyclerViewAdapter;
import com.company.calendar.managers.EventManager;
import com.company.calendar.managers.EventSubscriptionManager;
import com.company.calendar.managers.UserManager;
import com.company.calendar.models.AlarmCounter;
import com.company.calendar.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;


/**
 * Created by abdul on 17-Jun-17.
 */

public class AddEventActivity extends AppCompatActivity {

    private Calendar rightNow;
    private ArrayList<User> userList;
    private UserRecyclerViewAdapter userAdapter;
    private RecyclerView userRecyclerView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView dateTextBox;
    private TextView timeTextBox;
    private Button addEventButton;

    private int hour = 0;
    private int minutes = 0;

    private int date = 0;
    private int month = 0;
    private int year = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        intitialiseViews();
        setUserList();

        rightNow = Calendar.getInstance();

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEventClick();
            }
        });

        dateTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        timeTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
    }

    private void handleAddEventClick() {
        final String title = titleEditText.getText().toString().trim();
        final String description = descriptionEditText.getText().toString().trim();

        if (title.length() == 0) {
            Toast.makeText(AddEventActivity.this, "Title is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());    //encoding as firebase doen not supoort '.' in its path
        final DatabaseReference alarmCounter = FirebaseDatabase.getInstance().getReference().child(AlarmCounter.ALARM_COUNTER_FIELD);

        alarmCounter.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {

                int alarmId = data.getValue(Integer.class);
                alarmCounter.setValue(alarmId + 1);

                String key = EventManager.addEventToDb(title, description, currUser, alarmId);

                //AlarmHelper.setAlarm(AddEventActivity.this, title, eventId, alarmId, year, month, date, hour, minutes);
                Toast.makeText(AddEventActivity.this, "Alarm Set", Toast.LENGTH_SHORT).show();

                ArrayList<String> invitedUsersEmail = userAdapter.getSelectedUsers();
                EventSubscriptionManager.addSubscriptionToDb(invitedUsersEmail, key, currUser);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddEventActivity.this, "Call to database failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void setUserList() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userList = UserManager.getAllUsersFromDb((Map<String, Object>) dataSnapshot.getValue());

                        for (int i = 0; i < userList.size(); i++) {
                            String s = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                            if (s.equals(userList.get(i).getEmail())) {
                                userList.remove(i);             //you cannot invite yourself
                            }
                        }
                        userAdapter = new UserRecyclerViewAdapter(userList);
                        userRecyclerView.setAdapter(userAdapter);
                        userRecyclerView.setLayoutManager(new LinearLayoutManager(AddEventActivity.this));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AddEventActivity.this, "Cannot retrieve user lists", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void showTimePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
        builder.setView(R.layout.dialog_time_picker)
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AddEventActivity.this, "TODO", Toast.LENGTH_LONG).show();
                    }
                })
                .create()
                .show();
    }

    private void intitialiseViews() {
        userRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        dateTextBox = (TextView) findViewById(R.id.dateTextBox);
        timeTextBox = (TextView) findViewById(R.id.timeTextBox);
        addEventButton = (Button) findViewById(R.id.addEvent);
    }

    private void showDatePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
        builder.setView(R.layout.dialog_date_picker)
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AddEventActivity.this, "TODO", Toast.LENGTH_LONG).show();
                    }
                })
                .create()
                .show();
    }

    private void setDateTimeVariablesToCurrent() {
        hour = rightNow.get(Calendar.HOUR_OF_DAY);
        minutes = rightNow.get(Calendar.MINUTE);
        date = rightNow.get(Calendar.DAY_OF_MONTH);
        month = rightNow.get(Calendar.MONTH);
        year = rightNow.get(Calendar.YEAR);
    }

    private void setDateTimeVariables(DatePicker date, TimePicker time) {
        //hour = time.get();
        //minutes = time.getMinute();
    }

    private void displayDateTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Select Date and Time")
                .setView(R.layout.dialog_date_picker)
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }

}
