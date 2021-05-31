package com.bestfriend.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.bestfriend.model.AnalyticsData;
import com.codepan.utils.CodePanUtils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class LineGraph {

    private int axisTextSize, lblTextSize, legendTextSize;
    private int totalColor, receiveColor, transferColor;
    private final Context context;
    private int x, y;

    public LineGraph(Context context) {
        this.context = context;
    }

    public GraphicalView getGraph(String date, ArrayList<AnalyticsData> totalList,
            ArrayList<AnalyticsData> receiveList,
            ArrayList<AnalyticsData> transferList) {
        Calendar calendar = CodePanUtils.getCalendar(date);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        String year = CodePanUtils.getDisplayYear(date);
        String xTitle = month + " " + year;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(true);
        float maxAmount = 0;
        int minDay = 1;
        TimeSeries totalSeries = new TimeSeries("Total");
        for(AnalyticsData std : totalList) {
            totalSeries.add(std.day, std.amount);
            if(std.amount > maxAmount) {
                maxAmount = std.amount;
            }
            if(std.isMin) {
                minDay = std.day;
            }
        }
        float interval = maxAmount / y;
        TimeSeries receiveSeries = new TimeSeries("Receive");
        for(AnalyticsData std : receiveList) {
            receiveSeries.add(std.day, std.amount);
        }
        TimeSeries transferSeries = new TimeSeries("Transfer");
        for(AnalyticsData std : transferList) {
            transferSeries.add(std.day, std.amount);
        }
        int maxDay = minDay + x;
        int size = totalList.size();
        maxDay = Math.min(maxDay, size);
        minDay = minDay > x ? size - x : maxDay - x;
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        dataSet.addSeries(transferSeries);
        dataSet.addSeries(receiveSeries);
        dataSet.addSeries(totalSeries);
        XYSeriesRenderer totalRenderer = new XYSeriesRenderer();
        totalRenderer.setColor(totalColor);
        totalRenderer.setFillPoints(true);
        totalRenderer.setPointStyle(PointStyle.CIRCLE);
        XYSeriesRenderer receiveRenderer = new XYSeriesRenderer();
        receiveRenderer.setColor(receiveColor);
        receiveRenderer.setFillPoints(true);
        receiveRenderer.setPointStyle(PointStyle.CIRCLE);
        XYSeriesRenderer transferRenderer = new XYSeriesRenderer();
        transferRenderer.setColor(transferColor);
        transferRenderer.setFillPoints(true);
        transferRenderer.setPointStyle(PointStyle.CIRCLE);
        XYMultipleSeriesRenderer msRenderer = new XYMultipleSeriesRenderer();
        msRenderer.setBackgroundColor(Color.WHITE);
        msRenderer.setApplyBackgroundColor(true);
        msRenderer.setYTitle("Amount");
        msRenderer.setXTitle(xTitle);
        msRenderer.setLabelsTextSize(lblTextSize);
        msRenderer.setGridColor(Color.rgb(200, 200, 200));
        msRenderer.setPanEnabled(true, true);
        msRenderer.setLabelsColor(Color.BLACK);
        msRenderer.setMarginsColor(Color.WHITE);
        msRenderer.setMargins(new int[]{20, 100, 20, 20});
        msRenderer.setXLabels(0);
        msRenderer.setYLabels(0);
        msRenderer.setXAxisMax(maxDay, 0);
        msRenderer.setXAxisMin(minDay, 0);
        msRenderer.setYAxisMax(maxAmount + interval, 0);
        msRenderer.setYAxisMin(0, 0);
        msRenderer.setShowLegend(true);
        msRenderer.setLegendTextSize(legendTextSize);
        msRenderer.setFitLegend(true);
        msRenderer.setYLabelsColor(0, Color.BLACK);
        msRenderer.setXLabelsColor(Color.BLACK);
        msRenderer.setAxesColor(Color.BLACK);
        msRenderer.addSeriesRenderer(transferRenderer);
        msRenderer.addSeriesRenderer(receiveRenderer);
        msRenderer.addSeriesRenderer(totalRenderer);
        msRenderer.setInScroll(true);
        msRenderer.setPointSize(4f);
        msRenderer.setShowCustomTextGrid(true);
        msRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        msRenderer.setAxisTitleTextSize(axisTextSize);
        msRenderer.setPanLimits(new double[]{0.0, (double)
                size + 1, 0.0, (double) maxAmount});
        for(AnalyticsData data : totalList) {
            msRenderer.addXTextLabel(data.day, String.valueOf(data.day));
        }
        float increment = 0;
        for(int i = 1; i <= y; i++) {
            increment += interval;
            String amount = nf.format(increment) + " ";
            msRenderer.addYTextLabel(increment, amount);
        }
        GraphicalView view = ChartFactory.getLineChartView(context, dataSet, msRenderer);
        view.refreshDrawableState();
        view.repaint();
        return view;
    }

    public void setColor(int totalColor, int receiveColor, int transferColor) {
        this.totalColor = totalColor;
        this.receiveColor = receiveColor;
        this.transferColor = transferColor;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setTextSize(int axisTextSize, int lblTextSize, int legendTextSize) {
        this.axisTextSize = axisTextSize;
        this.lblTextSize = lblTextSize;
        this.legendTextSize = legendTextSize;
    }
}
