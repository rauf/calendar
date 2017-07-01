package com.company.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by abdul on 17-Jun-17.
 */

@IgnoreExtraProperties
public class Event {

    public static String EVENT_TABLE = "events";

    public static final String GOING = "GOING";
    public static final String NOT_GOING = "NOT_GOING";
    public static final String MAYBE_GOING = "MAYBE_GOING";
    public static final String UNCONFIRMED = "UNCONFIRMED";

    public static final String ID_FIELD = "id";
    public static final String TITLE_FIELD = "title";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String OWNER_EMAIL_FIELD = "ownerEmail";
    public static final String START_ALARM_ID_FIELD = "startAlarmId";
    public static final String END_ALARM_ID_FIELD = "endAlarmId";

    private String id;

    private String title;
    private String description;
    private String ownerEmail;

    private int startAlarmId;
    private int endAlarmId;

    private Date startTime;
    private Date endTime;

    public Event() {
        //required. Do not delete
    }

    public Event(String id, String title, String description, String ownerEmail, int startAlarmId, int endAlarmId, Date startTime, Date endTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerEmail = ownerEmail;
        this.startAlarmId = startAlarmId;
        this.endAlarmId = endAlarmId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public int getStartAlarmId() {
        return startAlarmId;
    }

    public void setStartAlarmId(int startAlarmId) {
        this.startAlarmId = startAlarmId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getEndAlarmId() {
        return endAlarmId;
    }

    public void setEndAlarmId(int endAlarmId) {
        this.endAlarmId = endAlarmId;
    }
}
