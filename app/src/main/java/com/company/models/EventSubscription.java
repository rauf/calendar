package com.company.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by abdul on 17-Jun-17.
 */

@IgnoreExtraProperties
public class EventSubscription {

    public static String EVENT_SUBSCRIPTION_TABLE = "event_subscription";

    public static String USER_EMAIL_FIELD = "userEmail";
    public static String EVENT_ID_FIELD = "eventId";
    public static String STATUS_FIELD = "status";

    private Map<String, String> subs;       //for email to status

    public EventSubscription() {
        //required
    }

    public EventSubscription(Map<String, String> subs) {
        this.subs = subs;
    }

    public Map<String, String> getSubs() {
        return subs;
    }

    public void setSubs(Map<String, String> subs) {
        this.subs = subs;
    }
}
