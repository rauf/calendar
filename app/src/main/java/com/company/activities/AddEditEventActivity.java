package com.company.activities;

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

import com.company.adapters.UserRecyclerViewAdapter;
import com.company.R;
import com.company.managers.DateTimeManager;
import com.company.managers.EventManager;
import com.company.managers.EventSubscriptionManager;
import com.company.managers.UserManager;
import com.company.models.AlarmCounter;
import com.company.models.Event;
import com.company.models.EventSubscription;
import com.company.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;


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

        db.orderByKey()
                .equalTo(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
        addOrUpdateEvent(title, description, currUser);
    }

    private void addOrUpdateEvent(final String title, final String description, final String currUser) {

        final Event event = new Event("", title, description, currUser, startAlarmId, endAlarmId, startTime, endTime);

        event.setStartTime(DateTimeManager.toGMT(event.getStartTime()));        //convert to gmt
        event.setEndTime(DateTimeManager.toGMT(event.getEndTime()));


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(AddEditEventActivity.this, "Startting thread", Toast.LENGTH_SHORT).show();
                performOperation(currUser, event);
            }
        });
        t.start();
    }

    private void displayOnUiThread(final String str) {
        AddEditEventActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AddEditEventActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performOperation(final String currUser, final Event event) {
        final Set<String> collisionUsers = new HashSet<>();
        final Task<ArrayList<String>> getEventSubTask = getEventsSubscription(collisionUsers);

        ArrayList<String> eventSubList = null;
        try {
            eventSubList = Tasks.await(getEventSubTask);

            final Task<Event>[] tasks = new Task[eventSubList.size()];

            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = getSingleEventTask(eventSubList.get(i));
            }

            Tasks.whenAll(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    ArrayList<Event> eventsList = new ArrayList<>();

                    for (Task<Event> task1 : tasks) {
                        eventsList.add(task1.getResult());
                    }

                    if (eventsList.isEmpty()) {
                        postToDb(currUser, event);
                        return;
                    }

                    if (checkIfDatesColliding(eventsList, event)) {
                        final Date next = DateTimeManager.gmttoLocalDate(DateTimeManager.getNextTime(eventsList, event));
                        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        final String colliUsers = createStringFromSet(collisionUsers);

                        AddEditEventActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddEditEventActivity.this, "your current schedule is colliding with " + colliUsers +
                                        " You can reschedule your event at " + sdf.format(next), Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        postToDb(currUser, event);
                    }
                }

            });

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String createStringFromSet(Set<String> collisionUsers) {
        StringBuilder sb = new StringBuilder();
        int i = 0;

        for (String s : collisionUsers) {
            sb.append(User.decodeString(s));
            if (i != collisionUsers.size() - 1)
                sb.append(", ");
            i++;
        }
        return sb.toString();
    }

    private Task<Event> getSingleEventTask(String s) {
        final TaskCompletionSource<Event> tcs = new TaskCompletionSource<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        ref.child(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                tcs.setResult(event);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                tcs.setException(databaseError.toException());
            }
        });
        return tcs.getTask();
    }

    private void postToDb(final String currUser, final Event event) {

        final DatabaseReference alarmCounter = FirebaseDatabase.getInstance().getReference()
                .child(AlarmCounter.ALARM_COUNTER_FIELD);

        alarmCounter.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {

                int startAlarmId = data.getValue(Integer.class);
                int endAlarmId = startAlarmId + 1;

                event.setStartAlarmId(startAlarmId);
                event.setEndAlarmId(endAlarmId);
                alarmCounter.setValue(endAlarmId + 1);

                if (editMode) {
                    EventManager.deleteEvent(AddEditEventActivity.this, eventId, editMode);
                }

                String key = EventManager.addEventToDb(AddEditEventActivity.this, event);
                displayOnUiThread("Alarm Set");
                EventSubscriptionManager.addSubscriptionToDb(userAdapter.getSelectedUsers(), key, currUser);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                displayOnUiThread("Call to database failed");
            }
        });
    }


    private boolean checkIfDatesColliding(ArrayList<Event> events, Event currEvent) {
        ArrayList<Event> copEvents = new ArrayList<>(events);
        copEvents.add(currEvent);

        Collections.sort(copEvents, new Comparator<Event>() {       //sort by start time
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getStartTime().compareTo(o2.getStartTime());
            }
        });

        for (int i = 1; i < copEvents.size(); i++) {            //check if collision is there
            if (copEvents.get(i).getStartTime().before(copEvents.get(i - 1).getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private Task<ArrayList<String>> getEventsSubscription(final Set<String> collisionsUsers) {

        final TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        final ArrayList<String> allSubs = new ArrayList<>();
        final ArrayList<String> invitedUsers = userAdapter.getSelectedUsers();

        final DatabaseReference eventSubReference = FirebaseDatabase.getInstance().getReference()
                .child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);

        eventSubReference
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            tcs.setResult(allSubs);
                            return;
                        }

                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            EventSubscription currSub = snap.getValue(EventSubscription.class);
                            String subKey = snap.getKey();

                            if (editMode && subKey.equals(eventId)) {     //skip the current event in the database, otherwise it will always collide
                                continue;
                            }

                            Map<String, String> map = currSub.getSubs();

                            boolean cont = true;

                            for (int i = 0; i < invitedUsers.size(); i++) { //this is one of the subscriptions we are interested in
                                if (map.containsKey(invitedUsers.get(i)) && map.get(invitedUsers.get(i)).equals(Event.GOING)) {
                                    cont = false;
                                    collisionsUsers.add(invitedUsers.get(i));
                                }
                            }
                            if (cont) continue;

                            allSubs.add(snap.getKey());
                        }
                        tcs.setResult(allSubs);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        tcs.setException(databaseError.toException());
                    }
                });
        return tcs.getTask();
    }


    void setUserList() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(User.USER_TABLE);
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
