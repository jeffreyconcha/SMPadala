package com.bestfriend.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

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

	private String removeCurrency(String amount) {
		String text = null;
		if(amount != null) {
			text = amount.replace("PHP", "")
					.replace("P", "")
					.replace(",", "");
		}
		return text;
	}

	public void handleReceiver(Intent intent, SQLiteAdapter db) {
		final int type = intent.getIntExtra(Key.RECEIVER, 0);
		switch(type) {
			case Receiver.SMS_RECEIVER:
				try {
					long timestamp = intent.getLongExtra(Key.TIMESTAMP, 0L);
					String sender = intent.getStringExtra(Key.SENDER);
					String message = intent.getStringExtra(Key.MESSAGE);
					String smDate = CodePanUtils.getDate(timestamp);
					String smTime = CodePanUtils.getTime(timestamp);
					if(sender != null && (sender.equalsIgnoreCase(RemittanceKey.SENDER_SP) ||
							sender.equalsIgnoreCase(RemittanceKey.SENDER_SM) ||
							sender.equalsIgnoreCase(RemittanceKey.SENDER_T1) ||
							sender.equalsIgnoreCase(RemittanceKey.SENDER_T2))) {
						if(message != null) {
							String[] fields = message.split(" ");
							if(message.contains(RemittanceKey.RECEIVE_SP_1)) {
								String amount = removeCurrency(fields[3]);
								String charge = removeCurrency(fields[5]);
								String mobileNo = fields[6];
								String referenceNo = fields[13]
										.replace("Ref:", "");
								String balance = removeCurrency(fields[14])
										.replace("Bal:", "");
								saveRemittance(db, RemittanceType.RECEIVE, smDate, smTime, amount,
										charge, mobileNo, balance, referenceNo);
							}
							else if(message.contains(RemittanceKey.RECEIVE_SP_2)) {
								String amount = removeCurrency(fields[4]);
								String charge = removeCurrency(fields[8]);
								String mobileNo = fields[12].split("\\.")[0];
								String referenceNo = fields[17].split(":")[1];
								saveRemittance(db, RemittanceType.RECEIVE, smDate, smTime, amount,
										charge, mobileNo, null, referenceNo);
							}
							else if(message.contains(RemittanceKey.RECEIVE_SP_3)) {
								String amount = removeCurrency(fields[3]);
								String referenceNo = fields[17]
										.replace("Ref:", "");
								String balance = removeCurrency(fields[19]);
								if(balance.length() > 1) {
									int lastIndex = balance.length() - 1;
									char period = balance.charAt(lastIndex);
									if(period == '.') {
										balance = balance.substring(0, lastIndex - 1);
									}
								}
								saveRemittance(db, RemittanceType.RECEIVE, smDate, smTime, amount,
										null, null, balance, referenceNo);
							}
							else if(message.contains(RemittanceKey.RECEIVE_SP_4)) {
								String amount = removeCurrency(fields[3]);
								String charge = removeCurrency(fields[7]);
								String field = removeCurrency(fields[13])
										.replace("Bal:", "")
										.replace(".Ref", "");
								String balance = field.split(":")[0];
								String referenceNo = field.split(":")[1];
								saveRemittance(db, RemittanceType.RECEIVE, smDate, smTime, amount,
										charge, null, balance, referenceNo);
							}
							else if(message.contains(RemittanceKey.TRANSFER_SP_1)) {
								String amount = removeCurrency(fields[2]);
								String mobileNo = fields[8].replace("\\.", "");
								String charge = removeCurrency(fields[11]);
								String text = removeCurrency(fields[14])
										.replace("account.Bal:", "")
										.replace(".Ref", "");
								String[] array = text.split(":");
								String balance = array[0];
								String referenceNo = array[1];
								saveRemittance(db, RemittanceType.TRANSFER, smDate, smTime, amount,
										charge, mobileNo, balance, referenceNo);
							}
							else if(message.contains(RemittanceKey.TRANSFER_SP_2)) {
								String amount = removeCurrency(fields[3]);
								String charge = removeCurrency(fields[7]);
								String field = removeCurrency(fields[13])
										.replace("bal:", "")
										.replace(".Ref", "");
								String balance = field.split(":")[0];
								String referenceNo = field.split(":")[1];
								saveRemittance(db, RemittanceType.TRANSFER, smDate, smTime, amount,
										charge, null, balance, referenceNo);
							}
							else if(message.contains(RemittanceKey.TRANSFER_SP_3)) {
								String amount = removeCurrency(fields[3]);
								String field = fields[9]
										.replace(".Ref", "")
										.replace(".Sa", "");
								String mobileNo = field.split(":")[0];
								String referenceNo = field.split(":")[1];
								saveRemittance(db, RemittanceType.TRANSFER, smDate, smTime, amount,
										null, mobileNo, null, referenceNo);
							}
							else if(message.contains(RemittanceKey.TRANSFER_SM)) {
								String amount = removeCurrency(fields[5]);
								String balance = removeCurrency(fields[18]);
								if(balance.length() > 1) {
									int lastIndex = balance.length() - 1;
									char period = balance.charAt(lastIndex);
									if(period == '.') {
										balance = balance.substring(0, lastIndex - 1);
									}
								}
								String referenceNo = fields[21];
								if(referenceNo.length() > 1) {
									int lastIndex = referenceNo.length() - 1;
									char period = referenceNo.charAt(lastIndex);
									if(period == '.') {
										referenceNo = referenceNo.substring(0, lastIndex - 1);
									}
								}
								String charge = "0.00";
								saveRemittance(db, RemittanceType.TRANSFER, smDate, smTime, amount,
										charge, null, balance, referenceNo);
							}
							else if(message.contains(RemittanceKey.BALANCE)) {
								String balance = removeCurrency(fields[1])
										.replace("Bal:", "");
								updateBalance(db, balance);
							}
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
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
