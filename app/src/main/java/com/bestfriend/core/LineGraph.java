package com.bestfriend.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.bestfriend.model.SalesToDateObj;
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

public class LineGraph {

	private int axisTextSize, lblTextSize, legendTextSize;
	private int totalColor, receiveColor, transferColor;
	private Context context;
	private int x, y;

	public LineGraph(Context context) {
		this.context = context;
	}

	public GraphicalView getGraph(String date, ArrayList<SalesToDateObj> totalList,
								  ArrayList<SalesToDateObj> receiveList,
								  ArrayList<SalesToDateObj> transferList) {
		String xTitle = CodePanUtils.getNameOfMonths(date, false);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(true);
		float maxAmount = 0;
		int minDay = 1;
		TimeSeries totalSeries = new TimeSeries("Total");
		for(SalesToDateObj std : totalList) {
			totalSeries.add(std.day, std.amount);
			if(std.amount > maxAmount) {
				maxAmount = std.amount;
			}
			if(std.isMin) {
				minDay = std.day;
			}
		}
		TimeSeries receiveSeries = new TimeSeries("Receive");
		for(SalesToDateObj std : receiveList) {
			receiveSeries.add(std.day, std.amount);
		}
		TimeSeries transferSeries = new TimeSeries("Transfer");
		for(SalesToDateObj std : transferList) {
			transferSeries.add(std.day, std.amount);
		}
		int maxDay = minDay + x;
		int size = totalList.size();
		maxDay = maxDay > size ? size : maxDay;
		minDay = minDay > x ? size - x : minDay;
		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
		dataSet.addSeries(totalSeries);
		dataSet.addSeries(receiveSeries);
		dataSet.addSeries(transferSeries);
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
		msRenderer.setMargins(new int[]{20, 80, 20, 20});
		msRenderer.setXLabels(0);
		msRenderer.setYLabels(0);
		msRenderer.setXAxisMax(maxDay, 0);
		msRenderer.setXAxisMin(minDay, 0);
		msRenderer.setYAxisMax(maxAmount, 0);
		msRenderer.setYAxisMin(0, 0);
		msRenderer.setShowLegend(true);
		msRenderer.setLegendTextSize(legendTextSize);
		msRenderer.setFitLegend(true);
		msRenderer.setYLabelsColor(0, Color.BLACK);
		msRenderer.setXLabelsColor(Color.BLACK);
		msRenderer.setAxesColor(Color.BLACK);
		msRenderer.addSeriesRenderer(totalRenderer);
		msRenderer.addSeriesRenderer(receiveRenderer);
		msRenderer.addSeriesRenderer(transferRenderer);
		msRenderer.setInScroll(true);
		msRenderer.setPointSize(4f);
		msRenderer.setShowCustomTextGrid(true);
		msRenderer.setYLabelsAlign(Paint.Align.RIGHT);
		msRenderer.setAxisTitleTextSize(axisTextSize);
		msRenderer.setPanLimits(new double[]{0.0, (double)
				size + 1, 0.0, (double) maxAmount});
		for(SalesToDateObj obj : totalList) {
			msRenderer.addXTextLabel(obj.day, String.valueOf(obj.day));
		}
		float interval = maxAmount / y;
		float increment = 0;
		for(int i = 1; i <= y; i++) {
			increment += interval;
			msRenderer.addYTextLabel(increment, nf.format(increment));
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
