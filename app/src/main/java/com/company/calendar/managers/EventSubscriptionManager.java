package com.company.calendar.managers;

import com.company.calendar.models.Event;
import com.company.calendar.models.EventSubscription;
import com.company.calendar.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by abdul on 18-Jun-17.
 */

public class EventSubscriptionManager {

    private EventSubscriptionManager() {
        //private, cannot be instantiated
    }

    public static void addSubscriptionToDb(ArrayList<String> users, String key, String currUser) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child(EventSubscription.EVENT_SUBSCRIPTION_TABLE);

        Map<String, String> subs = new HashMap<>();

        for (String usr : users) {
            subs.put(usr, Event.UNCONFIRMED);
        }
        subs.put(currUser, Event.GOING);
        db.child(key).setValue(new EventSubscription(subs));
    }
}
