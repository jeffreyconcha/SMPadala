package com.bestfriend.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.bestfriend.constant.Key;
import com.bestfriend.constant.Receiver;
import com.bestfriend.service.MainService;
import com.codepan.utils.Console;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Console.log("RECEIVE SMS");
        final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
        String action = intent.getAction();
        if(action != null && action.equals(SMS_RECEIVED)) {
            final Bundle bundle = intent.getExtras();
            if(bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if(pdus != null && pdus.length > 0) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < pdus.length; i++) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            String format = bundle.getString("format");
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        }
                        else {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                        sb.append(messages[i].getMessageBody());
                    }
                    long timestamp = messages[0].getTimestampMillis();
                    String sender = messages[0].getOriginatingAddress();
                    String message = sb.toString();
                    Intent service = new Intent(context, MainService.class);
                    service.putExtra(Key.RECEIVER, Receiver.SMS_RECEIVER);
                    service.putExtra(Key.TIMESTAMP, timestamp);
                    service.putExtra(Key.MESSAGE, message);
                    service.putExtra(Key.SENDER, sender);
                    context.startService(service);
                }
            }
        }
    }
}
