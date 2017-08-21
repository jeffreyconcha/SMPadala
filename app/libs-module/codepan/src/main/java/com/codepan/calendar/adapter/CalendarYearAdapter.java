package com.codepan.calendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.codepan.R;
import com.codepan.calendar.model.YearObj;
import com.codepan.widget.CodePanLabel;

import java.util.ArrayList;

public class CalendarYearAdapter extends ArrayAdapter<YearObj> {

	private ArrayList<YearObj> items;
	private LayoutInflater inflater;
	private int height;

	public CalendarYearAdapter(Context context, ArrayList<YearObj> items, int height) {
		super(context, 0, items);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		this.height = height;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		final YearObj obj = items.get(position);
		if(obj != null) {
			if(convertView == null) {
				view = inflater.inflate(R.layout.calendar_year_item, parent, false);
				holder = new ViewHolder();
				holder.tvYear = (CodePanLabel) view.findViewById(R.id.tvYear);
				holder.tvYear.setHeight(height);
				view.setTag(holder);
			}
			else {
				holder = (ViewHolder) view.getTag();
			}
			if(holder.tvYear != null) {
				holder.tvYear.setText(obj.name);
			}
		}
		return view;
	}

	private class ViewHolder {
		private CodePanLabel tvYear;
	}
}
