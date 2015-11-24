package com.michead.smarterthermometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Simone on 11/23/2015.
 */
public class STMainScreen extends Fragment implements   SwipeRefreshLayout.OnRefreshListener,
                                                        CompoundButton.OnCheckedChangeListener,
                                                        SeekBar.OnSeekBarChangeListener {

    private static List<Entry> tempInEntries = new ArrayList<>();
    private static List<Entry> tempOutEntries = new ArrayList<>();
    private static List<Entry> hTempInEntries = new ArrayList<>();
    private static List<Entry> hTempOutEntries = new ArrayList<>();

    private static List<Date> timestamps = new ArrayList<>();
    private static List<Date> hTimestamps = new ArrayList<>();

    private SwipeRefreshLayout srl;
    private LineChart lc;
    private Switch hSwitch;
    private SeekBar seekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.st_main_screen, container, false);

        srl = (SwipeRefreshLayout)rootView.findViewById(R.id.srl);
        srl.setOnRefreshListener(this);

        lc = (LineChart)rootView.findViewById(R.id.line_chart);
        initChart();

        hSwitch = (Switch)rootView.findViewById(R.id.h_switch);
        hSwitch.setOnCheckedChangeListener(this);

        seekBar = (SeekBar)rootView.findViewById(R.id.temp_size);
        seekBar.setOnSeekBarChangeListener(this);
        // seekBar.setMax(80);

        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();

        fetchData();
    }

    @Override
    public void onRefresh() {
        fetchData();
        srl.setRefreshing(false);
    }

    public void initChart(){
        XAxis xAxis = lc.getXAxis();
        YAxis yAxisL = lc.getAxisLeft();
        YAxis yAxisR = lc.getAxisRight();

        xAxis.setDrawGridLines(false);
        yAxisL.setDrawGridLines(false);
        yAxisR.setDrawGridLines(false);

        // xAxis.setDrawLabels(false);
        // yAxisL.setDrawLabels(false);
        // yAxisR.setDrawLabels(false);
    }

    public void fetchData(){
        refreshTemps(false);
        refreshHTemps(false);

        if (tempInEntries == null || tempOutEntries == null) refreshTemps(true);
        if (hTempInEntries == null || hTempOutEntries == null) refreshHTemps(true);

        refreshChart(false, 100);
    }

    public void refreshTemps (boolean useCache){
        List<Temperature> temps = null;

        if (useCache) temps = DataStore.getInstance().getCachedTemps();
        else temps = DataStore.getInstance().getTempsInRange();

        tempInEntries.clear();
        tempOutEntries.clear();

        timestamps.clear();

        if (temps == null) return;

        int i = 0;
        for (Temperature temp : temps){
            tempInEntries.add(new Entry((float)temp.getInTemp(), i));
            tempOutEntries.add(new Entry((float)temp.getOutTemp(), i));

            timestamps.add(temp.getTimestamp());

            i++;
        }
    }

    public void refreshHTemps (boolean useCache){
        List<HTemperature> temps = null;

        if (useCache) temps = DataStore.getInstance().getCachedHTemps();
        else temps = DataStore.getInstance().getHTempsInRange();

        hTempInEntries.clear();
        hTempOutEntries.clear();

        hTimestamps.clear();

        if (temps == null) return;

        int i = 0;
        for (HTemperature temp : temps){
            hTempInEntries.add(new Entry((float)temp.getInTemp(), i));
            hTempOutEntries.add(new Entry((float)temp.getOutTemp(), i));

            hTimestamps.add(temp.getTimestamp());

            i++;
        }
    }

    public void refreshChart(boolean hOnly, int size){
        ArrayList<LineDataSet> tempSets = new ArrayList<>();
        List<String> xLabels = null;

        if (size > tempInEntries.size()) size = tempInEntries.size();

        if (hOnly){
            LineDataSet lineInHTemps = new LineDataSet(hTempInEntries.subList(0, size), "HInsideTemps");
            LineDataSet lineOutHTemps = new LineDataSet(hTempOutEntries.subList(0, size), "HOutsideTemps");

            lineInHTemps.setColor(R.color.red);
            lineOutHTemps.setColor(R.color.blue);

            lineInHTemps.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineOutHTemps.setAxisDependency(YAxis.AxisDependency.LEFT);

            tempSets.add(lineInHTemps);
            tempSets.add(lineOutHTemps);

            xLabels = getXLabels(hTimestamps);
        }
        else{
            LineDataSet lineInTemps = new LineDataSet(tempInEntries.subList(0, size), "InsideTemps");
            LineDataSet lineOutTemps = new LineDataSet(tempOutEntries.subList(0, size), "OutsideTemps");

            lineInTemps.setColor(R.color.red);
            lineOutTemps.setColor(R.color.blue);

            lineInTemps.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineOutTemps.setAxisDependency(YAxis.AxisDependency.LEFT);

            tempSets.add(lineInTemps);
            tempSets.add(lineOutTemps);

            xLabels = getXLabels(timestamps);
        }

        LineData lineData = new LineData(xLabels, tempSets);
        lc.setData(lineData);
        lc.invalidate();
    }

    public List<String> getXLabels(List<Date> timestamps){
        List<String> xLabels = new ArrayList<>();

        // TODO This is just a stub
        for (int i = 0; i < timestamps.size(); i++)
            xLabels.add("");

        return xLabels;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        refreshChart(isChecked, seekBar.getProgress() + 20);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        refreshChart(hSwitch.isChecked(), progress + 20);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
