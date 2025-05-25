package com.example.quietspaceeee.data.viewmodel;

import android.content.Context;

import com.example.quietspaceeee.data.db.UserDatabaseHelper;
import com.example.quietspaceeee.data.model.User;

public class UserRepository {
    private UserDatabaseHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    public boolean registerUser(User user) {
        return dbHelper.addUser(user);
    }

    public boolean loginUser(String email, String password) {
        return dbHelper.checkUser(email, password);
    }

    public boolean userExists(String email) {
        return dbHelper.checkUserExists(email);
    }
}
