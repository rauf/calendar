package com.company.calendar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.company.calendar.activities.AddEventActivity;
import com.company.calendar.activities.SignInActivity;
import com.company.calendar.adapters.EventRecyclerViewAdapter;
import com.company.calendar.managers.EventSubscriptionManager;
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
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser firebaseUser;
    private String userName;
    private RecyclerView confirmedEventsRecyclerView;
    private RecyclerView pendingEventsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }


    void updateLists() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);
        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*if (dataSnapshot.getValue() == null)
                            return;

                        ArrayList<EventSubscription> allSubs = EventSubscriptionManager.getAllSubscriptionsFromDb((Map<String, Object>) dataSnapshot.getValue());
                        String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        ArrayList<EventSubscription> currUserSubs = EventSubscriptionManager.filterCurrentUserSubs(allSubs, currUser);
                        setUpRecyclerViews(currUserSubs);*/

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
                            eventRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot snap : dataSnapshot.getChildren()) {

                                        final Event ev = snap.getValue(Event.class);

                                        if (!ev.getId().equals(eventSubscription.getEventId())) {
                                            continue;
                                        }

                                        if (eventSubscription.getStatus().equals(Event.GOING)) {
                                            gEvents.add(ev);
                                        } else {
                                            pEvents.add(ev);
                                        }
                                    }

                                    setRecyclerViews(gEvents, pEvents);
                                    Toast.makeText(MainActivity.this, "All Events Retrieved", LENGTH_SHORT).show();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Cannot retrieve events", Toast.LENGTH_LONG).show();
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

    private void setUpRecyclerViews(ArrayList<EventSubscription> currUserSubs) {

        Toast.makeText(MainActivity.this, "Retrieving events: " + currUserSubs.size(), Toast.LENGTH_SHORT).show();
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);
        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        for (final EventSubscription sub : currUserSubs) {
            eventRef
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final ArrayList<Event> gEvents = new ArrayList<>();
                            final ArrayList<Event> pEvents = new ArrayList<>();

                            for (DataSnapshot snap : dataSnapshot.getChildren()) {

                                final Event ev = snap.getValue(Event.class);

                                if (!ev.getId().equals(sub.getEventId())) {
                                    continue;
                                }

                                if (sub.getStatus().equals(Event.GOING)) {
                                    gEvents.add(ev);
                                } else {
                                    pEvents.add(ev);
                                }
                            }

                            setRecyclerViews(gEvents, pEvents);
                            Toast.makeText(MainActivity.this, "All Events Retrieved", LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, "Call to Database failed", LENGTH_SHORT).show();
                        }
                    });
        }
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
            case R.id.addEvent:
                Intent i = new Intent(this, AddEventActivity.class);
                startActivity(i);
                return true;

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