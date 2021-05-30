package com.bestfriend.smpadala;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bestfriend.cache.SQLiteCache;
import com.bestfriend.constant.App;
import com.codepan.app.CPSplashFragment;
import com.codepan.callback.Interface.OnInitializeCallback;
import com.codepan.database.SQLiteAdapter;

public class SplashFragment extends CPSplashFragment {

    private final int DELAY = 2000;
    private SQLiteAdapter db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash_layout, container, false);
    }

    @Override
    protected void onInitialize(OnInitializeCallback initializeCallback) {
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            if (!isPause) {
                manager.popBackStack();
                if (initializeCallback != null) {
                    initializeCallback.onInitialize(db);
                }
            }
            return true;
        });
        Thread bg = new Thread(() -> {
            try {
                db = SQLiteCache.getDatabase(activity, App.DB);
                db.openConnection();
                Thread.sleep(DELAY);
                handler.sendMessage(handler.obtainMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }
}