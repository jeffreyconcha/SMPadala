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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bestfriend.adapter.RemittanceAdapter;
import com.bestfriend.callback.Interface.OnInitializeCallback;
import com.bestfriend.constant.Key;
import com.bestfriend.constant.Notification;
import com.bestfriend.constant.RequestCode;
import com.bestfriend.core.Data;
import com.bestfriend.model.RemittanceObj;
import com.codepan.callback.Interface.OnPermissionGrantedCallback;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;

import java.util.ArrayList;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class MainActivity extends FragmentActivity implements OnInitializeCallback, OnClickListener {

    private OnPermissionGrantedCallback permissionGrantedCallback;
    private ArrayList<RemittanceObj> remittanceList;
    private LocalBroadcastManager broadcastManager;
    private FragmentTransaction transaction;
    private BroadcastReceiver receiver;
    private LinearLayout llMenuMain;
    private CodePanButton btnMenuMain;
    private RemittanceAdapter adapter;
    private FragmentManager manager;
    private boolean isInitialized;
    private DrawerLayout dlMain;
    private SQLiteAdapter db;
    private ListView lvMain;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        this.manager = getSupportFragmentManager();
        llMenuMain = (LinearLayout) findViewById(R.id.llMenuMain);
        btnMenuMain = (CodePanButton) findViewById(R.id.btnMenuMain);
        dlMain = (DrawerLayout) findViewById(R.id.dlMain);
        lvMain = (ListView) findViewById(R.id.lvMain);
        btnMenuMain.setOnClickListener(this);
        int color = getResources().getColor(R.color.black_trans_twenty);
        dlMain.setScrimColor(color);
        init(savedInstanceState);
    }

    public void loadRemittance(final SQLiteAdapter db) {
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    remittanceList = Data.loadRemittance(db);
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
            adapter = new RemittanceAdapter(MainActivity.this, remittanceList);
            lvMain.setAdapter(adapter);
            return true;
        }
    });

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
        }
    }
}
