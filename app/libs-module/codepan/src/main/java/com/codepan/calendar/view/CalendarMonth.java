package com.codepan.calendar.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.codepan.R;
import com.codepan.calendar.adapter.CalendarMonthAdapter;
import com.codepan.calendar.callback.Interface.OnPickMonthCallback;
import com.codepan.calendar.model.MonthObj;

import java.util.ArrayList;

public class CalendarMonth extends Fragment {

	private OnPickMonthCallback pickMonthCallback;
	private ArrayList<MonthObj> monthList;
	private CalendarMonthAdapter adapter;
	private GridView gvCalendarMonth;
	private int height;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.calendar_month_layout, container, false);
		gvCalendarMonth = (GridView) view.findViewById(R.id.gvCalendarMonth);
		gvCalendarMonth.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				if(pickMonthCallback != null) {
					pickMonthCallback.onPickMonth(monthList.get(i));
				}
			}
		});
		adapter = new CalendarMonthAdapter(getActivity(), monthList, height);
		gvCalendarMonth.setAdapter(adapter);
		return view;
	}

	public void init(ArrayList<MonthObj> monthList, int height, OnPickMonthCallback pickMonthCallback) {
		this.monthList = monthList;
		this.pickMonthCallback = pickMonthCallback;
		this.height = height;
	}
}
