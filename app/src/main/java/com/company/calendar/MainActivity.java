package com.company.calendar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.company.calendar.activities.AddEventActivity;
import com.company.calendar.activities.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    @Override
    protected void onStart() {
        super.onStart();
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