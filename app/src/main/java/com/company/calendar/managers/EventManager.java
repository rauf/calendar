package com.company.calendar.managers;

import com.company.calendar.models.Event;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by abdul on 18-Jun-17.
 */

public class EventManager {

    private EventManager() {
        //private, cannot be instantiated
    }

    public static String addEventToDb(String title, String description, String ownerEmail) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        String key = db.push().getKey();

        final Event event = new Event(key, title, description, ownerEmail);
        db.child(key).setValue(event);
        return key;
    }

/*
    public static Event getEventFromDb(String eventId) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(Event.EVENT_TABLE);

        String str = db.equalTo(eventId);


    }
*/

}
