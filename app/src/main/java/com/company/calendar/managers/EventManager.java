package com.company.calendar.managers;

import android.content.Context;
import android.widget.Toast;

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

public class EventManager {

    private EventManager() {
        //private, cannot be instantiated
    }

    public static String addEventToDb(Event event) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        String key = db.push().getKey();
        event.setId(key);

        event.setStartTime(DateTimeManager.toGMT(event.getStartTime()));        //convert to gmt
        event.setEndTime(DateTimeManager.toGMT(event.getEndTime()));

        db.child(key).setValue(event);
        return key;
    }

    public static void deleteEvent(final Context context, final String eventId, final boolean editMode) {

        DatabaseReference events = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);
        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        events
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        findAndDeleteEvent(dataSnapshot, eventId, currUser, context, editMode);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(context, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private static void findAndDeleteEvent(DataSnapshot dataSnapshot, String eventId, String currUser, Context context, boolean editMode) {
        boolean exit = true;

        for (DataSnapshot snap : dataSnapshot.getChildren()) {
            Event event = snap.getValue(Event.class);

            if (event.getId().equals(eventId) && event.getOwnerEmail().equals(currUser)) {
                snap.getRef().removeValue();
                exit = false;
            }
        }

        if (exit) {
            //Toast.makeText(context, "You don't have permissions to delete this event. You are not the owner", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference subs = FirebaseDatabase.getInstance().getReference()
                .child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);
        deleteSubscription(subs, eventId, context, editMode);
    }

    private static void deleteSubscription(DatabaseReference subs, final String eventId, final Context context, final boolean editMode) {
        subs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnap) {
                for (DataSnapshot snap : dataSnap.getChildren()) {
                    EventSubscription sub = snap.getValue(EventSubscription.class);

                    if (sub.getEventId().equals(eventId)) {
                        snap.getRef().removeValue();
                    }
                }

                if (!editMode)
                Toast.makeText(context, "Event Successfully Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Call to database failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
