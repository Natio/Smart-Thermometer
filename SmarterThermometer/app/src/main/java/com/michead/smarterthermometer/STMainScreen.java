package com.michead.smarterthermometer;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Simone on 11/23/2015.
 */
public class STMainScreen extends Fragment implements   SwipeRefreshLayout.OnRefreshListener,
                                                        OnChartValueSelectedListener {

    private static final int ANIM_TIME = 2000;
    private static final int CIRCLE_SIZE = 5;
    private static final float LINE_WIDTH = 2f;
    private static final float CUBIC_INTENSITY = 0.2f;
    private static final int FILL_LINE_POSITION = -20;
    private static final int FILL_ALPHA = 100;
    private static final int GRID_BACKGROUND_COLOR = Color.argb(50, 0, 0, 0);
    private static final int LABELS_TO_SKIP = 3;
    private static final int MAX_VISIBLE_VALUE_COUNT = 0;
    private static final String CHART_DESCRIPTION = "";

    private static ArrayList<Entry> tempInEntries = new ArrayList<>();
    private static ArrayList<Entry> tempOutEntries = new ArrayList<>();

    private static List<Date> timestamps = new ArrayList<>();

    private static List<String> xLabels = new ArrayList<>();
    private static List<LineDataSet> lineDataSets = new ArrayList<>();

    @SuppressWarnings("all")
    private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private SwipeRefreshLayout srl;
    private LineChart lc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.st_main_screen, container, false);

        srl = (SwipeRefreshLayout)rootView.findViewById(R.id.srl);
        srl.setOnRefreshListener(this);

        lc = (LineChart)rootView.findViewById(R.id.line_chart);

        initChart();
        fetchData(true);

        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();

        // Probably overkill
        // fetchData(true);
    }

    @Override
    public void onRefresh() {
        fetchData(false);
    }

    public void initChart(){
        XAxis xAxis = lc.getXAxis();
        YAxis yAxisL = lc.getAxisLeft();
        YAxis yAxisR = lc.getAxisRight();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new TimestampFormatter());
        xAxis.setLabelsToSkip(LABELS_TO_SKIP);

        yAxisL.setStartAtZero(false);
        yAxisL.setValueFormatter(new TemperatureFormatter());

        yAxisR.setValueFormatter(new NoValueFormatter());
        yAxisR.setDrawGridLines(false);

        lc.setAutoScaleMinMaxEnabled(true);
        lc.setDescription(CHART_DESCRIPTION);
        lc.setOnChartValueSelectedListener(this);
        lc.setMaxVisibleValueCount(MAX_VISIBLE_VALUE_COUNT);
        lc.setHighlightPerTapEnabled(true);
        lc.setDrawGridBackground(true);
        lc.setGridBackgroundColor(GRID_BACKGROUND_COLOR);
        lc.setDoubleTapToZoomEnabled(false);

        Legend legend = lc.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(R.color.blue);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
    }

    public void fetchData(boolean cached){

        if (!cached){
            DataStore.getInstance().getTemps(new ChartFindCallback());
            return;
        }

        refreshTemps();

        refreshData();
        animateLabels();
    }

    public void refreshTemps (){

        List<Temperature> temps = DataStore.getInstance().getCachedTemps();

        if (temps == null){
            fetchData(false);
            return;
        }

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

        srl.setRefreshing(false);
    }

    public void animateLabels(){
        lc.animateX(ANIM_TIME);
    }

    public void refreshData(){

        Resources res = getResources();
        String[] legend_strings = res.getStringArray(R.array.chart);

        lineDataSets.clear();
        xLabels.clear();

        LineDataSet lineInTemps = new LineDataSet(tempInEntries, legend_strings[0]);
        LineDataSet lineOutTemps = new LineDataSet(tempOutEntries, legend_strings[1]);

        initLineDataSet(lineInTemps, true);
        initLineDataSet(lineOutTemps, false);

        lineDataSets.add(lineInTemps);
        lineDataSets.add(lineOutTemps);

        xLabels = getXLabels(timestamps);

        invalidateChart(xLabels, lineDataSets);
    }

    public void initLineDataSet(LineDataSet lds, boolean isInside){

        if(isInside) {
            lds.setCircleColor(Color.RED);
            lds.setHighLightColor(Color.RED);
            lds.setColor(Color.RED);
            lds.setFillColor(Color.RED);
            lds.setFillAlpha(FILL_ALPHA);
        }
        else{
            lds.setCircleColor(Color.BLUE);
            lds.setHighLightColor(Color.BLUE);
            lds.setColor(Color.BLUE);
            lds.setFillColor(Color.BLUE);
            lds.setFillAlpha(FILL_ALPHA);
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

    public List<String> getXLabels(List<Date> timestamps){
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++)
            xLabels.add(df.format(timestamps.get(i)));

        return xLabels;
    }

    @SuppressWarnings("all")
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        String location = (h.getDataSetIndex() == 0)? "inside" : "outside";
        Date date = timestamps.get(dataSetIndex);

        String fDate = df.format(date);
        String day = fDate.split("/")[1] + "/" + fDate.split("/")[0];
        String hour = fDate.split(" ")[1].split(":")[0] + ":" + fDate.split(" ")[1].split(":")[1];

        Toast.makeText(getActivity(), String.format("%.2f", e.getVal()) + "° " + location +
                " on " + day + " at " + hour, Toast.LENGTH_SHORT); // .show(); // TODO Check what to do with this
    }

    @Override
    public void onNothingSelected() {

    }

    class TemperatureFormatter implements YAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, YAxis axis) {
            return (int)value + "°";
        }
    }

    class NoValueFormatter implements YAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, YAxis axis) {
            return "";
        }
    }

    class TimestampFormatter implements XAxisValueFormatter {

        @Override
        public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
            return Integer.parseInt(original.split(" ")[1].split(":")[0]) + "h";
        }
    }

    class ChartFindCallback implements FindCallback<Temperature>{

        @Override
        public void done(List<Temperature> objects, ParseException e) {
            Collections.reverse(objects);
            DataStore.getInstance().setTemps(objects);
            fetchData(true);
        }
    }
}
