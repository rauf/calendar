package com.company.calendar.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by abdul on 17-Jun-17.
 */

@IgnoreExtraProperties
public class User {

    public static String USER_TABLE = "users";
    public static String NAME_FIELD = "name";
    public static String EMAIL_FIELD = "email";

    private String name;
    private String email;

    public User() {
        //this is required. Do not delete
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }


    public static String encodeString(String string) {
        return string.replace(".", ",");
    }

    public static String decodeString(String string) {
        return string.replace(",", ".");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
