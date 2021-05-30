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
import com.bestfriend.model.SalesToDateData;
import com.codepan.app.CPFragment;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;

import org.achartengine.GraphicalView;

import java.util.ArrayList;

public class SalesToDateFragment extends CPFragment implements OnClickListener {

    private CodePanButton btnCalendarSalesToDate, btnBackSalesToDate;
    private ArrayList<SalesToDateData> transferList;
    private ArrayList<SalesToDateData> receiveList;
    private ArrayList<SalesToDateData> totalList;
    private FrameLayout flGraphSalesToDate;
    private ImageView ivLoadingSalesToDate;
    private String selectedDate;
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
        int theme = res.getColor(R.color.theme_ter);
        int gray = res.getColor(R.color.gray_pri);
        int red = res.getColor(R.color.red_pri);
        int six = res.getDimensionPixelSize(R.dimen.six);
        int seven = res.getDimensionPixelSize(R.dimen.seven);
        selectedDate = CodePanUtils.getDate();
        line = new LineGraph(main);
        line.setColor(theme, gray, red);
        line.setTextSize(seven, six, seven);
        line.setXY(23, 10);
        anim = AnimationUtils.loadAnimation(main, R.anim.rotate_clockwise);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sales_to_date_layout, container, false);
        btnCalendarSalesToDate = view.findViewById(R.id.btnCalendarSalesToDate);
        btnBackSalesToDate = view.findViewById(R.id.btnBackSalesToDate);
        flGraphSalesToDate = view.findViewById(R.id.flGraphSalesToDate);
        ivLoadingSalesToDate = view.findViewById(R.id.ivLoadingSalesToDate);
        btnCalendarSalesToDate.setOnClickListener(this);
        btnBackSalesToDate.setOnClickListener(this);
        showCalendar();
        return view;
    }

    public void loadSalesToDate(final SQLiteAdapter db, final String date) {
        ivLoadingSalesToDate.setVisibility(View.VISIBLE);
        ivLoadingSalesToDate.startAnimation(anim);
        Thread bg = new Thread(() -> {
            try {
                totalList = Data.loadSalesToDate(db, date, RemittanceType.DEFAULT);
                transferList = Data.loadSalesToDate(db, date, RemittanceType.OUTGOING);
                receiveList = Data.loadSalesToDate(db, date, RemittanceType.INCOMING);
                handler.obtainMessage().sendToTarget();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        ivLoadingSalesToDate.setVisibility(View.GONE);
        ivLoadingSalesToDate.clearAnimation();
        GraphicalView graph = line.getGraph(selectedDate, totalList, receiveList, transferList);
        flGraphSalesToDate.removeAllViews();
        flGraphSalesToDate.addView(graph);
        return true;
    });

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBackSalesToDate:
                manager.popBackStack();
                break;
            case R.id.btnCalendarSalesToDate:
                showCalendar();
                break;
        }
    }

    private void showCalendar() {
        CalendarDialogFragment calendar = new CalendarDialogFragment();
        calendar.setCurrentDate(selectedDate);
        calendar.setOnPickDateCallback(date -> {
            loadSalesToDate(db, date);
            selectedDate = date;
        });
        transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
            R.anim.fade_in, R.anim.fade_out);
        transaction.add(R.id.rlMain, calendar);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
