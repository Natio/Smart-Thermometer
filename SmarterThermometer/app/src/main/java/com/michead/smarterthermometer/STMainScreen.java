package com.michead.smarterthermometer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.LineDataProvider;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
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
                                                        SeekBar.OnSeekBarChangeListener,
                                                        OnChartValueSelectedListener,
                                                        OnChartGestureListener{

    private static final int MAX_SIZE = 24;
    private static final int MIN_SIZE = 12;
    private static final int ANIM_TIME = 2000;
    private static final int CIRCLE_SIZE = 5;
    private static final float LINE_WIDTH = 2f;
    private static final float CUBIC_INTENSITY = 0.2f;
    private static final int FILL_LINE_POSITION = -20;

    private static ArrayList<Entry> tempInEntries = new ArrayList<>();
    private static ArrayList<Entry> tempOutEntries = new ArrayList<>();

    private static List<Date> timestamps = new ArrayList<>();

    private static List<String> xLabels = new ArrayList<>();
    private static List<LineDataSet> lineDataSets = new ArrayList<>();


    private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private SwipeRefreshLayout srl;
    private LineChart lc;
    private SeekBar seekBar;

    private int currentSize = MAX_SIZE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.st_main_screen, container, false);

        srl = (SwipeRefreshLayout)rootView.findViewById(R.id.srl);
        srl.setOnRefreshListener(this);

        lc = (LineChart)rootView.findViewById(R.id.line_chart);
        initChart();

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

        // xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new TimestampFormatter());
        xAxis.setSpaceBetweenLabels(1);

        // yAxisL.setDrawGridLines(false);
        yAxisL.setValueFormatter(new TemperatureFormatter());

        lc.setAutoScaleMinMaxEnabled(true);
        lc.setDescription("");
        lc.setOnChartValueSelectedListener(this);
        lc.setMaxVisibleValueCount(0); // lc.setMaxVisibleValueCount(MIN_SIZE * 4);
        lc.setOnChartGestureListener(this);
        lc.setHighlightPerTapEnabled(true);
        lc.setDrawGridBackground(true);
        lc.setGridBackgroundColor(Color.argb(50, 0, 0, 0));
        // lc.setPinchZoom(true); // TODO Check this

        Legend legend = lc.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(R.color.blue);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
    }

    public void fetchData(){
        refreshTemps(false);

        if (tempInEntries == null || tempOutEntries == null) refreshTemps(true);

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

    public void animateLabels(){
        lc.animateX(ANIM_TIME);
    }

    public void refreshData(){

        lineDataSets.clear();
        xLabels.clear();

        LineDataSet lineInTemps = new LineDataSet((List)tempInEntries.clone(), "Temperature inside");
        LineDataSet lineOutTemps = new LineDataSet((List)tempOutEntries.clone(), "Temperature outside");

        initLineDataSet(lineInTemps, true);
        initLineDataSet(lineOutTemps, false);

        lineDataSets.add(lineInTemps);
        lineDataSets.add(lineOutTemps);

        xLabels = getXLabels(timestamps);

        resizeChartData();
    }

    public void initLineDataSet(LineDataSet lds, boolean isInside){

        if(isInside) {
            lds.setCircleColor(Color.RED);
            lds.setHighLightColor(Color.RED);
            lds.setColor(Color.RED);
            lds.setFillColor(Color.RED);
            lds.setFillAlpha(100);
        }
        else{
            lds.setCircleColor(Color.BLUE);
            lds.setHighLightColor(Color.BLUE);
            lds.setColor(Color.BLUE);
            lds.setFillColor(Color.BLUE);
            lds.setFillAlpha(100);
        }

        lds.setAxisDependency(YAxis.AxisDependency.LEFT);
        lds.setDrawFilled(true);
        lds.setCircleSize(CIRCLE_SIZE);
        lds.setDrawCubic(true);
        lds.setCubicIntensity(CUBIC_INTENSITY);
        lds.setDrawCircles(false);
        lds.setLineWidth(LINE_WIDTH);
        lds.setDrawHorizontalHighlightIndicator(false);
        lds.setDrawVerticalHighlightIndicator(false);
        lds.disableDashedHighlightLine();
        lds.setFillFormatter(new FillFormatter() {
            @Override
            public float getFillLinePosition(LineDataSet dataSet, LineDataProvider dataProvider) {
                return FILL_LINE_POSITION;
            }
        });
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

    public List<String> getXLabels(List<Date> timestamps){
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++)
            xLabels.add(df.format(timestamps.get(i)));

        return xLabels;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentSize = progress + MIN_SIZE;
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
        Date date = timestamps.get(dataSetIndex);

        String fDate = df.format(date);
        String day = fDate.split("/")[1] + "/" + fDate.split("/")[0];
        String hour = fDate.split(" ")[1].split(":")[0] + ":" + fDate.split(" ")[1].split(":")[1];

        Toast.makeText(getActivity(), String.format("%.2f", e.getVal()) + "° " + location +
                " on " + day + " at " + hour, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onChartGestureStart(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent motionEvent) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent motionEvent) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent motionEvent) {

    }

    @Override
    public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        // TODO
    }

    @Override
    public void onChartScale(MotionEvent motionEvent, float v, float v1) {
        // TODO
    }

    @Override
    public void onChartTranslate(MotionEvent motionEvent, float v, float v1) {

    }

    class TemperatureFormatter implements YAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, YAxis axis) {
            return (int)value + "°";
        }
    }

    class TimestampFormatter implements XAxisValueFormatter {

        @Override
        public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
            return original.split(" ")[1].split(":")[0];
        }
    }
}
