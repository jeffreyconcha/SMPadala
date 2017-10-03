package com.bestfriend.smpadala;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bestfriend.constant.RemittanceType;
import com.bestfriend.core.Data;
import com.bestfriend.core.LineGraph;
import com.bestfriend.model.SalesToDateObj;
import com.codepan.database.SQLiteAdapter;

import org.achartengine.GraphicalView;

import java.util.ArrayList;

public class SalesToDateFragment extends Fragment {

	private ArrayList<SalesToDateObj> transferList;
	private ArrayList<SalesToDateObj> receiveList;
	private ArrayList<SalesToDateObj> totalList;
	private FrameLayout flSalesToDate;
	private FragmentManager manager;
	private MainActivity main;
	private SQLiteAdapter db;
	private LineGraph line;
	private String date;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = (MainActivity) getActivity();
		manager = main.getSupportFragmentManager();
		db = main.getDatabase();
		db.openConnection();
		Resources res = main.getResources();
		int theme = res.getColor(R.color.theme_ter);
		int gray = res.getColor(R.color.gray_pri);
		int red = res.getColor(R.color.red_pri);
		int six = res.getDimensionPixelSize(R.dimen.six);
		int seven = res.getDimensionPixelSize(R.dimen.seven);
		date = "2017-09-01";
		line = new LineGraph(main);
		line.setColor(theme, gray, red);
		line.setTextSize(seven, six, seven);
		line.setXY(20, 10);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sales_to_date_layout, container, false);
		flSalesToDate = (FrameLayout) view.findViewById(R.id.flSalesToDate);
		loadSalesToDate(db, date);
		return view;
	}

	public void loadSalesToDate(final SQLiteAdapter db, final String date) {
		Thread bg = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					totalList = Data.loadSalesToDate(db, date, RemittanceType.DEFAULT);
					transferList = Data.loadSalesToDate(db, date, RemittanceType.TRANSFER);
					receiveList = Data.loadSalesToDate(db, date, RemittanceType.RECEIVE);
					handler.obtainMessage().sendToTarget();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		bg.start();
	}

	Handler handler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			GraphicalView graph = line.getGraph(date, totalList, receiveList, transferList);
			flSalesToDate.removeAllViews();
			flSalesToDate.addView(graph);
			return true;
		}
	});
}
