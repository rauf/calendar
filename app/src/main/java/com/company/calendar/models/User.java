package com.company.calendar.models;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by abdul on 17-Jun-17.
 */

public class User {
    private int id;
    private String name;
    private String email;

    ArrayList<Integer> selfCreateEvents;  //events which are create by user itself. If it is deleted, then whole event will be deleted.
    ArrayList<Integer> extraEvents;       //events which are created by other users. The current user has no control over it.
}
