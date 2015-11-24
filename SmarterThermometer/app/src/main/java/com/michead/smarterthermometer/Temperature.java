package com.michead.smarterthermometer;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Simone on 11/24/2015.
 */
@ParseClassName("Temperatures")
public class Temperature extends ParseObject {

    private static final String INSIDE_TEMPERATURE_ATTRIBUTE = "inside";
    private static final String OUTSIDE_TEMPERATURE_ATTRIBUTE = "outside";

    public Temperature(){
        super();
    }

    public double getInTemp(){
        return getDouble(INSIDE_TEMPERATURE_ATTRIBUTE);
    }

    public double getOutTemp(){
        return getDouble(OUTSIDE_TEMPERATURE_ATTRIBUTE);
    }

    public Date getTimestamp(){
        return getCreatedAt();
    }
}
