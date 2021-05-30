package com.bestfriend.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.bestfriend.cache.SQLiteCache;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Key;
import com.bestfriend.constant.Notification;
import com.bestfriend.constant.Receiver;
import com.bestfriend.constant.RemittanceKey;
import com.bestfriend.constant.RequestCode;
import com.bestfriend.constant.Result;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.MessageData;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainService extends Service {

    private LocalBroadcastManager manager;
    private SQLiteAdapter db;

    @Override
    public void onCreate() {
        super.onCreate();
        manager = LocalBroadcastManager.getInstance(this);
        db = SQLiteCache.getDatabase(this, App.DB);
        db.openConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SQLiteAdapter db = getDatabase();
        if (intent != null && intent.hasExtra(Key.RECEIVER)) {
            handleReceiver(intent, db);
        }
        return START_STICKY;
    }

    public void handleReceiver(Intent intent, SQLiteAdapter db) {
        final int type = intent.getIntExtra(Key.RECEIVER, 0);
        if (type == Receiver.SMS_RECEIVER) {
            try {
                long timestamp = intent.getLongExtra(Key.TIMESTAMP, 0L);
                String sender = intent.getStringExtra(Key.SENDER);
                String text = intent.getStringExtra(Key.MESSAGE);
                String smDate = CodePanUtils.getDate(timestamp);
                String smTime = CodePanUtils.getTime(timestamp);
                if (sender != null && (sender.equalsIgnoreCase(RemittanceKey.SENDER_SP) ||
                    sender.equalsIgnoreCase(RemittanceKey.SENDER_SM) ||
                    sender.equalsIgnoreCase(RemittanceKey.SENDER_PM) ||
                    sender.equalsIgnoreCase(RemittanceKey.SENDER_PN) ||
                    sender.equalsIgnoreCase(RemittanceKey.SENDER_T1) ||
                    sender.equalsIgnoreCase(RemittanceKey.SENDER_T2) ||
                    sender.equalsIgnoreCase(RemittanceKey.SENDER_T3))) {
                    if (text != null) {
                        MessageData message = SMPadalaLib.scanMessage(text);
                        if (message != null) {
                            saveRemittance(db, message.type, smDate, smTime, message.amount,
                                message.charge, null, message.balance, message.referenceNo);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveRemittance(final SQLiteAdapter db, final int type, final String smDate,
                               final String smTime, final String amount, final String charge, final String mobileNo,
                               final String balance, final String referenceNo) {
        Thread bg = new Thread(() -> {
            try {
                boolean result = SMPadalaLib.saveRemittance(db, type, smDate, smTime, amount,
                        charge, mobileNo, balance, referenceNo);
                handler.obtainMessage(result ? Result.SUCCESS :
                        Result.FAILED).sendToTarget();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case Result.SUCCESS:
                sendBroadcast(Notification.SMS_RECEIVE);
                break;
            case Result.FAILED:
                break;
        }
        return true;
    });

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public SQLiteAdapter getDatabase() {
        if (db == null) {
            db = SQLiteCache.getDatabase(this, App.DB);
            db.openConnection();
        }
        return this.db;
    }

    public void sendBroadcast(int notification) {
        Intent intent = new Intent(RequestCode.NOTIFICATION);
        intent.putExtra(Key.NOTIFICATION, notification);
        manager.sendBroadcast(intent);
    }
}
