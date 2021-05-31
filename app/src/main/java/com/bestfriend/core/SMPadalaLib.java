package com.bestfriend.core;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bestfriend.constant.App;
import com.bestfriend.constant.RemittanceKey;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.constant.Result;
import com.bestfriend.model.CustomerData;
import com.bestfriend.model.MessageData;
import com.bestfriend.model.ReceiveData;
import com.bestfriend.model.RemittanceData;
import com.bestfriend.model.TransferData;
import com.bestfriend.schema.Tables;
import com.bestfriend.smpadala.AlertDialogFragment;
import com.bestfriend.smpadala.R;
import com.codepan.database.Condition;
import com.codepan.database.Field;
import com.codepan.database.FieldValue;
import com.codepan.database.SQLiteAdapter;
import com.codepan.database.SQLiteBinder;
import com.codepan.database.SQLiteQuery;
import com.codepan.database.TableIndices;
import com.codepan.utils.CodePanUtils;
import com.codepan.utils.Console;
import com.codepan.widget.CodePanLabel;

import net.sqlcipher.Cursor;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.bestfriend.schema.Tables.TB;

public class SMPadalaLib {

    public static void alertToast(FragmentActivity activity, String message) {
        int offsetY = activity.getResources().getDimensionPixelSize(R.dimen.one_hundred);
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, activity.findViewById(R.id.rlAlertToast));
        CodePanLabel text = layout.findViewById(R.id.tvMessageAlertToast);
        text.setText(message);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.BOTTOM, 0, offsetY);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void alertDialog(final FragmentActivity activity, String title, String message) {
        final FragmentManager manager = activity.getSupportFragmentManager();
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle(title);
        alert.setDialogMessage(message);
        alert.setPositiveButton("OK", view -> manager.popBackStack());
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);
        transaction.add(R.id.rlMain, alert);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static void createTables(SQLiteAdapter db) {
        SQLiteBinder binder = new SQLiteBinder(db);
        List<Tables.TB> tableList = Arrays.asList(TB.values());
        for(TB tb : tableList) {
            String table = Tables.getName(tb);
            binder.createTable(table, Tables.fields(tb));
        }
        binder.finish();
    }

    public static void createIndexes(SQLiteAdapter db) {
        for(TB tb : TB.values()) {
            TableIndices indices = Tables.indexes(tb);
            if(indices != null) {
                indices.create(db);
            }
        }
    }

    public static void updateTables(SQLiteAdapter db, int o, int n) {
        SQLiteBinder binder = new SQLiteBinder(db);
        TB[] tableList = TB.values();
        for(TB tb : tableList) {
            String table = Tables.getName(tb);
            SQLiteQuery create = Tables.fields(tb);
            if(create.hasFields()) {
                ArrayList<Field> fieldList = create.getFieldList();
                ArrayList<String> columnList = db.getColumnList(table);
                if(fieldList.size() > columnList.size()) {
                    for(Field field : fieldList) {
                        if(!columnList.contains(field.field)) {
                            binder.addColumn(table, field);
                        }
                    }
                }
            }
        }
        binder.finish();
    }

    public static void fixData(SQLiteAdapter db) {
        String table = Tables.getName(TB.REMITTANCE);
        db.execQuery("UPDATE " + table + " SET amount = 0 WHERE length(amount) > 10");
    }

    public static boolean hasRemittance(SQLiteAdapter db) {
        String table = Tables.getName(TB.REMITTANCE);
        String query = "SELECT ID FROM " + table + " LIMIT 1";
        return db.isRecordExists(query);
    }

    public static boolean saveRemittance(SQLiteAdapter db, int type, String smDate, String smTime,
        String amount, String charge, String mobileNo, String balance, String referenceNo) {
        String dDate = CodePanUtils.getDate();
        String dTime = CodePanUtils.getTime();
        SQLiteBinder binder = new SQLiteBinder(db);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("type", type));
        query.add(new FieldValue("dDate", dDate));
        query.add(new FieldValue("dTime", dTime));
        query.add(new FieldValue("smDate", smDate));
        query.add(new FieldValue("smTime", smTime));
        query.add(new FieldValue("amount", amount));
        query.add(new FieldValue("charge", charge));
        query.add(new FieldValue("mobileNo", mobileNo));
        query.add(new FieldValue("balance", balance));
        query.add(new FieldValue("referenceNo", referenceNo));
        String table = Tables.getName(TB.REMITTANCE);
        String sql = "SELECT ID FROM " + table + " WHERE referenceNo = '" + referenceNo + "' " +
                "AND type = '" + type + "'";
        if (!db.isRecordExists(sql)) {
            binder.insert(table, query);
        }
        return binder.finish();
    }

    public static boolean updateBalance(SQLiteAdapter db, String balance) {
        SQLiteBinder binder = new SQLiteBinder(db);
        String table = Tables.getName(TB.REMITTANCE);
        String sql = "SELECT ID FROM " + table + " WHERE balance IS NULL AND type = '" +
                RemittanceType.INCOMING + "' ORDER BY ID LIMIT 1";
        if (db.isRecordExists(sql)) {
            String remittanceID = db.getString(sql);
            SQLiteQuery query = new SQLiteQuery();
            query.add(new FieldValue("balance", balance));
            binder.update(table, query, remittanceID);
        }
        return binder.finish();
    }

    public static boolean undoTransaction(SQLiteAdapter db, RemittanceData remittance) {
        SQLiteBinder binder = new SQLiteBinder(db);
        String h = Tables.getName(TB.REMITTANCE);
        String d = Tables.getName(TB.RECEIVE);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("isClaimed", false));
        binder.update(h, query, remittance.ID);
        query.clearAll();
        ReceiveData receive = remittance.receive;
        query.add(new FieldValue("isCancelled", true));
        binder.update(d, query, receive.ID);
        return binder.finish();
    }

    public static boolean untagCustomer(SQLiteAdapter db, TransferData transfer) {
        SQLiteBinder binder = new SQLiteBinder(db);
        String table = Tables.getName(TB.TRANSFER);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("isCancelled", true));
        binder.update(table, query, transfer.ID);
        return binder.finish();
    }

    public static boolean backUpData(Context context, boolean external) {
        return CodePanUtils.extractDatabase(context, App.DB_BACKUP, App.DB, external);
    }

    public static CustomerData addCustomer(SQLiteAdapter db, String name, String mobileNo,
        String address, String photo) {
        CustomerData customer = new CustomerData();
        SQLiteBinder binder = new SQLiteBinder(db);
        String table = Tables.getName(TB.CUSTOMER);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("name", name));
        query.add(new FieldValue("mobileNo", mobileNo));
        query.add(new FieldValue("address", address));
        query.add(new FieldValue("photo", photo));
        String sql = "SELECT ID FROM " + table + " WHERE name = '" + name + "' AND isActive = 1";
        if(!db.isRecordExists(sql)) {
            customer.ID = binder.insert(table, query);
        }
        else {
            customer.ID = db.getString(sql);
            binder.update(table, query, customer.ID);
        }
        binder.finish();
        if (customer.ID != null) {
            customer.name = name;
            customer.address = address;
            customer.mobileNo = mobileNo;
            customer.photo = photo;
        }
        return customer;
    }

    public static CustomerData editCustomer(SQLiteAdapter db, String name, String mobileNo,
                                            String address, String photo, String customerID) {
        CustomerData customer = new CustomerData();
        SQLiteBinder binder = new SQLiteBinder(db);
        String table = Tables.getName(TB.CUSTOMER);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("name", name));
        query.add(new FieldValue("mobileNo", mobileNo));
        query.add(new FieldValue("address", address));
        query.add(new FieldValue("photo", photo));
        binder.update(table, query, customerID);
        boolean result = binder.finish();
        if (result) {
            customer.ID = customerID;
            customer.name = name;
            customer.address = address;
            customer.mobileNo = mobileNo;
            customer.photo = photo;
        }
        return customer;
    }

    public static boolean claim(SQLiteAdapter db, String dDate, String dTime, String remittanceID) {
        SQLiteBinder binder = new SQLiteBinder(db);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("isClaimed", true));
        binder.update(Tables.getName(TB.REMITTANCE), query, remittanceID);
        query.clearAll();
        query.add(new FieldValue("dDate", dDate));
        query.add(new FieldValue("dTime", dTime));
        query.add(new Condition("remittanceID", remittanceID));
        binder.update(Tables.getName(TB.RECEIVE), query);
        return binder.finish();
    }

    public static boolean deleteCustomer(SQLiteAdapter db, String customerID) {
        SQLiteBinder binder = new SQLiteBinder(db);
        String table = Tables.getName(TB.CUSTOMER);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("isActive", false));
        binder.update(table, query, customerID);
        return binder.finish();
    }

    public static ReceiveData receive(SQLiteAdapter db, CustomerData customer, String remittanceID,
                                      boolean isClaimed) {
        ReceiveData receive = new ReceiveData();
        SQLiteBinder binder = new SQLiteBinder(db);
        String dDate = CodePanUtils.getDate();
        String dTime = CodePanUtils.getTime();
        String h = Tables.getName(TB.REMITTANCE);
        String d = Tables.getName(TB.RECEIVE);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("isClaimed", isClaimed));
        binder.update(h, query, remittanceID);
        query.clearAll();
        query.add(new FieldValue("dDate", dDate));
        query.add(new FieldValue("dTime", dTime));
        query.add(new FieldValue("isCancelled", false));
        query.add(new FieldValue("customerID", customer.ID));
        query.add(new FieldValue("remittanceID", remittanceID));
        String sql = "SELECT ID, dDate, dTime FROM " + d + " WHERE remittanceID = '" +
                remittanceID + "' AND customerID = '" + customer.ID + "' AND " +
                "isCancelled = 0";
        if (!db.isRecordExists(sql)) {
            receive.ID = binder.insert(d, query);
            receive.dDate = dDate;
            receive.dTime = dTime;
        }
        else {
            Cursor cursor = db.read(sql);
            while (cursor.moveToNext()) {
                receive.ID = cursor.getString(0);
                receive.dDate = cursor.getString(1);
                receive.dTime = cursor.getString(2);
            }
            cursor.close();
        }
        receive.customer = customer;
        binder.finish();
        return receive;
    }

    public static TransferData tagTransfer(SQLiteAdapter db, CustomerData customer,
                                           String remittanceID, String receiver) {
        TransferData transfer = new TransferData();
        SQLiteBinder binder = new SQLiteBinder(db);
        String dDate = CodePanUtils.getDate();
        String dTime = CodePanUtils.getTime();
        String table = Tables.getName(TB.TRANSFER);
        SQLiteQuery query = new SQLiteQuery();
        query.add(new FieldValue("dDate", dDate));
        query.add(new FieldValue("dTime", dTime));
        query.add(new FieldValue("receiver", receiver));
        query.add(new FieldValue("isCancelled", false));
        query.add(new FieldValue("customerID", customer.ID));
        query.add(new FieldValue("remittanceID", remittanceID));
        String sql = "SELECT ID, dDate, dTime, receiver FROM " + table + " WHERE remittanceID = '" +
                remittanceID + "' AND customerID = '" + customer.ID + "' AND " +
                "isCancelled = 0";
        if (!db.isRecordExists(sql)) {
            transfer.ID = binder.insert(table, query);
            transfer.dDate = dDate;
            transfer.dTime = dTime;
            transfer.receiver = receiver;
        }
        else {
            Cursor cursor = db.read(sql);
            while (cursor.moveToNext()) {
                transfer.ID = cursor.getString(0);
                transfer.dDate = cursor.getString(1);
                transfer.dTime = cursor.getString(2);
                transfer.receiver = cursor.getString(3);
            }
            cursor.close();
        }
        transfer.customer = customer;
        binder.finish();
        return transfer;
    }

    public static int getNoOfCustomers(SQLiteAdapter db) {
        String table = Tables.getName(TB.CUSTOMER);
        String query = "SELECT COUNT(ID) FROM " + table + " WHERE isActive = 1";
        return db.getInt(query);
    }

    public static int getMessageType(String text) {
        if (text != null) {
            for (String key : RemittanceKey.INGOING) {
                if (StringUtils.containsIgnoreCase(text, key)) {
                    return RemittanceType.INCOMING;
                }
            }
            for (String key : RemittanceKey.OUTGOING) {
                if (StringUtils.containsIgnoreCase(text, key)) {
                    return RemittanceType.OUTGOING;
                }
            }
        }
        return Result.FAILED;
    }

    public static String getNextAmountFromKey(String text, String key) {
        StringBuilder builder = new StringBuilder();
        int index = text.toLowerCase().lastIndexOf(key.toLowerCase());
        String substring = text.substring(index + key.length());
        char period = '.';
        int maxDecimal = 2;
        int decimalCount = 0;
        boolean isDecimal = false;
        for (int i = 0; i < substring.length(); i++) {
            if (decimalCount < maxDecimal) {
                char c = substring.charAt(i);
                if (Character.isDigit(c)) {
                    if (isDecimal) {
                        decimalCount++;
                    }
                    builder.append(c);
                }
                else {
                    if (c == period) {
                        isDecimal = true;
                        builder.append(c);
                    }
                }
            }
        }
        return builder.toString();
    }

    public static String getReferenceAfterKey(String text, String key) {
        StringBuilder builder = new StringBuilder();
        int index = text.toLowerCase().lastIndexOf(key.toLowerCase());
        String substring = text.substring(index + key.length());
        boolean hasStarted = false;
        boolean isFinish = false;
        for (int i = 0; i < substring.length(); i++) {
            if (!isFinish) {
                char c = substring.charAt(i);
                if (Character.isDigit(c) || Character.isLetter(c)) {
                    builder.append(c);
                    if (!hasStarted) {
                        hasStarted = true;
                    }
                }
                else {
                    if (hasStarted) {
                        isFinish = true;
                    }
                }
            }
        }
        return builder.toString();
    }

    public static MessageData scanMessage(String text) {
        MessageData message = null;
        if (text != null) {
            int type = getMessageType(text);
            if (type != Result.FAILED) {
                message = new MessageData(type);
                for (String key : RemittanceKey.AMOUNT) {
                    if (StringUtils.containsIgnoreCase(text, key)) {
                        message.amount = getNextAmountFromKey(text, key);
                        break;
                    }
                }
                for (String key : RemittanceKey.CHARGE) {
                    if (StringUtils.containsIgnoreCase(text, key)) {
                        message.charge = getNextAmountFromKey(text, key);
                        break;
                    }
                }
                for (String key : RemittanceKey.BALANCE) {
                    if (StringUtils.containsIgnoreCase(text, key)) {
                        message.balance = getNextAmountFromKey(text, key);
                        break;
                    }
                }
                for (String key : RemittanceKey.REFERENCE) {
                    if (StringUtils.containsIgnoreCase(text, key)) {
                        message.referenceNo = getReferenceAfterKey(text, key);
                        break;
                    }
                }
                String t = type == RemittanceType.OUTGOING ? "outgoing" : "incoming";
                Console.log("TYPE: " + t);
                Console.log("AMOUNT: " + message.amount);
                Console.log("CHARGE: " + message.charge);
                Console.log("BALANCE: " + message.balance);
                Console.log("REF NO: " + message.referenceNo);
            }
        }
        return message;
    }
}
