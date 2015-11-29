package com.michead.smarterthermometer;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simone on 11/26/2015.
 */
public class StatisticsScreen extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String CELSIUS_DEGS = "°C";
    private static final int TV_TEXT_SIZE = 18;
    private static final int TV_PADDING_V = 30;
    private static final int TV_PADDING_H = 5;

    private static final DecimalFormat df = new DecimalFormat("#.00");

    private SwipeRefreshLayout srl;

    private List<TextView> tvs = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.statistics_screen, container, false);

        srl = (SwipeRefreshLayout)rootView.findViewById(R.id.srl);
        srl.setOnRefreshListener(this);

        tvs.clear();

        tvs.add((TextView)rootView.findViewById(R.id.today_min_in));
        tvs.add((TextView)rootView.findViewById(R.id.today_max_in));
        tvs.add((TextView)rootView.findViewById(R.id.today_min_out));
        tvs.add((TextView)rootView.findViewById(R.id.today_max_out));
        tvs.add((TextView)rootView.findViewById(R.id.tomorrow_in_same_time));
        tvs.add((TextView)rootView.findViewById(R.id.tomorrow_out_same_time));

        initTextViews();

        return rootView;
    }

    @SuppressWarnings("all")
    public void initTextViews(){

        Resources res = getResources();
        String[] strings = res.getStringArray(R.array.statistics);

        for(int i = 0; i < tvs.size(); i++){
            TextView tv = tvs.get(i);

            tv.setText(strings[i] + Utils.TEXTVIEW_SEPARATOR);
            tv.setTextSize(TV_TEXT_SIZE);
            tv.setPadding(TV_PADDING_H, TV_PADDING_V, TV_PADDING_H, TV_PADDING_V);
            tv.setGravity(Gravity.CLIP_HORIZONTAL);
        }
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

        updateView(tvs.get(0), minIn);
        updateView(tvs.get(1), maxIn);
        updateView(tvs.get(2), minOut);
        updateView(tvs.get(3), maxOut);
    }

    public void updateTomorrowStats(List<Temperature> temps){

        double inTemp = temps.get(temps.size() - 1).getInTemp();
        double outTemp = temps.get(temps.size() - 1).getOutTemp();

        double diff = inTemp - outTemp;

        double tomorrowTempOut = Utils.getWeatherForecastForTomorrowThisTime(getActivity());
        double tomorrowTempIn = tomorrowTempOut + diff;

        updateView(tvs.get(4), tomorrowTempIn);
        updateView(tvs.get(5), tomorrowTempOut);
    }

    @SuppressWarnings("all")
    public void updateView(TextView tv, double val){
        String boldTemp = "<b>" + df.format(val) + CELSIUS_DEGS + "</b>";
        tv.setText(Html.fromHtml(tv.getText().toString().split(Utils.TEXTVIEW_SEPARATOR)[0] + Utils.TEXTVIEW_SEPARATOR + boldTemp));
    }
}
