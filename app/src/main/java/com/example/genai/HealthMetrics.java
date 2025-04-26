package com.example.genai;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class HealthMetrics {
    private final LineChart chart;
    private final ArrayDeque<Float> metricQueue = new ArrayDeque<>(60);
    private final String metricLabel;
    private final int lineColor;
    private int entryIndex = 0;

    public HealthMetrics(Context context, LineChart chart, String metricLabel, int lineColor, boolean isBigChart) {
        this.chart = chart;
        this.metricLabel = metricLabel;
        this.lineColor = lineColor;

        if(!isBigChart){
            setupChart();
        }else{
            setupBigChart();
        }
    }
    private void setupChart() {
        if (chart == null) return;

        // Basic configuration
        chart.setDescription(null);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.TRANSPARENT);

        // Remove legend
        chart.getLegend().setEnabled(false);

        // Configure X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setDrawLabels(false); // Remove X-axis labels

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        chart.getAxisRight().setEnabled(false);

        // Set Y-axis range based on metric
        switch (metricLabel) {
            case "Heart Rate (BPM)":
                leftAxis.setAxisMinimum(50f);
                leftAxis.setAxisMaximum(110f);
                break;
            case "Systolic BP (mmHg)":
                leftAxis.setAxisMinimum(90f);
                leftAxis.setAxisMaximum(160f);
                break;
            case "Temperature (°C)":
                leftAxis.setAxisMinimum(35f);
                leftAxis.setAxisMaximum(40f);
                break;
            case "Blood Oxygen (%)":
                leftAxis.setAxisMinimum(90f);
                leftAxis.setAxisMaximum(105f);
                break;
        }

        // Initialize empty data
        LineData data = new LineData();
        chart.setData(data);

        chart.invalidate();
    }

    private void setupBigChart() {
        if (chart == null) return;

        chart.setDescription(null);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setExtraOffsets(10f, 10f, 10f, 10f);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setGridLineWidth(0.5f);
        xAxis.setGranularity(1f);
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setDrawLabels(true);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(10f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f", value);
            }
        });

        // Disable right Y-axis
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        switch (metricLabel) {
            case "Heart Rate (BPM)":
                leftAxis.setAxisMinimum(50f);
                leftAxis.setAxisMaximum(110f);
                leftAxis.setLabelCount(4, true);
                break;
            case "Systolic BP (mmHg)":
                leftAxis.setAxisMinimum(90f);
                leftAxis.setAxisMaximum(160f);
                leftAxis.setLabelCount(4, true);
                break;
            case "Temperature (°C)":
                leftAxis.setAxisMinimum(35f);
                leftAxis.setAxisMaximum(40f);
                leftAxis.setLabelCount(4, true);
                break;
            case "Blood Oxygen (%)":
                leftAxis.setAxisMinimum(90f);
                leftAxis.setAxisMaximum(105f);
                leftAxis.setLabelCount(4, true);
                break;
            default:
                leftAxis.setAxisMinimum(0f);
                leftAxis.setAxisMaximum(100f);
                break;
        }

        // Initialize empty data
        LineData data = new LineData();
        chart.setData(data);

        // Add animation
        chart.animateY(1000, Easing.EaseInOutQuad); // Animate Y-axis over 1 second

        chart.setHighlightPerTapEnabled(true);
        chart.setHighlightPerDragEnabled(true);

        chart.invalidate();
    }

    public void updateChart(float value) {
        if (chart == null) return;

        // Update queue
        metricQueue.offer(value);
        if (metricQueue.size() > 60) metricQueue.poll();

        // Update chart data
        LineData data = chart.getData();
        if (data != null) {
            LineDataSet dataSet = (LineDataSet) data.getDataSetByIndex(0);
            if (dataSet == null) {
                dataSet = new LineDataSet(null, metricLabel);
                dataSet.setColor(lineColor);
                dataSet.setLineWidth(2f);
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);
                data.addDataSet(dataSet);
            }
            dataSet.setValues(createEntries());

            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.moveViewToX(entryIndex);
            chart.invalidate();
        }
        entryIndex++;
    }

    private ArrayList<Entry> createEntries() {
        ArrayList<Entry> entries = new ArrayList<>();
        int index = entryIndex - metricQueue.size();
        for (Float value : metricQueue) {
            entries.add(new Entry(index++, value));
        }
        return entries;
    }
}