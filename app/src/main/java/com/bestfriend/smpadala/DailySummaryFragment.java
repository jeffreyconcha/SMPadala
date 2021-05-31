package com.bestfriend.smpadala;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.bestfriend.adapter.DailySummaryAdapter;
import com.bestfriend.core.Data;
import com.bestfriend.model.DailySummaryData;
import com.codepan.app.CPFragment;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DailySummaryFragment extends CPFragment implements OnClickListener, OnScrollListener {

	private CodePanButton btnBackDailySummary, btnCalendarDailySummary;
	private int visibleItem, totalItem, firstVisible;
	private ArrayList<DailySummaryData> summaryList;
	private DailySummaryAdapter adapter;
	private ListView lvDailySummary;
	private MainActivity main;
	private SQLiteAdapter db;
	private String date;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = (MainActivity) getActivity();
		db = main.getDatabase();
		date = CodePanUtils.getDate();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.daily_summary_layout, container, false);
		btnBackDailySummary = view.findViewById(R.id.btnBackDailySummary);
		btnCalendarDailySummary = view.findViewById(R.id.btnCalendarDailySummary);
		lvDailySummary = view.findViewById(R.id.lvDailySummary);
		btnBackDailySummary.setOnClickListener(this);
		btnCalendarDailySummary.setOnClickListener(this);
		lvDailySummary.setOnScrollListener(this);
		loadDailySummary(db, date);
		return view;
	}

	private void loadDailySummary(final SQLiteAdapter db, final String date) {
		final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
			adapter = new DailySummaryAdapter(main, summaryList);
			lvDailySummary.setAdapter(adapter);
			return true;
		});
		final Thread bg = new Thread(() -> {
			try {
				Looper.prepare();
				summaryList = Data.loadDailySummary(db, date);
				handler.obtainMessage().sendToTarget();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		});
		bg.start();
	}

	private void loadMoreDailySummary(final SQLiteAdapter db) {
		final int lastIndex = summaryList.size() - 1;
		final DailySummaryData last = summaryList.get(lastIndex);
		final String date = CodePanUtils.rollDate(last.date, -1);
		final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
			adapter.notifyDataSetChanged();
			lvDailySummary.invalidate();
			return true;
		});
		final Thread bg = new Thread(() -> {
			try {
				Looper.prepare();
				ArrayList<DailySummaryData> additionalList = Data.loadDailySummary(db, date);
				summaryList.addAll(additionalList);
				handler.obtainMessage().sendToTarget();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		});
		bg.start();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnBackDailySummary:
				onBackPressed();
				break;
			case R.id.btnCalendarDailySummary:
				CalendarDialogFragment calendar = new CalendarDialogFragment();
				calendar.setCurrentDate(date);
				calendar.setOnPickDateCallback(date -> {
					loadDailySummary(db, date);
					this.date = date;
				});
				transaction = manager.beginTransaction();
				transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
					R.anim.fade_in, R.anim.fade_out);
				transaction.add(R.id.rlMain, calendar);
				transaction.addToBackStack(null);
				transaction.commit();
				break;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == SCROLL_STATE_IDLE) {
			if(firstVisible == totalItem - visibleItem) {
				loadMoreDailySummary(db);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisible,
		int visibleItem, int totalItem) {
		this.firstVisible = firstVisible;
		this.visibleItem = visibleItem;
		this.totalItem = totalItem;
	}
}
