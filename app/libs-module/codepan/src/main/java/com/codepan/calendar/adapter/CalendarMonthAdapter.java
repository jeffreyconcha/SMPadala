package com.codepan.calendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.codepan.R;
import com.codepan.calendar.model.MonthObj;
import com.codepan.widget.CodePanLabel;

import java.util.ArrayList;

public class CalendarMonthAdapter extends ArrayAdapter<MonthObj> {

	private ArrayList<MonthObj> items;
	private LayoutInflater inflater;
	private int height;

	public CalendarMonthAdapter(Context context, ArrayList<MonthObj> items, int height) {
		super(context, 0, items);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		this.height = height;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		final MonthObj obj = items.get(position);
		if(obj != null) {
			if(convertView == null) {
				view = inflater.inflate(R.layout.calendar_month_item, parent, false);
				holder = new ViewHolder();
				holder.tvMonth = (CodePanLabel) view.findViewById(R.id.tvMonth);
				holder.tvMonth.setHeight(height);
				view.setTag(holder);
			}
			else {
				holder = (ViewHolder) view.getTag();
			}
			if(holder.tvMonth != null) {
				holder.tvMonth.setText(obj.name);
			}
		}
		return view;
	}

	private class ViewHolder {
		private CodePanLabel tvMonth;
	}
}
