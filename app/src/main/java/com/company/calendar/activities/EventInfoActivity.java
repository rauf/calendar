package com.company.calendar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.company.calendar.R;
import com.company.calendar.managers.DateTimeManager;
import com.company.calendar.managers.EventManager;
import com.company.calendar.models.Event;
import com.company.calendar.models.EventSubscription;
import com.company.calendar.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by abdul on 18-Jun-17.
 */

public class EventInfoActivity extends AppCompatActivity {

    public static String EVENT_ID = "eventId";

    private String eventId;
    private TextView titleInfo;
    private TextView descriptionInfo;
    private TextView ownerInfo;
    private TextView otherUsersInfo;
    private RadioGroup responseRadioGroup;
    private RadioButton goingRadioButton;
    private RadioButton notGoingRadioButton;
    private RadioButton maybeGoingRadioButton;
    private Button editEventButton;
    private Button deleteEventButton;
    private int startAlarmId;
    private int endAlarmId;
    private String ownerEmail;
    private TextView timeInfo;
    private TextView dateInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        Intent receivedIntent = getIntent();

        eventId = receivedIntent.getStringExtra(EVENT_ID);
        startAlarmId = receivedIntent.getIntExtra(Event.START_ALARM_ID_FIELD, 0);
        endAlarmId = receivedIntent.getIntExtra(Event.END_ALARM_ID_FIELD, 0);
        ownerEmail = receivedIntent.getStringExtra(Event.OWNER_EMAIL_FIELD);

