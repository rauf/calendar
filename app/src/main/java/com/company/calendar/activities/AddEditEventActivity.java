package com.company.calendar.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.company.calendar.managers.DateTimeManager;
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
import java.util.Date;
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
    private TextView startDateTextBox;
    private TextView startTimeTextBox;
    private TextView endDateTextBox;
    private TextView endTimeTextBox;
    private Button addEventButton;
    private boolean editMode;
    private String eventId;

    private Date startTime;
    private Date endTime;

    private int startAlarmId;
    private int endAlarmId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initialiseViews();
        setUserList();

        Intent recvIntent = getIntent();

        editMode = recvIntent.getBooleanExtra(EDIT_EVENT_MODE, false);
        startAlarmId = recvIntent.getIntExtra(Event.START_ALARM_ID_FIELD, 0);
        endAlarmId = recvIntent.getIntExtra(Event.END_ALARM_ID_FIELD, 0);

        if (editMode) {
            eventId = recvIntent.getStringExtra(Event.ID_FIELD);
            populateFieldsFromSavedEvent();
        } else {
            setDateTimeVariablesToCurrent();
            startDateTextBox.setText(DateTimeManager.getDateString(startTime));
            startTimeTextBox.setText(DateTimeManager.getTimeString(startTime));

            endDateTextBox.setText(DateTimeManager.getDateString(endTime));
            endTimeTextBox.setText(DateTimeManager.getTimeString(endTime));
        }

        addEventButton.setOnClickListener(getAddEventButtonOnClickListener());
        startDateTextBox.setOnClickListener(getDateTextBoxOnClickListener(true));
        startTimeTextBox.setOnClickListener(getTimeTextBoxOnClickListener(true));

        endDateTextBox.setOnClickListener(getDateTextBoxOnClickListener(false));
        endTimeTextBox.setOnClickListener(getTimeTextBoxOnClickListener(false));
    }

    @NonNull
    private View.OnClickListener getAddEventButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEventButtonClick();
            }
        };
    }

    @NonNull
    private View.OnClickListener getTimeTextBoxOnClickListener(final boolean start) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(start);
            }
        };
    }

    @NonNull
    private View.OnClickListener getDateTextBoxOnClickListener(final boolean start) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(start);
            }
        };
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
                Toast.makeText(AddEditEventActivity.this, "Database call failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFieldsFromEvent(Event event) {
        titleEditText.setText(event.getTitle());
        descriptionEditText.setText(event.getDescription());

        startTime = DateTimeManager.gmttoLocalDate(event.getStartTime());
        endTime = DateTimeManager.gmttoLocalDate(event.getEndTime());

        startDateTextBox.setText(DateTimeManager.getDateString(startTime));
        startTimeTextBox.setText(DateTimeManager.getTimeString(startTime));

        endTimeTextBox.setText(DateTimeManager.getTimeString(endTime));
        endDateTextBox.setText(DateTimeManager.getDateString(endTime));
    }

    private void handleEventButtonClick() {
        final String title = titleEditText.getText().toString().trim();
        final String description = descriptionEditText.getText().toString().trim();

        if (title.length() == 0) {
            Toast.makeText(AddEditEventActivity.this, "Title is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime.after(endTime)) {
            Toast.makeText(AddEditEventActivity.this, "Start Time is after End Time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime.equals(endTime)) {
            Toast.makeText(AddEditEventActivity.this, "Start Time is equal to End Time", Toast.LENGTH_SHORT).show();
            return;
        }

        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());    //encoding as firebase doen not supoort '.' in its path
        final DatabaseReference alarmCounter = FirebaseDatabase.getInstance().getReference()
                .child(AlarmCounter.ALARM_COUNTER_FIELD);
        addOrUpdateEvent(title, description, currUser, alarmCounter);
    }

    private void addOrUpdateEvent(final String title, final String description, final String currUser, final DatabaseReference alarmCounter) {
        alarmCounter.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {

                int startAlarmId = data.getValue(Integer.class);
                int endAlarmId = startAlarmId + 1;

                alarmCounter.setValue(endAlarmId + 1);

                Event event = new Event("",title, description, currUser, startAlarmId, endAlarmId, startTime, endTime);

                if (editMode) {
                    EventManager.deleteEvent(AddEditEventActivity.this, eventId, editMode);
                    AlarmHelper.cancelAlarm(AddEditEventActivity.this, AddEditEventActivity.this.startAlarmId);
                    AlarmHelper.cancelAlarm(AddEditEventActivity.this, AddEditEventActivity.this.endAlarmId);
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


    private void showTimePicker(final boolean forStart) {

        int currHour = DateTimeManager.getHour(endTime);
        int currMin = DateTimeManager.getMinute(endTime);

        if (forStart) {
            currHour = DateTimeManager.getHour(startTime);
            currMin = DateTimeManager.getMinute(startTime);
        }
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddEditEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                if (forStart) {
                    startTime = DateTimeManager.setTime(startTime, hourOfDay, minute);
                    startTimeTextBox.setText(DateTimeManager.getTimeString(startTime));
                } else {
                    endTime = DateTimeManager.setTime(endTime, hourOfDay, minute);
                    endTimeTextBox.setText(DateTimeManager.getTimeString(endTime));
                }
            }
        }, currHour, currMin, false);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void showDatePicker(final boolean forStart) {

        int currYear = DateTimeManager.getYear(endTime);
        int currMonth = DateTimeManager.getMonth(endTime);
        int currDay = DateTimeManager.getDay(endTime);

        if (forStart) {
            currYear = DateTimeManager.getYear(startTime);
            currMonth = DateTimeManager.getMonth(startTime);
            currDay = DateTimeManager.getDay(startTime);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddEditEventActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                if (forStart) {
                    startTime = DateTimeManager.setDate(startTime, year, month, dayOfMonth);
                    startDateTextBox.setText(DateTimeManager.getDateString(startTime));
                } else {
                    endTime = DateTimeManager.setDate(endTime, year, month, dayOfMonth);
                    endDateTextBox.setText(DateTimeManager.getDateString(endTime));
                }
            }
        }, currYear, currMonth, currDay);
        datePickerDialog.setTitle("Select Date");
        datePickerDialog.show();
    }

    private void setDateTimeVariablesToCurrent() {
        Calendar rightNow = Calendar.getInstance();
        startTime = rightNow.getTime();
        endTime = rightNow.getTime();
    }

    private void initialiseViews() {
        userRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        startDateTextBox = (TextView) findViewById(R.id.startDateTextBox);
        startTimeTextBox = (TextView) findViewById(R.id.startTimeTextBox);
        addEventButton = (Button) findViewById(R.id.addEvent);
        endDateTextBox = (TextView) findViewById(R.id.endDateTextBox);
        endTimeTextBox = (TextView) findViewById(R.id.endTimeTextBox);
    }
}
