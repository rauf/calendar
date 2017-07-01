package com.company;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.company.activities.SignInActivity;
import com.company.adapters.EventRecyclerViewAdapter;
import com.company.activities.AddEditEventActivity;
import com.company.managers.AlarmHelper;
import com.company.models.Event;
import com.company.models.EventSubscription;
import com.company.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser firebaseUser;
    private RecyclerView confirmedEventsRecyclerView;
    private RecyclerView pendingEventsRecyclerView;
    private Button addEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        initialiseViews();

        if (firebaseUser == null) {
            //not signed in, launch login activity
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else {
            String userName = firebaseUser.getDisplayName();
        }

        addEventButton.setOnClickListener(getAddEventButtonOnClickListener());
    }

    private void initialiseViews() {
        confirmedEventsRecyclerView = (RecyclerView) findViewById(R.id.confirmedEventsRecyclerView);
        pendingEventsRecyclerView = (RecyclerView) findViewById(R.id.pendingEventsRecyclerView);
        addEventButton = (Button) findViewById(R.id.addEventButton);
    }

    @NonNull
    private View.OnClickListener getAddEventButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditEventActivity.class);
                startActivity(intent);
            }
        };
    }


    void updateLists() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);

        ref.
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final ArrayList<Event> gEvents = new ArrayList<>();
                        final ArrayList<Event> pEvents = new ArrayList<>();
                        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                        if (dataSnapshot == null || !dataSnapshot.exists()) {
                            setRecyclerViews(gEvents, pEvents);
                            return;
                        }

                        for (final DataSnapshot sub : dataSnapshot.getChildren()) {
                            final EventSubscription eventSubscription = sub.getValue(EventSubscription.class);

                            Map<String, String> userSubs = eventSubscription.getSubs();

                            if (!userSubs.containsKey(currUser)) {
                                continue;
                            }
                            filterEvents(gEvents, pEvents, currUser, userSubs, sub.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Cannot retrieve events", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filterEvents(final ArrayList<Event> gEvents, final ArrayList<Event> pEvents, final String currUser, final Map<String, String> userSubs, String key) {

        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        eventRef
                .orderByKey()
                .equalTo(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final Iterator<DataSnapshot> itr = dataSnapshot.getChildren().iterator();
                        Event ev;

                        if (itr.hasNext()) {
                            ev = itr.next().getValue(Event.class);
                        } else {
                            return;
                        }

                        if (userSubs.get(currUser).equals(Event.GOING)) {
                            AlarmHelper.setAlarm(MainActivity.this, ev);
                            gEvents.add(ev);
                        } else {
                            AlarmHelper.cancelAlarm(MainActivity.this, ev.getStartAlarmId());        //cancel alarms for pending events
                            AlarmHelper.cancelAlarm(MainActivity.this, ev.getEndAlarmId());
                            pEvents.add(ev);
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