        if (eventId == null) {
            Toast.makeText(EventInfoActivity.this, "Event Id not received by this activity. Closing it", Toast.LENGTH_SHORT).show();
            finish();
        }
        initialiseViews();
        populateEventInfo(eventId);
        populateStatusInfo(eventId);
        responseRadioGroup.setOnCheckedChangeListener(postStatusChangeToDb());
        deleteEventButton.setOnClickListener(getDeleteButtonOnClickListener());
        editEventButton.setOnClickListener(getEditButtonOnClickListener());
    }

    @NonNull
    private View.OnClickListener getEditButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEditEventClick();
            }
        };
    }

    @NonNull
    private View.OnClickListener getDeleteButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.deleteEvent(EventInfoActivity.this, eventId, false);
                finish();
            }
        };
    }

    private void handleEditEventClick() {
        String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        if (!currUser.equals(ownerEmail)) {
            Toast.makeText(EventInfoActivity.this, "Cannot edit. You are not the owner", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent in = new Intent(EventInfoActivity.this, AddEditEventActivity.class);
        in.putExtra(AddEditEventActivity.EDIT_EVENT_MODE, true);
        in.putExtra(Event.ID_FIELD, eventId);
        in.putExtra(Event.START_ALARM_ID_FIELD, startAlarmId);
        in.putExtra(Event.END_ALARM_ID_FIELD, endAlarmId);
        startActivity(in);
        finish();
    }


    private RadioGroup.OnCheckedChangeListener postStatusChangeToDb() {
        return new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int selectedId = responseRadioGroup.getCheckedRadioButtonId();

                final String status = getStatusFromRadioSelect(selectedId);
                final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                                .child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            String email = (String) snap.child(EventSubscription.USER_EMAIL_FIELD).getValue();
                            String evId = (String) snap.child(EventSubscription.EVENT_ID_FIELD).getValue();

                            if (email.equals(currUser) && evId.equals(eventId)) {
                                snap.child(EventSubscription.STATUS_FIELD).getRef().setValue(status);
                            }
                        }
                        //Toast.makeText(EventInfoActivity.this, "Your response is saved", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EventInfoActivity.this, "Call to database failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }

    private String getStatusFromRadioSelect(int selectedId) {
        switch (selectedId) {
            case R.id.goingRadioButton:
                return Event.GOING;
            case R.id.notGoingRadioButton:
                return Event.NOT_GOING;
            case R.id.maybeGoingRadioButton:
                return Event.MAYBE_GOING;
            default:
                return Event.UNCONFIRMED;
        }
    }


    private void populateStatusInfo(final String event) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);
        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        final Map<String, ArrayList<String>> map = new HashMap<>();

        ref
                //.equalTo(event)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        map.clear();
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            createStatusMap(snap, event, currUser, map);
                        }
                        otherUsersInfo.setText(buildStringFromMap(map));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EventInfoActivity.this, "Call to Database failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @NonNull
    private String buildStringFromMap(Map<String, ArrayList<String>> map) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {

            String key = entry.getKey();

            if (key.equals(Event.NOT_GOING)) {
                key = "NOT GOING";
            } else if (key.equals(Event.MAYBE_GOING)) {
                key = "MAYBE GOING";
            }

            sb.append(key).append('\n');
            ArrayList<String> list = entry.getValue();

            for (String str : list) {
                sb.append('\t').append("\t\t\t\t").append(User.decodeString(str)).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private void createStatusMap(DataSnapshot snap, String event, String currUser, Map<String, ArrayList<String>> map) {
        EventSubscription sub = snap.getValue(EventSubscription.class);

        if (sub.getEventId().equals(event) && sub.getUserEmail().equals(currUser)) {
            updataRadioButtons(sub.getStatus());
        } else if (sub.getEventId().equals(event)) {
            if (map.containsKey(sub.getStatus())) {
                ArrayList<String> list = map.get(sub.getStatus());
                list.add(sub.getUserEmail());
                map.put(sub.getStatus(), list);
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(sub.getUserEmail());
                map.put(sub.getStatus(), list);
            }
        }
    }

    private void updataRadioButtons(String status) {
        switch (status) {
            case Event.GOING:
                goingRadioButton.setChecked(true);
                return;
            case Event.NOT_GOING:
                notGoingRadioButton.setChecked(true);
                return;
            case Event.MAYBE_GOING:
                maybeGoingRadioButton.setChecked(true);
                return;
            default:
                return;
        }
    }

    private void populateEventInfo(final String event) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        ref
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            if (snap.child(Event.ID_FIELD).getValue().equals(event)) {
                                Event ev = snap.getValue(Event.class);

                                titleInfo.setText(ev.getTitle());
                                descriptionInfo.setText(ev.getDescription());
                                ownerInfo.setText(User.decodeString(ev.getOwnerEmail()));
                                dateInfo.setText(DateTimeManager.getDateString(DateTimeManager.gmttoLocalDate(ev.getStartTime())) +
                                        "  -  "  + DateTimeManager.getDateString(DateTimeManager.gmttoLocalDate(ev.getEndTime())));
                                timeInfo.setText(DateTimeManager.getTimeString(DateTimeManager.gmttoLocalDate(ev.getStartTime())) +
                                        "  -  " + DateTimeManager.getTimeString(DateTimeManager.gmttoLocalDate(ev.getEndTime())));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EventInfoActivity.this, "Call to Database failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initialiseViews() {
        titleInfo = (TextView) findViewById(R.id.titleInfo);
        descriptionInfo = (TextView) findViewById(R.id.descriptionInfo);
        ownerInfo = (TextView) findViewById(R.id.ownerInfo);
        dateInfo = (TextView) findViewById(R.id.dateInfo);
        timeInfo = (TextView) findViewById(R.id.timeInfo);
        responseRadioGroup = (RadioGroup) findViewById(R.id.responseRadioGroup);
        editEventButton = (Button) findViewById(R.id.editEventButton);
        deleteEventButton = (Button) findViewById(R.id.deleteEventButton);
        goingRadioButton = (RadioButton) findViewById(R.id.goingRadioButton);
        notGoingRadioButton = (RadioButton) findViewById(R.id.notGoingRadioButton);
        maybeGoingRadioButton = (RadioButton) findViewById(R.id.maybeGoingRadioButton);
        otherUsersInfo = (TextView) findViewById(R.id.otherUsersInfo);
    }

}
