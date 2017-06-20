package com.company.calendar.models;

import com.google.firebase.database.IgnoreExtraProperties;

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
    public static final String ALARM_ID_FIELD = "alarmId";

    private String id;
    //check date and time compatibility with firebase

    private String title;
    private String description;
    private String ownerEmail;

    private int alarmId;
    private int year;
    private int month;
    private int date;
    private int hour;
    private int minute;

    public Event() {
        //required. Do not delete
    }

    public Event(String id, String title, String description, String ownerEmail, int alarmId, int year, int month, int date, int hour, int minute) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerEmail = ownerEmail;
        this.alarmId = alarmId;
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
    }

    public Event(String title, String description, String ownerEmail, int alarmId, int year, int month, int date, int hour, int minute) {
        this.title = title;
        this.description = description;
        this.ownerEmail = ownerEmail;
        this.alarmId = alarmId;
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
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

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getDateString() {
        return date + " / " + (month + 1) + " / " + year;
    }

    public String getTimeString() {
        return hour + " : " + minute;
    }

}
