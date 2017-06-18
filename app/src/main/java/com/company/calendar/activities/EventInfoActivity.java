package com.company.calendar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.company.calendar.R;
import com.company.calendar.models.Event;
import com.company.calendar.models.EventSubscription;
import com.company.calendar.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by abdul on 18-Jun-17.
 */

public class EventInfoActivity extends AppCompatActivity {

    public static String EVENT_ID = "eventId";

    private String eventId;
    private TextView titleInfo;
    private TextView descriptionInfo;
    private RadioGroup responseRadioGroup;
    private RadioButton goingRadioButton;
    private RadioButton notGoingRadioButton;
    private RadioButton maybeGoingRadioButton;
    private RadioButton radioButton;
    private Button editEventButton;
    private Button deleteEventButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        Intent receivedIntent = getIntent();

        eventId = receivedIntent.getStringExtra(EVENT_ID);

        if (eventId == null) {
            Toast.makeText(EventInfoActivity.this, "Event Id not received by this activity. Closing it", Toast.LENGTH_SHORT).show();
            finish();
        }
        initialiseViews();
        populateEventInfo(eventId);
        populateStatusInfo(eventId);
        responseRadioGroup.setOnCheckedChangeListener(postStatusChangeToDb());
    }


    private RadioGroup.OnCheckedChangeListener postStatusChangeToDb() {
        return new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int selectedId = responseRadioGroup.getCheckedRadioButtonId();

                final String status = getStatusFromRadioSelect(selectedId);
                final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);

                Toast.makeText(EventInfoActivity.this, "Saving your response", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(EventInfoActivity.this, "Your response is saved", Toast.LENGTH_SHORT).show();
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

        ref
                //.equalTo(event)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {

                            String user = (String) snap.child(EventSubscription.USER_EMAIL_FIELD).getValue();
                            String ev = (String) snap.child(EventSubscription.EVENT_ID_FIELD).getValue();

                            if (ev.equals(event) && user.equals(currUser)) {

                                String status = (String) snap.child(EventSubscription.STATUS_FIELD).getValue();
                                updataRadioButtons(status);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EventInfoActivity.this, "Call to Database failed", Toast.LENGTH_SHORT).show();
                    }
                });
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
                //.equalTo(event)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            if (snap.child(Event.ID_FIELD).getValue().equals(event)) {
                                String title = (String) snap.child(Event.TITLE_FIELD).getValue();
                                String description = (String) snap.child(Event.DESCRIPTION_FIELD).getValue();

                                titleInfo.setText(title);
                                descriptionInfo.setText(description);
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
        responseRadioGroup = (RadioGroup) findViewById(R.id.responseRadioGroup);
        editEventButton = (Button) findViewById(R.id.editEventButton);
        deleteEventButton = (Button) findViewById(R.id.deleteEventButton);
        goingRadioButton = (RadioButton) findViewById(R.id.goingRadioButton);
        notGoingRadioButton = (RadioButton) findViewById(R.id.notGoingRadioButton);
        maybeGoingRadioButton = (RadioButton) findViewById(R.id.maybeGoingRadioButton);
    }

}
