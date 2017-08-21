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
import com.codepan.calendar.adapter.CalendarYearAdapter;
import com.codepan.calendar.callback.Interface.OnPickYearCallback;
import com.codepan.calendar.model.YearObj;

import java.util.ArrayList;

public class CalendarYear extends Fragment {

	private OnPickYearCallback pickYearCallback;
	private ArrayList<YearObj> yearList;
	private CalendarYearAdapter adapter;
	private GridView gvCalendarYear;
	private int height;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.calendar_year_layout, container, false);
		gvCalendarYear = (GridView) view.findViewById(R.id.gvCalendarYear);
		gvCalendarYear.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				if(pickYearCallback != null) {
					pickYearCallback.onPickYear(yearList.get(i));
				}
			}
		});
		adapter = new CalendarYearAdapter(getActivity(), yearList, height);
		gvCalendarYear.setAdapter(adapter);
		return view;
	}

	public void init(ArrayList<YearObj> yearList, int height, OnPickYearCallback pickYearCallback) {
		this.yearList = yearList;
		this.pickYearCallback = pickYearCallback;
		this.height = height;
	}
}
