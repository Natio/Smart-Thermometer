package com.michead.smarterthermometer;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class STApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, Keys.PARSE_KEY_1, Keys.PARSE_KEY_2);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseObject.registerSubclass(Temperature.class);

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
