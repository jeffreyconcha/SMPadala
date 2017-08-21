package com.codepan.calendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.codepan.R;
import com.codepan.calendar.model.DayObj;
import com.codepan.widget.CodePanLabel;

import java.util.ArrayList;

public class CalendarDayAdapter extends ArrayAdapter<DayObj> {

	private int inActive, active, selected;
	private ArrayList<DayObj> items;
	private LayoutInflater inflater;
	private ViewGroup parent;

	public CalendarDayAdapter(Context context, ArrayList<DayObj> items) {
		super(context, 0, items);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.inActive = context.getResources().getColor(R.color.cal_day_inactive);
		this.active = context.getResources().getColor(R.color.cal_day_active);
		this.selected = context.getResources().getColor(R.color.cal_day_selected);
		this.items = items;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		final DayObj obj = items.get(position);
		if(obj != null) {
			if(convertView == null) {
				view = inflater.inflate(R.layout.calendar_day_item, parent, false);
				holder = new ViewHolder();
				holder.tvDay = (CodePanLabel) view.findViewById(R.id.tvDay);
				if(position == 0) {
					this.parent = (ViewGroup) holder.tvDay.getParent();
				}
				view.setTag(holder);
			}
			else {
				holder = (ViewHolder) view.getTag();
			}
			if(holder.tvDay != null) {
				holder.tvDay.setText(String.valueOf(obj.ID));
				if(obj.isSelect) {
					holder.tvDay.setBackgroundResource(R.drawable.state_oval_cal_selected);
					holder.tvDay.setTextColor(selected);
				}
				else {
					holder.tvDay.setBackgroundResource(R.drawable.state_oval_trans_dark);
					if(obj.isActive) {
						holder.tvDay.setTextColor(active);
					}
					else {
						holder.tvDay.setTextColor(inActive);
					}
				}
			}
		}
		return view;
	}

	private class ViewHolder {
		private CodePanLabel tvDay;
	}

	public ViewGroup getParent() {
		return this.parent;
	}
}
