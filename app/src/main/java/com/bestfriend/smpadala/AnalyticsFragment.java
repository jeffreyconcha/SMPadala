package com.bestfriend.smpadala;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bestfriend.constant.RemittanceType;
import com.bestfriend.core.Data;
import com.bestfriend.core.LineGraph;
import com.bestfriend.model.AnalyticsData;
import com.codepan.app.CPFragment;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;

import org.achartengine.GraphicalView;

import java.util.ArrayList;

public class AnalyticsFragment extends CPFragment implements OnClickListener {

    private CodePanButton btnCalendarAnalytics, btnBackAnalytics;
    private ArrayList<AnalyticsData> transferList;
    private ArrayList<AnalyticsData> receiveList;
    private ArrayList<AnalyticsData> totalList;
    private FrameLayout flGraphAnalytics;
    private ImageView ivLoadingAnalytics;
    private String date;
    private MainActivity main;
    private Animation anim;
    private SQLiteAdapter db;
    private LineGraph line;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) getActivity();
        manager = main.getSupportFragmentManager();
        db = main.getDatabase();
        db.openConnection();
        Resources res = main.getResources();
        int theme = res.getColor(R.color.theme_sec);
        int gray = res.getColor(R.color.gray_pri);
        int red = res.getColor(R.color.red_pri);
        int six = res.getDimensionPixelSize(R.dimen.six);
        int seven = res.getDimensionPixelSize(R.dimen.seven);
        date = CodePanUtils.getDate();
        line = new LineGraph(main);
        line.setColor(theme, gray, red);
        line.setTextSize(seven, six, seven);
        line.setXY(23, 10);
        anim = AnimationUtils.loadAnimation(main, R.anim.rotate_clockwise);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analytics_layout, container, false);
        btnCalendarAnalytics = view.findViewById(R.id.btnCalendarAnalytics);
        btnBackAnalytics = view.findViewById(R.id.btnBackAnalytics);
        flGraphAnalytics = view.findViewById(R.id.flGraphAnalytics);
        ivLoadingAnalytics = view.findViewById(R.id.ivLoadingAnalytics);
        btnCalendarAnalytics.setOnClickListener(this);
        btnBackAnalytics.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showCalendar();
    }

    public void loadSalesToDate(final SQLiteAdapter db, final String date) {
        ivLoadingAnalytics.setVisibility(View.VISIBLE);
        ivLoadingAnalytics.startAnimation(anim);
        Thread bg = new Thread(() -> {
            try {
                totalList = Data.loadDataAnalytics(db, date, RemittanceType.DEFAULT);
                transferList = Data.loadDataAnalytics(db, date, RemittanceType.OUTGOING);
                receiveList = Data.loadDataAnalytics(db, date, RemittanceType.INCOMING);
                handler.obtainMessage().sendToTarget();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        ivLoadingAnalytics.setVisibility(View.GONE);
        ivLoadingAnalytics.clearAnimation();
        GraphicalView graph = line.getGraph(date, totalList, receiveList, transferList);
        flGraphAnalytics.removeAllViews();
        flGraphAnalytics.addView(graph);
        return true;
    });

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBackAnalytics:
                manager.popBackStack();
                break;
            case R.id.btnCalendarAnalytics:
                showCalendar();
                break;
        }
    }

    private void showCalendar() {
        CalendarDialogFragment calendar = new CalendarDialogFragment();
        calendar.setCurrentDate(date);
        calendar.setOnPickDateCallback(date -> {
            loadSalesToDate(db, date);
            this.date = date;
        });
        transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
            R.anim.fade_in, R.anim.fade_out);
        transaction.add(R.id.rlMain, calendar);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
