package com.bestfriend.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bestfriend.cache.SQLiteCache;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Key;
import com.bestfriend.constant.Notification;
import com.bestfriend.constant.Receiver;
import com.bestfriend.constant.RemittanceKey;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.constant.RequestCode;
import com.bestfriend.constant.Result;
import com.bestfriend.core.SMPadalaLib;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;

public class MainService extends Service {

    private LocalBroadcastManager manager;
    private SQLiteAdapter db;

    @Override
    public void onCreate() {
        super.onCreate();
        if(CodePanUtils.isPermissionGranted(this)) {
            manager = LocalBroadcastManager.getInstance(this);
            db = SQLiteCache.getDatabase(this, App.DB);
            db.openConnection();
        }
        else {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(CodePanUtils.isPermissionGranted(this)) {
            SQLiteAdapter db = getDatabase();
            if(intent != null && intent.hasExtra(Key.RECEIVER)) {
                handleReceiver(intent, db);
            }
        }
        return START_STICKY;
    }

    public void handleReceiver(Intent intent, SQLiteAdapter db) {
        final int type = intent.getIntExtra(Key.RECEIVER, 0);
        switch(type) {
            case Receiver.SMS_RECEIVER:
                long timestamp = intent.getLongExtra(Key.TIMESTAMP, 0L);
                String sender = intent.getStringExtra(Key.SENDER);
                String message = intent.getStringExtra(Key.MESSAGE);
                String smDate = CodePanUtils.getDate(timestamp);
                String smTime = CodePanUtils.getTime(timestamp);
                //if(sender != null && sender.equalsIgnoreCase(RemittanceKey.SENDER)) {
                if(message != null) {
                    if(message.contains(RemittanceKey.RECEIVE)) {
                        String[] fields = message.split(" ");
                        String amount = fields[4].replace("PHP", "").replace(",", "");
                        String charge = fields[8].replace("PHP", "").replace(",", "");
                        String mobileNo = fields[12].split("\\.")[0];
                        String referenceNo = fields[17].split(":")[1];
                        saveRemittance(db, RemittanceType.RECEIVE, smDate, smTime, amount,
                                charge, mobileNo, null, referenceNo);
                    }
                    else if(message.contains(RemittanceKey.TRANSFER)) {
                        String[] fields = message.split(" ");
                        String amount = fields[3].replace("PHP", "").replace(",", "");
                        String charge = fields[7].replace("PHP", "").replace(",", "");
                        String field = fields[13].replace("bal:PHP", "").replace(".Ref", "").replace(",", "");
                        String balance = field.split(":")[0];
                        String referenceNo = field.split(":")[1];
                        saveRemittance(db, RemittanceType.TRANSFER, smDate, smTime, amount,
                                charge, null, balance, referenceNo);
                    }
                    else if(message.contains(RemittanceKey.BALANCE)) {
                        String[] fields = message.split(" ");
                        String balance = fields[1].replace("Bal:PHP", "").replace(",", "");
                        updateBalance(db, balance);
                    }
                }
                //}
                break;
        }
    }

    public void saveRemittance(final SQLiteAdapter db, final int type, final String smDate,
                               final String smTime, final String amount, final String charge,
                               final String mobileNo, final String balance,
                               final String referenceNo) {
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = SMPadalaLib.saveRemittance(db, type, smDate, smTime, amount,
                            charge, mobileNo, balance, referenceNo);
                    handler.obtainMessage(result ? Result.SUCCESS :
                            Result.FAILED).sendToTarget();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bg.start();
    }

    public void updateBalance(final SQLiteAdapter db, final String balance) {
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = SMPadalaLib.updateBalance(db, balance);
                    handler.obtainMessage(result ? Result.SUCCESS :
                            Result.FAILED).sendToTarget();
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
            switch(msg.what) {
                case Result.SUCCESS:
                    sendBroadcast(Notification.SMS_RECEIVE);
                    break;
                case Result.FAILED:
                    break;
            }
            return true;
        }
    });

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public SQLiteAdapter getDatabase() {
        if(db == null) {
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
