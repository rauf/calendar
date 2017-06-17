package com.company.calendar.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by abdul on 17-Jun-17.
 */

@IgnoreExtraProperties
public class Event {

    private static String GOING = "GOING";
    private static String NOT_GOING = "NOT_GOING";
    private static String MAYBE_GOING = "MAYBE_GOING";
    private static String UNCONFIRMED = "UNCONFIRMED";

    private int id;
    //check date and time compatibility with firebase

    private String title;
    private String description;
    private String ownerEmail;

    private ArrayList<String> goingUsers;
    private ArrayList<String> notgoingUsers;
    private ArrayList<String> maybeGoingUsers;
    private ArrayList<String> notRespondedUsers;

    public Event() {
        //required. Do not delete
    }

    public Event(int id, String title, String description, String ownerEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerEmail = ownerEmail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public ArrayList<String> getGoingUsers() {
        return goingUsers;
    }

    public void setGoingUsers(ArrayList<String> goingUsers) {
        this.goingUsers = goingUsers;
    }

    public ArrayList<String> getNotgoingUsers() {
        return notgoingUsers;
    }

    public void setNotgoingUsers(ArrayList<String> notgoingUsers) {
        this.notgoingUsers = notgoingUsers;
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
}
