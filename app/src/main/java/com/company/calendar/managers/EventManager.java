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

    public static String addEventToDb(Context context, Event event) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        String key = db.push().getKey();
        event.setId(key);

        db.child(key).setValue(event);
        return key;
    }

    public static void deleteEvent(final Context context, final String eventId, final boolean editMode) {

        DatabaseReference events = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        final DatabaseReference subs = FirebaseDatabase.getInstance().getReference()
                .child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);

        final String currUser = User.encodeString(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        events.orderByKey()
                .equalTo(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Event curr = dataSnapshot.getChildren().iterator().next().getValue(Event.class);

                        if (!curr.getId().equals(eventId)) {
                            return;
                        }

                        if (curr.getOwnerEmail().equals(currUser)) {
                            dataSnapshot.child(eventId).getRef().removeValue();
                            //deleteSubscription(eventId, context, editMode);
                            subs.child(eventId).removeValue();
                            //AlarmHelper.cancelAlarm(context, curr.getStartAlarmId());
                            //AlarmHelper.cancelAlarm(context, curr.getEndAlarmId());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(context, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });

    }
/*

    private static void deleteSubscription(final String eventId, final Context context, final boolean editMode) {

        DatabaseReference subs = FirebaseDatabase.getInstance().getReference()
                .child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);


        subs.
                orderByKey()
                .equalTo(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnap) {

                        dataSnap.child(eventId).getRef().removeValue();

                        if (!editMode)
                            Toast.makeText(context, "Event Successfully Deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(context, "Call to database failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
*/

}
