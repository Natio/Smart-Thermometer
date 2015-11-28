package com.michead.smarterthermometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Simone on 11/26/2015.
 */
public class StatisticsScreen extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TEXTVIEW_SEPARATOR = ": ";
    private static final String CELSIUS_DEGS = "°C";

    private static final DecimalFormat df = new DecimalFormat("#.00");

    private SwipeRefreshLayout srl;

    private TextView today_in_min;
    private TextView today_in_max;
    private TextView today_out_min;
    private TextView today_out_max;

    private TextView tomorrow_in_same_time;
    private TextView tomorrow_out_same_time;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.statistics_screen, container, false);

        srl = (SwipeRefreshLayout)rootView.findViewById(R.id.srl);
        srl.setOnRefreshListener(this);

        today_in_min = (TextView)rootView.findViewById(R.id.today_min_in);
        today_in_max = (TextView)rootView.findViewById(R.id.today_max_in);
        today_out_min = (TextView)rootView.findViewById(R.id.today_min_out);
        today_out_max = (TextView)rootView.findViewById(R.id.today_max_out);

        tomorrow_in_same_time = (TextView)rootView.findViewById(R.id.tomorrow_in_same_time);
        tomorrow_out_same_time = (TextView)rootView.findViewById(R.id.tomorrow_out_same_time);

        initTextViews();

        return rootView;
    }

    @SuppressWarnings("all")
    public void initTextViews(){
        today_in_min.setText("Min temp inside today" + TEXTVIEW_SEPARATOR);
        today_in_max.setText("Max temp inside today" + TEXTVIEW_SEPARATOR);
        today_out_min.setText("Min temp outside today" + TEXTVIEW_SEPARATOR);
        today_out_max.setText("Max temp outside today" + TEXTVIEW_SEPARATOR);

        tomorrow_in_same_time.setText("Expected temperature inside tomorrow this time: " + TEXTVIEW_SEPARATOR);
        tomorrow_out_same_time.setText("Expected temperature outside tomorrow this time: " + TEXTVIEW_SEPARATOR);
    }

    @Override
    public void onResume(){
        super.onResume();

        List<Temperature> temps = DataStore.getInstance().getCachedTemps();

        updateTodayStats(temps);
        updateTomorrowStats(temps);
    }

    @Override
    public void onRefresh() {

        List<Temperature> temps = DataStore.getInstance().getCachedTemps();

        updateTodayStats(temps);
        updateTomorrowStats(temps);

        srl.setRefreshing(false);
    }

    public void updateTodayStats(List<Temperature> temps){

        double minIn = Float.MAX_VALUE;
        double minOut = Float.MAX_VALUE;
        double maxIn = Float.MIN_VALUE;
        double maxOut = Float.MIN_VALUE;

        for (Temperature temp : temps){
            if (temp.getInTemp() < minIn) minIn = temp.getInTemp();
            if (temp.getInTemp() > maxIn) maxIn = temp.getInTemp();
            if (temp.getOutTemp() < minOut) minOut = temp.getOutTemp();
            if (temp.getOutTemp() > maxOut) maxOut = temp.getOutTemp();
        }

        updateView(today_in_min, minIn);
        updateView(today_in_max, maxIn);
        updateView(today_out_min, minOut);
        updateView(today_out_max, maxOut);
    }

    public void updateTomorrowStats(List<Temperature> temps){

        double inTemp = temps.get(temps.size() - 1).getInTemp();
        double outTemp = temps.get(temps.size() - 1).getOutTemp();

        double diff = inTemp - outTemp;

        double tomorrowTempOut = Utils.getWeatherForecastForTomorrowThisTime(getActivity());
        double tomorrowTempIn = tomorrowTempOut + diff;

        updateView(tomorrow_in_same_time, tomorrowTempIn);
        updateView(tomorrow_out_same_time, tomorrowTempOut);
    }

    @SuppressWarnings("all")
    public void updateView(TextView tv, double val){

        tv.setText(  tv.getText().toString().split(TEXTVIEW_SEPARATOR)[0] + TEXTVIEW_SEPARATOR + df.format(val) + CELSIUS_DEGS);
    }
}
