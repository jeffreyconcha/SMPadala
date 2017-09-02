package com.bestfriend.smpadala;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bestfriend.adapter.RemittanceAdapter;
import com.bestfriend.callback.Interface.OnInitializeCallback;
import com.bestfriend.constant.Key;
import com.bestfriend.constant.Notification;
import com.bestfriend.constant.ProcessName;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.constant.RequestCode;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.RemittanceObj;
import com.codepan.callback.Interface.OnPermissionGrantedCallback;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanTextField;

import java.util.ArrayList;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class MainActivity extends FragmentActivity implements OnInitializeCallback, OnClickListener {

    private final int LIMIT = 200;
    private final long IDLE_TIME = 500;

    private OnPermissionGrantedCallback permissionGrantedCallback;
    private LinearLayout llMenuMain, llCustomersMain, llBackUpMain;
    private int visibleItem, totalItem, firstVisible;
    private ArrayList<RemittanceObj> remittanceList;
    private LocalBroadcastManager broadcastManager;
    private FragmentTransaction transaction;
    private CodePanTextField etSearchMain;
    private boolean isInitialized, isEnd;
    private String search, smDate, start;
    private Handler inputFinishHandler;
    private BroadcastReceiver receiver;
    private CodePanButton btnMenuMain;
    private RemittanceAdapter adapter;
    private FragmentManager manager;
    private DrawerLayout dlMain;
    private SQLiteAdapter db;
    private ListView lvMain;
    private long lastEdit;

    @Override
    protected void onStart() {
        super.onStart();
        if(isInitialized) {
            registerReceiver();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isInitialized) {
            unregisterReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        backUpData(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        this.manager = getSupportFragmentManager();
        this.inputFinishHandler = new Handler();
        llCustomersMain = (LinearLayout) findViewById(R.id.llCustomersMain);
        etSearchMain = (CodePanTextField) findViewById(R.id.etSearchMain);
        llBackUpMain = (LinearLayout) findViewById(R.id.llBackUpMain);
        btnMenuMain = (CodePanButton) findViewById(R.id.btnMenuMain);
        llMenuMain = (LinearLayout) findViewById(R.id.llMenuMain);
        dlMain = (DrawerLayout) findViewById(R.id.dlMain);
        lvMain = (ListView) findViewById(R.id.lvMain);
        btnMenuMain.setOnClickListener(this);
        llBackUpMain.setOnClickListener(this);
        llCustomersMain.setOnClickListener(this);
        int color = getResources().getColor(R.color.black_trans_twenty);
        dlMain.setScrimColor(color);
        lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch(scrollState) {
                    case SCROLL_STATE_TOUCH_SCROLL:
                        etSearchMain.clearFocus();
                        CodePanUtils.hideKeyboard(etSearchMain, MainActivity.this);
                        break;
                    case SCROLL_STATE_IDLE:
                        if(firstVisible == totalItem - visibleItem & !isEnd) {
                            loadMoreRemittance(db);
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                firstVisible = firstVisibleItem;
                visibleItem = visibleItemCount;
                totalItem = totalItemCount;
            }
        });
        etSearchMain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search = s.toString();
                lastEdit = System.currentTimeMillis();
                inputFinishHandler.removeCallbacks(inputFinishChecker);
                inputFinishHandler.postDelayed(inputFinishChecker, IDLE_TIME);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        init(savedInstanceState);
    }

    public void loadRemittance(final SQLiteAdapter db) {
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    remittanceList = Data.loadRemittance(db, search, smDate, start, LIMIT);
                    if(remittanceList.size() < LIMIT) {
                        isEnd = true;
                        start = null;
                    }
                    else {
                        isEnd = false;
                        int lastPosition = remittanceList.size() - 1;
                        start = remittanceList.get(lastPosition).ID;
                    }
                    loadRemittanceHandler.obtainMessage().sendToTarget();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });
        bg.start();
    }

    Handler loadRemittanceHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            updateRemittance(false);
            return true;
        }
    });

    public void loadMoreRemittance(final SQLiteAdapter db) {
        lvMain.setEnabled(false);
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<RemittanceObj> additionalList = Data.loadRemittance(db, search, smDate, start, LIMIT);
                    remittanceList.addAll(additionalList);
                    if(additionalList.size() < LIMIT) {
                        isEnd = true;
                        start = null;
                    }
                    else {
                        isEnd = false;
                        int lastPosition = additionalList.size() - 1;
                        start = additionalList.get(lastPosition).ID;
                    }
                    loadMoreRemittanceHandler.obtainMessage().sendToTarget();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });
        bg.start();
    }

    Handler loadMoreRemittanceHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            lvMain.setEnabled(true);
            updateRemittance(true);
            return true;
        }
    });

    public void updateRemittance(boolean isUpdate) {
        if(isUpdate) {
            adapter.notifyDataSetChanged();
            lvMain.invalidate();
        }
        else {
            adapter = new RemittanceAdapter(MainActivity.this, remittanceList);
            lvMain.setAdapter(adapter);
        }
    }

    public void init(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            if(isInitialized) {
                checkRevokedPermissions();
            }
            else {
                this.finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }
        else {
            SplashFragment splash = new SplashFragment();
            splash.setOnInitializeCallback(this);
            transaction = manager.beginTransaction();
            transaction.add(R.id.rlMain, splash);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void checkRevokedPermissions() {
        if(!CodePanUtils.isPermissionGranted(this)) {
            manager.popBackStack(null, POP_BACK_STACK_INCLUSIVE);
            Intent intent = new Intent(this, getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            this.finish();
        }
    }

    public void setOnPermissionGrantedCallback(OnPermissionGrantedCallback permissionGrantedCallback) {
        this.permissionGrantedCallback = permissionGrantedCallback;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch(requestCode) {
            case RequestCode.PERMISSION:
                if(grantResults.length > 0) {
                    boolean isPermissionGranted = true;
                    for(int result : grantResults) {
                        if(result == PackageManager.PERMISSION_DENIED) {
                            isPermissionGranted = false;
                            break;
                        }
                    }
                    if(permissionGrantedCallback != null) {
                        permissionGrantedCallback.onPermissionGranted(isPermissionGranted);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onInitialize(SQLiteAdapter db) {
        this.isInitialized = true;
        this.db = db;
        setReceiver();
        registerReceiver();
        loadRemittance(db);
    }

    public void registerReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter(RequestCode.NOTIFICATION);
        broadcastManager.registerReceiver((receiver), filter);
    }

    public void unregisterReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(receiver);
    }

    public void setReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra(Key.NOTIFICATION)) {
                    int code = intent.getIntExtra(Key.NOTIFICATION, 0);
                    switch(code) {
                        case Notification.SMS_RECEIVE:
                            loadRemittance(db);
                            break;
                    }
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnMenuMain:
                if(dlMain.isDrawerOpen(llMenuMain)) {
                    dlMain.closeDrawer(llMenuMain);
                }
                else {
                    dlMain.openDrawer(llMenuMain);
                }
                break;
            case R.id.llCustomersMain:
                dlMain.closeDrawer(llMenuMain);
                String dDate = CodePanUtils.getDate();
                String dTime = CodePanUtils.getTime();
                for(int i = 0; i < 5; i++) {
                    float amount = i * 20;
                    float balance = i * 1000;
                    int type = i % 2 != 0 ? RemittanceType.RECEIVE : RemittanceType.TRANSFER;
                    SMPadalaLib.saveRemittance(db, type, dDate, dTime, String.valueOf(amount), "11",
                            null, String.valueOf(balance), "jeff29vsacs23sf" + i);
                    loadRemittance(db);
                }
                break;
            case R.id.llBackUpMain:
                dlMain.closeDrawer(llMenuMain);
                AlertDialogFragment alert = new AlertDialogFragment();
                alert.setDialogTitle("Back-up Data");
                alert.setDialogMessage("This will back-up your data to external storage. " +
                        "Are you sure you want to back-up data?");
                alert.setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        manager.popBackStack();
                        backUpData(true);
                    }
                });
                alert.setNegativeButton("No", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        manager.popBackStack();
                    }
                });
                transaction = manager.beginTransaction();
                transaction.add(R.id.rlMain, alert);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }

    public void backUpData(final boolean external) {
        if(!CodePanUtils.isThreadRunning(ProcessName.BACK_UP_DB)) {
            Thread bg = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean result = SMPadalaLib.backUpData(MainActivity.this, external);
                        if(external && result) {
                            backUpDataHandler.obtainMessage().sendToTarget();
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            bg.setName(ProcessName.BACK_UP_DB);
            bg.start();
        }
    }

    Handler backUpDataHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message message) {
            CodePanUtils.alertToast(MainActivity.this, "Data has been successfully backed-up.");
            return true;
        }
    });

    private Runnable inputFinishChecker = new Runnable() {
        @Override
        public void run() {
            if(System.currentTimeMillis() > lastEdit + IDLE_TIME - 500) {
                loadRemittance(db);
            }
        }
    };
}
