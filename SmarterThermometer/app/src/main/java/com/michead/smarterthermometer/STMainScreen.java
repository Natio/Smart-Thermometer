package com.michead.smarterthermometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Simone on 11/23/2015.
 */
public class STMainScreen extends Fragment implements   SwipeRefreshLayout.OnRefreshListener,
                                                        CompoundButton.OnCheckedChangeListener,
                                                        SeekBar.OnSeekBarChangeListener,
                                                        OnChartValueSelectedListener{

    private static final int MAX_SIZE = 100;
    private static final int MIN_SIZE = 10;

    private static ArrayList<Entry> tempInEntries = new ArrayList<>();
    private static ArrayList<Entry> tempOutEntries = new ArrayList<>();
    private static ArrayList<Entry> hTempInEntries = new ArrayList<>();
    private static ArrayList<Entry> hTempOutEntries = new ArrayList<>();

    private static List<Date> timestamps = new ArrayList<>();
    private static List<Date> hTimestamps = new ArrayList<>();

    private static List<String> xLabels = new ArrayList<>();
    private static List<LineDataSet> lineDataSets = new ArrayList<>();

    private static List<String> hXLabels = new ArrayList<>();
    private static List<LineDataSet> hLineDataSets = new ArrayList<>();

    private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private SwipeRefreshLayout srl;
    private LineChart lc;
    private Switch hSwitch;
    private SeekBar seekBar;

    private int currentSize = MAX_SIZE;
    private boolean isHourly = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.st_main_screen, container, false);

        srl = (SwipeRefreshLayout)rootView.findViewById(R.id.srl);
        srl.setOnRefreshListener(this);

        lc = (LineChart)rootView.findViewById(R.id.line_chart);
        initChart();

        hSwitch = (Switch)rootView.findViewById(R.id.h_switch);
        hSwitch.setChecked(isHourly);
        hSwitch.setOnCheckedChangeListener(this);

        seekBar = (SeekBar)rootView.findViewById(R.id.temp_size);
        seekBar.setMax(MAX_SIZE - MIN_SIZE);
        seekBar.setProgress(MAX_SIZE - MIN_SIZE);
        seekBar.setOnSeekBarChangeListener(this);

        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();

        fetchData();
        resizeChartData();
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

        xAxis.setValueFormatter(new TimestampFormatter());
        xAxis.setSpaceBetweenLabels(2);

        lc.setAutoScaleMinMaxEnabled(true);
        lc.setDescription("Temperature samples");
        lc.setOnChartValueSelectedListener(this);
        lc.setMaxVisibleValueCount(MIN_SIZE * 4);

        // xAxis.setDrawLabels(false);
        // yAxisL.setDrawLabels(false);
        // yAxisR.setDrawLabels(false);
    }

    public void fetchData(){
        refreshTemps(false);
        refreshHTemps(false);

        if (tempInEntries == null || tempOutEntries == null) refreshTemps(true);
        if (hTempInEntries == null || hTempOutEntries == null) refreshHTemps(true);

        refreshData();
        animateLabels();
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

    public void animateLabels(){
        lc.animateXY(3000, 3000);
    }

    public void refreshData(){

        hLineDataSets.clear();
        xLabels.clear();

        LineDataSet lineInHTemps = new LineDataSet((List)hTempInEntries.clone(), "HInsideTemps");
        LineDataSet lineOutHTemps = new LineDataSet((List)hTempOutEntries.clone(), "HOutsideTemps");

        initLineDataSet(lineInHTemps, R.color.red);
        initLineDataSet(lineOutHTemps, R.color.blue);

        hLineDataSets.add(lineInHTemps);
        hLineDataSets.add(lineOutHTemps);

        hXLabels = getXLabels(hTimestamps);


        lineDataSets.clear();
        xLabels.clear();

        LineDataSet lineInTemps = new LineDataSet((List)tempInEntries.clone(), "InsideTemps");
        LineDataSet lineOutTemps = new LineDataSet((List)tempOutEntries.clone(), "OutsideTemps");

        initLineDataSet(lineInTemps, R.color.red);
        initLineDataSet(lineOutTemps, R.color.blue);

        lineDataSets.add(lineInTemps);
        lineDataSets.add(lineOutTemps);

        xLabels = getXLabels(timestamps);

        resizeChartData();
    }

    public void initLineDataSet(LineDataSet lds, int color){
        lds.setColor(color);
        lds.setAxisDependency(YAxis.AxisDependency.LEFT);
    }

    public void invalidateChart(List<String> xLabels, List<LineDataSet> tempSets){
        LineData lineData = new LineData(xLabels, tempSets);
        lc.setData(lineData);
        lc.notifyDataSetChanged();
        lc.invalidate();
    }

    public synchronized void resizeChartData() {
        Logger.getAnonymousLogger().log(Level.INFO, "Changing data size to " + currentSize);

        if (currentSize < MIN_SIZE) currentSize = MIN_SIZE;
        if (currentSize > MAX_SIZE) currentSize = MAX_SIZE;

        if (!isHourly){
            int diff = currentSize - xLabels.size();

            Logger.getAnonymousLogger().log(Level.INFO, "Difference in size: " + diff);

            if (diff > 0) {
                int index = xLabels.size();
                while(xLabels.size() != currentSize){
                    xLabels.add(df.format(timestamps.get(index)));
                    lineDataSets.get(0).addEntry(tempInEntries.get(index));
                    lineDataSets.get(1).addEntry(tempOutEntries.get(index));
                    index++;
                }
            }
            else if (diff < 0){
                while(xLabels.size() != currentSize){
                    xLabels.remove(xLabels.size() - 1);
                    lineDataSets.get(0).removeLast();
                    lineDataSets.get(1).removeLast();
                }
            }

            lc.notifyDataSetChanged();
            invalidateChart(xLabels, lineDataSets);
        }
        else{
            int diff = currentSize - hXLabels.size();

            Logger.getAnonymousLogger().log(Level.INFO, "Difference in size: " + diff);

            if (diff > 0) {
                int index = hXLabels.size();
                while(hXLabels.size() != currentSize){
                    hXLabels.add(df.format(hTimestamps.get(index)));
                    hLineDataSets.get(0).addEntry(hTempInEntries.get(index));
                    hLineDataSets.get(1).addEntry(hTempOutEntries.get(index));
                    index++;
                }
            }
            else if (diff < 0){
                while(hXLabels.size() != currentSize){
                    hXLabels.remove(hXLabels.size() - 1);
                    hLineDataSets.get(0).removeLast();
                    hLineDataSets.get(1).removeLast();
                }
            }

            invalidateChart(hXLabels, hLineDataSets);
        }
    }

    public List<String> getXLabels(List<Date> timestamps){
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++)
            xLabels.add(df.format(timestamps.get(i)));

        return xLabels;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        isHourly = isChecked;
        resizeChartData();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentSize = progress;
        resizeChartData();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        String location = (h.getDataSetIndex() == 0)? "inside" : "outside";
        Date date = null;

        if (isHourly) date = hTimestamps.get(dataSetIndex);
        else date = timestamps.get(dataSetIndex);

        String fDate = df.format(date);
        String day = fDate.split("/")[1] + fDate.split("/")[0];
        String hour = fDate.split(" ")[1].split(":")[0] + fDate.split(" ")[1].split(":")[1];

        Toast.makeText(getActivity(), e.getVal() + "° " + location +
                " on " + day + " at " + hour, Toast.LENGTH_LONG);
    }

    @Override
    public void onNothingSelected() {

    }

    class TimestampFormatter implements XAxisValueFormatter {

        @Override
        public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {

            if (STMainScreen.this.isHourly){
                String[] tokens = original.split("/");
                return tokens[1] + "/" + tokens[0];
            }
            else{
                String[] tokens = original.split(" ")[1].split(":");
                return tokens[0] + ":" + tokens[1];
            }
        }
    }
}
