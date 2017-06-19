package com.company.calendar.managers;

import com.company.calendar.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;


/**
 * Created by abdul on 17-Jun-17.
 */

public class UserManager {

    private UserManager() {
        //private, cannot be instantiated
    }

    private static String TAG = "UserManager";

    public static void addUserToDB(String name, final String email) {

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        final String encodedEmail = User.encodeString(email);
        final DatabaseReference userTable = db.child(User.USER_TABLE);

        final User user = new User(name, encodedEmail);
        userTable.child(encodedEmail).setValue(user);
    }

    public static ArrayList<User> getAllUsersFromDb(Map<String, Object> map) {

        ArrayList<User> users = new ArrayList<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Map singleUser = (Map) entry.getValue();
            users.add(new User((String) singleUser.get(User.NAME_FIELD), (String) singleUser.get(User.EMAIL_FIELD)));
        }
        return users;
    }


}
