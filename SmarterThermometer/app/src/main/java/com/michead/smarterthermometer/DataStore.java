package com.michead.smarterthermometer;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

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

    public void getTemps(FindCallback<Temperature> callback) {
        try {

            ParseQuery<Temperature> query = ParseQuery.getQuery(TEMPERATURE_PARSE_OBJECT_KEY);

            query.addDescendingOrder(TIMESTAMP_KEY);
            query.setLimit(Utils.MAX_QUERY_RESULT_SIZE);

            if (callback == null){
                List<Temperature> queryResult = query.find();
                Collections.reverse(queryResult);
                temps = queryResult;
            }
            else query.findInBackground(callback);
        }
        catch(ParseException pe){
            Logger.getAnonymousLogger().log(Level.SEVERE, pe.getMessage());
        }
    }

    public List<Temperature> getCachedTemps(){
        return temps;
    }

    public void setTemps(List<Temperature> temps){
        this.temps = temps;
    }
}
