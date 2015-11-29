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

    private static final String TEMPERATURE_PARSE_OBJECT_KEY = "Hour";
    private static final String TIMESTAMP_KEY = "createdAt";

    private List<Temperature> temps;

    private DataStore(){}

    public static DataStore getInstance(){
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public List<Temperature> getTempsInRange(){
        ParseQuery<Temperature> query = ParseQuery.getQuery(TEMPERATURE_PARSE_OBJECT_KEY);

        query.addDescendingOrder(TIMESTAMP_KEY);
        query.setLimit(Utils.MAX_QUERY_RESULT_SIZE);

        List<Temperature> queryResult = null;

        try {
            queryResult = query.find();
            Logger.getAnonymousLogger().log(Level.INFO, "Query result size: " + queryResult.size());
        }
        catch(ParseException pe){
            Logger.getAnonymousLogger().log(Level.SEVERE, pe.getMessage());
        }

        Collections.reverse(queryResult);
        temps = queryResult;

        return temps;
    }

    public List<Temperature> getCachedTemps(){
        return temps;
    }
}
