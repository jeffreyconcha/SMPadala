package com.bestfriend.smpadala;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.bestfriend.cache.SQLiteCache;
import com.bestfriend.callback.Interface.OnInitializeCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.DialogTag;
import com.bestfriend.constant.RequestCode;
import com.codepan.callback.Interface.OnPermissionGrantedCallback;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;

import java.io.File;
import java.io.IOException;

public class SplashFragment extends Fragment implements OnPermissionGrantedCallback {

    private final int DELAY = 2000;
    private OnInitializeCallback initializeCallback;
    private FragmentTransaction transaction;
    private FragmentManager manager;
    private MainActivity main;
    private SQLiteAdapter db;
    private boolean isPause;

    @Override
    public void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isPause) {
            checkPermission();
        }
        isPause = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) getActivity();
        manager = main.getSupportFragmentManager();
        checkPermission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash_layout, container, false);
    }

    public void init() {
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    db = SQLiteCache.getDatabase(main, App.DB);
                    db.openConnection();
                    restoreBackup(db);
                    Thread.sleep(DELAY);
                    handler.sendMessage(handler.obtainMessage());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bg.start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(!isPause) {
                manager.popBackStack();
                if(initializeCallback != null) {
                    initializeCallback.onInitialize(db);
                }
            }
            return true;
        }
    });

    public void setOnInitializeCallback(OnInitializeCallback initializeCallback) {
        this.initializeCallback = initializeCallback;
    }

    public void checkPermission() {
        if(CodePanUtils.isPermissionGranted(main)) {
            if(CodePanUtils.isOnBackStack(main, DialogTag.PERMISSION)) {
                manager.popBackStack();
            }
            init();
        }
        else {
            if(CodePanUtils.isPermissionHidden(main)) {
                if(!CodePanUtils.isOnBackStack(main, DialogTag.PERMISSION)) {
                    showPermissionNote();
                }
            }
            else {
                main.setOnPermissionGrantedCallback(this);
                CodePanUtils.requestPermission(main, RequestCode.PERMISSION);
            }
        }
    }

    @Override
    public void onPermissionGranted(boolean isPermissionGranted) {
    }

    public void restoreBackup(SQLiteAdapter db) {
        String externalPath = Environment.getExternalStorageDirectory() + "/" + App.DB_BACKUP + "/" + App.DB;
        String internalPath = main.getDir(App.DB_BACKUP, Context.MODE_PRIVATE).getPath() + "/" + App.DB;
        File external = new File(externalPath);
        File internal = new File(internalPath);
        File src = external.exists() ? external : internal;
        if(src.exists()) {
            File dst = main.getDatabasePath(App.DB);
            try {
                db.close();
                CodePanUtils.copyFile(src, dst);
                this.db = SQLiteCache.getDatabase(main, App.DB);
                this.db.openConnection();
                if(src.getPath().equals(externalPath)) {
                    src.delete();
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showPermissionNote() {
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle(R.string.permission_title);
        alert.setDialogMessage(R.string.permission_message);
        alert.setPositiveButton("Settings", new OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.popBackStack();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + main.getPackageName()));
                main.startActivity(intent);
            }
        });
        alert.setNegativeButton("Exit", new OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.popBackStack();
                main.finish();
            }
        });
        transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);
        transaction.add(R.id.rlMain, alert, DialogTag.PERMISSION);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}