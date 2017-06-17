package com.company.calendar.models;

/**
 * Created by abdul on 17-Jun-17.
 */

public class EventSubscription {
    private int user;
    private int event;
    private String status;

    public EventSubscription(int user, int event, String status) {
        this.user = user;
        this.event = event;
        this.status = status;
    }
}
