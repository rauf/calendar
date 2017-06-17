package com.company.calendar.models;

import java.util.Set;

/**
 * Created by abdul on 17-Jun-17.
 */

public class Event {
    private int id;
    private int name;
    //check date and time compatibility with firebase

    private Set<Integer> goingUsersId;
    private Set<Integer> notGoingUsersId;
    private Set<Integer> maybeGoingUsersId;
    private Set<Integer> notRespondedUsersId;
}
