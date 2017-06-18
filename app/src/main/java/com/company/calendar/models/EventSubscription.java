package com.company.calendar.models;

/**
 * Created by abdul on 17-Jun-17.
 */

public class EventSubscription {

    public static String EVENT_SUBSCRIPTION_TABLE = "event_subscription";

    public static String USER_EMAIL_FIELD = "userEmail";
    public static String EVENT_ID_FIELD = "eventId";
    public static String STATUS_FIELD = "status";

    private String userEmail;
    private String eventId;
    private String status;

    public EventSubscription(String userEmail, String eventId, String status) {
        this.userEmail = userEmail;
        this.eventId = eventId;
        this.status = status;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
