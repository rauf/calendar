package com.company.calendar.managers;

import com.company.calendar.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by abdul on 17-Jun-17.
 */

public class UserManager {

    private static String TAG = "UserManager";
    private DatabaseReference db;

    public UserManager() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    public void addUserToDB(String name, final String email) {

        final String encodedEmail = User.encodeString(email);
        final DatabaseReference userTable = db.child(User.USER_TABLE);

        final User user = new User(name, encodedEmail);
        userTable.child(encodedEmail).setValue(user);
    }
}
