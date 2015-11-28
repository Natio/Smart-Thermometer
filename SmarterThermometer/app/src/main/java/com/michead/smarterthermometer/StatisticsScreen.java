package com.michead.smarterthermometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Simone on 11/26/2015.
 */
public class StatisticsScreen extends Fragment{

    private static final String OPENWEATHER_API_KEY = "2e292bad1e6b72870a2975759a06db52";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.statistics_screen, container, false);

        return rootView;
    }

    public static void getWeatherForecastForTomorrowThisTime() throws IOException, JSONException{

        StringBuilder sb = new StringBuilder();
        String request = "http://api.openweathermap.org/data/2.5/forecast?q=%1$2s,ie&appid=%2$2s&units=metric";
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(request, getLocation(), getCurrentHour());

        try{
            URL requestURL = new URL("api.openweathermap.org/data/2.5/weather?q=Dublin");
            URLConnection conn = requestURL.openConnection();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            JSONObject json = new JSONObject(responseStrBuilder.toString());

        }
        catch(IOException ioe){}
        catch(JSONException je){}
    }

    public static String getLocation(){
        return null;
    }

    public static int getCurrentHour(){
        return 0;
    }

}
