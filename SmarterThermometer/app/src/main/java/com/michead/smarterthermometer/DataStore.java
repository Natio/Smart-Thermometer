package com.michead.smarterthermometer;

import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Simone on 11/24/2015.
 */
public class DataStore {

    private static DataStore instance;

    private List<Temperature> temps;
    private List<HTemperature> hTemps;

    private DataStore(){}

    public static DataStore getInstance(){
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public List<Temperature> getTempsInRange(){
        ParseQuery<Temperature> query = ParseQuery.getQuery("Temperatures");

        query.addDescendingOrder("createdAt");

        List<Temperature> queryResult = null;

        try {
            queryResult = query.find();
            Logger.getAnonymousLogger().log(Level.INFO, "Query result size: " + queryResult.size());
        }
        catch(ParseException pe){
            Logger.getAnonymousLogger().log(Level.SEVERE, pe.getMessage());
        }

        Collections.reverse(queryResult);

        return queryResult;
    }

    public List<HTemperature> getHTempsInRange() {
        ParseQuery<HTemperature> query = ParseQuery.getQuery("Hour");

        query.addDescendingOrder("createdAt");

        List<HTemperature> queryResult = null;

        try {
            queryResult = query.find();
            Logger.getAnonymousLogger().log(Level.INFO, "Query result size: " + queryResult.size());
        }
        catch(ParseException pe){
            Logger.getAnonymousLogger().log(Level.SEVERE, pe.getMessage());
        }

        Collections.reverse(queryResult);

        return queryResult;
    }

    public List<Temperature> getCachedTemps(){
        return temps;
    }

    public List<HTemperature> getCachedHTemps(){
        return hTemps;
    }
}
