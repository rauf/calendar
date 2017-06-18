package com.company.calendar.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

/**
 * Created by abdul on 17-Jun-17.
 */

@IgnoreExtraProperties
public class Event {

    public static String EVENT_TABLE = "events";

    public static String GOING = "GOING";
    public static String NOT_GOING = "NOT_GOING";
    public static String MAYBE_GOING = "MAYBE_GOING";
    public static String UNCONFIRMED = "UNCONFIRMED";

    public static String ID_FIELD = "id";
    public static String TITLE_FIELD = "title";
    public static String DESCRIPTION_FIELD = "description";
    public static String OWNER_EMAIL_FIELD = "ownerEmail";

    private String id;
    //check date and time compatibility with firebase

    private String title;
    private String description;
    private String ownerEmail;
/*

    private ArrayList<String> goingUsers;
    private ArrayList<String> notGoingUsers;
    private ArrayList<String> maybeGoingUsers;
    private ArrayList<String> notRespondedUsers;
*/

    public Event() {
        //required. Do not delete
    }

    public Event(String id, String title, String description, String ownerEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerEmail = ownerEmail;
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
/*
    public ArrayList<String> getGoingUsers() {
        return goingUsers;
    }

    public void setGoingUsers(ArrayList<String> goingUsers) {
        this.goingUsers = goingUsers;
    }

    public ArrayList<String> getNotGoingUsers() {
        return notGoingUsers;
    }

    public void setNotGoingUsers(ArrayList<String> notGoingUsers) {
        this.notGoingUsers = notGoingUsers;
    }

    public ArrayList<String> getMaybeGoingUsers() {
        return maybeGoingUsers;
    }

    public void setMaybeGoingUsers(ArrayList<String> maybeGoingUsers) {
        this.maybeGoingUsers = maybeGoingUsers;
    }

    public ArrayList<String> getNotRespondedUsers() {
        return notRespondedUsers;
    }

    public void setNotRespondedUsers(ArrayList<String> notRespondedUsers) {
        this.notRespondedUsers = notRespondedUsers;
    }
    */
}
