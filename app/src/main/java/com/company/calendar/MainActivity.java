package com.company.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.company.calendar.activities.AddEditEventActivity;
import com.company.calendar.activities.SignInActivity;
import com.company.calendar.adapters.EventRecyclerViewAdapter;
import com.company.calendar.managers.AlarmHelper;
import com.company.calendar.models.AlarmCounter;
import com.company.calendar.models.Event;
import com.company.calendar.models.EventSubscription;
import com.company.calendar.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser firebaseUser;
    private String userName;
    private RecyclerView confirmedEventsRecyclerView;
    private RecyclerView pendingEventsRecyclerView;
    private Button addEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        confirmedEventsRecyclerView = (RecyclerView) findViewById(R.id.confirmedEventsRecyclerView);
        pendingEventsRecyclerView = (RecyclerView) findViewById(R.id.pendingEventsRecyclerView);

        if (firebaseUser == null) {
            //not signed in, launch login activity
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else {
            userName = firebaseUser.getDisplayName();
        }

        addEventButton = (Button) findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditEventActivity.class);
                startActivity(intent);
            }
        });
    }


    void updateLists() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);
        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        //FirebaseDatabase.getInstance().getReference().child(AlarmCounter.ALARM_COUNTER_FIELD).setValue(1);

        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final ArrayList<Event> gEvents = new ArrayList<>();
                        final ArrayList<Event> pEvents = new ArrayList<>();

                        if (dataSnapshot == null) {
                            setRecyclerViews(gEvents, pEvents);
                            return;
                        }

                        for (final DataSnapshot sub : dataSnapshot.getChildren()) {
                            final EventSubscription eventSubscription = sub.getValue(EventSubscription.class);

                            if (!eventSubscription.getUserEmail().equals(currUser)) {
                                continue;
                            }
                            DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);
                            filterEvents(gEvents, pEvents, eventSubscription, eventRef);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Cannot retrieve events", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filterEvents(final ArrayList<Event> gEvents, final ArrayList<Event> pEvents, final EventSubscription eventSubscription, DatabaseReference eventRef) {
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    final Event ev = snap.getValue(Event.class);

                    if (!ev.getId().equals(eventSubscription.getEventId())) {
                        continue;
                    }

                    if (eventSubscription.getStatus().equals(Event.GOING)) {
                        AlarmHelper.setAlarm(MainActivity.this, ev, ev.getAlarmId(), ev.getYear(),       //alarms set for only going events
                                ev.getMonth(), ev.getDate(), ev.getHour(), ev.getMinute());
                        gEvents.add(ev);
                    } else {
                        AlarmHelper.cancelAlarm(MainActivity.this, ev.getAlarmId());        //cancel alarms for pending events
                        pEvents.add(ev);
                    }
                }

                setRecyclerViews(gEvents, pEvents);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setRecyclerViews(ArrayList<Event> gEvents, ArrayList<Event> pEvents) {
        EventRecyclerViewAdapter goingAdapter = new EventRecyclerViewAdapter(gEvents, MainActivity.this);
        EventRecyclerViewAdapter pendingAdapter = new EventRecyclerViewAdapter(pEvents, MainActivity.this);

        confirmedEventsRecyclerView.setAdapter(goingAdapter);
        pendingEventsRecyclerView.setAdapter(pendingAdapter);
        confirmedEventsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        pendingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }


    @Override
    protected void onStart() {
        super.onStart();

        updateLists();
        //firebaseAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null)
            firebaseAuth.removeAuthStateListener(authListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                firebaseAuth.signOut();
                Toast.makeText(MainActivity.this, "You are logged out. ", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}