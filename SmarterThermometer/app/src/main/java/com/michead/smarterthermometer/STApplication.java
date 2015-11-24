package com.michead.smarterthermometer;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class STApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "GkfQgBEWhTwEWe6edrUDDt0sZ9DLPubO5HYrHBh7", "LeHrMeZQY2EfuDkQgfx8iyQL6NAS9uswZFFtpwcd");
        ParseObject.registerSubclass(Temperature.class);
        ParseObject.registerSubclass(HTemperature.class);

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
