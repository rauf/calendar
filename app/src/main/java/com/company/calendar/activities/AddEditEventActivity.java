package com.company.calendar.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.company.calendar.managers.AlarmHelper;
import com.company.calendar.managers.EventManager;
import com.company.calendar.managers.EventSubscriptionManager;
import com.company.calendar.managers.UserManager;
import com.company.calendar.models.AlarmCounter;
import com.company.calendar.models.Event;
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

public class AddEditEventActivity extends AppCompatActivity {

    public static String EDIT_EVENT_MODE = "editMode";

    private ArrayList<User> userList;
    private UserRecyclerViewAdapter userAdapter;
    private RecyclerView userRecyclerView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView dateTextBox;
    private TextView timeTextBox;
    private Button addEventButton;
    private boolean editMode;
    private String eventId;

    private int hour = 0;
    private int minutes = 0;

    private int date = 0;           //day is 1 indexed 1-31
    private int month = 0;          //month is 0 indexed 0-11
    private int year = 0;

    private int alarmId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initialiseViews();
        setUserList();

        Intent recvIntent = getIntent();

        editMode = recvIntent.getBooleanExtra(EDIT_EVENT_MODE, false);
        alarmId = recvIntent.getIntExtra(Event.ALARM_ID_FIELD, 0);

        if (editMode) {
            eventId = recvIntent.getStringExtra(Event.ID_FIELD);
            populateFieldsFromSavedEvent();
        } else {
            setDateTimeVariablesToCurrent();
            dateTextBox.setText(getDateString());
            timeTextBox.setText(getTimeString());
        }

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEventButtonClick();
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

    private void populateFieldsFromSavedEvent() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Event event = snap.getValue(Event.class);

                    if (event.getId().equals(eventId)) {
                        setFieldsFromEvent(event);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFieldsFromEvent(Event event) {
        titleEditText.setText(event.getTitle());
        descriptionEditText.setText(event.getDescription());

        hour = event.getHour();
        minutes = event.getMinute();
        year = event.getYear();
        month = event.getMonth();
        date = event.getDate();

        dateTextBox.setText(getDateString());
        timeTextBox.setText(getTimeString());
    }

    private void handleEventButtonClick() {
        final String title = titleEditText.getText().toString().trim();
        final String description = descriptionEditText.getText().toString().trim();

        if (title.length() == 0) {
            Toast.makeText(AddEditEventActivity.this, "Title is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());    //encoding as firebase doen not supoort '.' in its path
        final DatabaseReference alarmCounter = FirebaseDatabase.getInstance().getReference().child(AlarmCounter.ALARM_COUNTER_FIELD);

        addOrUpdateEvent(title, description, currUser, alarmCounter);
    }

    private void addOrUpdateEvent(final String title, final String description, final String currUser, final DatabaseReference alarmCounter) {
        alarmCounter.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {

                int alarmId = data.getValue(Integer.class);
                alarmCounter.setValue(alarmId + 1);

                Event event = new Event(title, description, currUser, alarmId, year, month, date, hour, minutes);

                if (editMode) {
                    EventManager.deleteEvent(AddEditEventActivity.this, eventId, editMode);
                    AlarmHelper.cancelAlarm(AddEditEventActivity.this, AddEditEventActivity.this.alarmId);
                }
                String key = EventManager.addEventToDb(event);

                Toast.makeText(AddEditEventActivity.this, "Alarm Set", Toast.LENGTH_SHORT).show();

                ArrayList<String> invitedUsersEmail = userAdapter.getSelectedUsers();
                EventSubscriptionManager.addSubscriptionToDb(invitedUsersEmail, key, currUser);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddEditEventActivity.this, "Call to database failed", Toast.LENGTH_SHORT).show();
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
                        userAdapter = new UserRecyclerViewAdapter(userList, editMode, eventId);
                        userRecyclerView.setAdapter(userAdapter);
                        userRecyclerView.setLayoutManager(new LinearLayoutManager(AddEditEventActivity.this));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AddEditEventActivity.this, "Cannot retrieve user lists", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddEditEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                hour = hourOfDay;
                AddEditEventActivity.this.minutes = minute;
                timeTextBox.setText(getTimeString());
            }
        }, hour, minutes, false);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddEditEventActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                AddEditEventActivity.this.year = year;
                AddEditEventActivity.this.month = month;
                AddEditEventActivity.this.date = dayOfMonth;
                dateTextBox.setText(getDateString());
            }
        }, year, month, date);
        datePickerDialog.setTitle("Select Date");
        datePickerDialog.show();
    }

    private void setDateTimeVariablesToCurrent() {
        Calendar rightNow = Calendar.getInstance();
        hour = rightNow.get(Calendar.HOUR_OF_DAY);
        minutes = rightNow.get(Calendar.MINUTE);
        date = rightNow.get(Calendar.DAY_OF_MONTH);
        month = rightNow.get(Calendar.MONTH);
        year = rightNow.get(Calendar.YEAR);
    }

    private String getDateString() {
        return date + " / " + (month + 1) + " / " + year;
    }

    private String getTimeString() {
        return hour + " : " + minutes;
    }

    private void initialiseViews() {
        userRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        dateTextBox = (TextView) findViewById(R.id.dateTextBox);
        timeTextBox = (TextView) findViewById(R.id.timeTextBox);
        addEventButton = (Button) findViewById(R.id.addEvent);
    }
}
