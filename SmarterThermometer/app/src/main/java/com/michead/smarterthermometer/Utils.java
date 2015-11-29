package com.michead.smarterthermometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Simone on 11/28/2015.
 */
public class Utils {

    public static final int MAX_QUERY_RESULT_SIZE = 24;
    public static final String LOCATION_KEY = "location";
    public static final String DEFAULT_LOCATION = "dublin";
    public static final String TEXTVIEW_SEPARATOR = ": ";

    public static final String OPENWEATHER_LIST_KEY = "list";
    public static final String OPENWEATHER_MAIN_KEY = "main";
    public static final String OPENWEATHER_TEMP_KEY = "temp";
    public static final int OPENWEATHER_TOMORROW_SAME_TIME_INDEX = 7;

    public static String getCurrentLocation(Context context) {

        String loc = null;

        try{
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Geocoder gcd = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            loc = addresses.get(0).getLocality();
        }
        catch(SecurityException se){}
        catch(IOException ioe){}

        if (loc == null || loc.equals(""))
            return Utils.DEFAULT_LOCATION;
        else
            return loc;
    }

    public static String getLocation(Context context){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String loc_setting = settings.getString(Utils.LOCATION_KEY, "");

        if (loc_setting == null || loc_setting.equals("")){
            SharedPreferences.Editor editor = settings.edit();
            loc_setting = getCurrentLocation(context);
            editor.putString(Utils.LOCATION_KEY, loc_setting);
            editor.commit();
        }

        return loc_setting;
    }

    @Deprecated
    public static int getCurrentHour(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static double getWeatherForecastForTomorrowThisTime(Context context) {

        double temperature = Float.MAX_VALUE;

        try{
            StringBuilder sb = new StringBuilder();
            String request = "http://api.openweathermap.org/data/2.5/forecast?q=%1$2s,ie&appid=%2$2s&units=metric";
            Formatter formatter = new Formatter(sb, Locale.US);
            formatter.format(request, Utils.getLocation(context), Keys.OPENWEATHER_API_KEY);

            temperature = new AsyncGetRequestor().execute(sb.toString()).get();
        }
        catch(ExecutionException ee){}
        catch(InterruptedException ie){}

        return temperature;
    }

    static class AsyncGetRequestor extends AsyncTask<String, Void, Double> {

        @Override
        protected Double doInBackground(String... params) {

            double temperature = Double.MAX_VALUE;

            try{
                URL requestURL = new URL(params[0]);
                URLConnection conn = requestURL.openConnection();

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                JSONObject json = new JSONObject(responseStrBuilder.toString());
                JSONObject tomorrow = (JSONObject)json.getJSONArray(Utils.OPENWEATHER_LIST_KEY).get(Utils.OPENWEATHER_TOMORROW_SAME_TIME_INDEX);
                JSONObject main = (JSONObject)tomorrow.get(Utils.OPENWEATHER_MAIN_KEY);
                temperature = main.getDouble(Utils.OPENWEATHER_TEMP_KEY);
            }
            catch(IOException ioe){}
            catch(JSONException je){}

            return temperature;
        }
    }

    public static String everyWordToUpperCase(String s) {
        String[] arr = s.split(" |-");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" "); // TODO Handle dash case
        }

        return sb.toString().trim();
    }
}
