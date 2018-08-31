package com.bestfriend.core;

import android.util.Log;

import com.bestfriend.constant.RemittanceStatus;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.ReceiveObj;
import com.bestfriend.model.RemittanceObj;
import com.bestfriend.model.SalesToDateObj;
import com.bestfriend.model.TransferObj;
import com.bestfriend.schema.Tables;
import com.bestfriend.schema.Tables.TB;
import com.codepan.database.Condition;
import com.codepan.database.Condition.Operator;
import com.codepan.database.SQLiteAdapter;
import com.codepan.database.SQLiteQuery;
import com.codepan.utils.CodePanUtils;

import net.sqlcipher.Cursor;

import java.util.ArrayList;
import java.util.Locale;

public class Data {

    public static ArrayList<RemittanceObj> loadRemittance(SQLiteAdapter db, CustomerObj receivedBy,
            String search, String smDate, String status, String start, int type, int limit) {
        ArrayList<RemittanceObj> remittanceList = new ArrayList<>();
        SQLiteQuery query = new SQLiteQuery();
        if(receivedBy != null) {
            query.add(new Condition("c.ID", receivedBy.ID));
        }
        if(smDate != null) {
            query.add(new Condition("h.smDate", smDate));
        }
        if(search != null) {
            query.add(new Condition("h.referenceNo", search, Operator.LIKE));
        }
        if(status != null) {
            switch(status) {
                case RemittanceStatus.CLAIMED:
                    query.add(new Condition("h.isClaimed", true));
                    break;
                case RemittanceStatus.PENDING:
                    query.add(new Condition("h.isClaimed", false));
                    break;
            }
        }
        if(type != RemittanceType.DEFAULT) {
            query.add(new Condition("h.type", type));
        }
        if(start != null) {
            query.add(new Condition("h.ID", start, Operator.LESS_THAN));
        }
        String condition = query.hasConditions() ? " WHERE " + query.getConditions() + " " : "";
        String h = Tables.getName(TB.REMITTANCE);
        String r = Tables.getName(TB.RECEIVE);
        String t = Tables.getName(TB.TRANSFER);
        String c = Tables.getName(TB.CUSTOMER);
        String sql = "SELECT h.ID, h.dDate, h.dTime, h.smDate, h.smTime, h.charge, h.type, " +
                "h.amount, h.referenceNo, h.balance, h.mobileNo, h.isClaimed, r.ID, r.dDate, " +
                "r.dTime, t.ID, t.dDate, t.dTime, t.receiver, c.ID, c.name, c.photo, c.address, " +
                "c.mobileNo FROM " + h + " h LEFT JOIN " + r + " r ON r.remittanceID = h.ID AND " +
                "r.isCancelled = 0 LEFT JOIN " + t + " t ON t.remittanceID = h.ID AND t.isCancelled = 0 " +
                "LEFT JOIN " + c + " c ON c.ID = (CASE WHEN h.type = '" + RemittanceType.RECEIVE + "' " +
                "THEN r.customerID ELSE t.customerID END) " + condition + "ORDER BY h.ID " +
                "DESC LIMIT " + limit;
        Log.e("DEPANOT", "" + sql);
        Cursor cursor = db.read(sql);
        while(cursor.moveToNext()) {
            RemittanceObj remittance = new RemittanceObj();
            remittance.ID = cursor.getString(0);
            remittance.dDate = cursor.getString(1);
            remittance.dTime = cursor.getString(2);
            remittance.smDate = cursor.getString(3);
            remittance.smTime = cursor.getString(4);
            remittance.charge = cursor.getFloat(5);
            remittance.type = cursor.getInt(6);
            remittance.amount = cursor.getFloat(7);
            remittance.referenceNo = cursor.getString(8);
            remittance.hasBalance = cursor.getString(9) != null;
            remittance.balance = cursor.getFloat(9);
            remittance.mobileNo = cursor.getString(10);
            remittance.isClaimed = cursor.getInt(11) == 1;
            CustomerObj customer = null;
            String customerID = cursor.getString(19);
            if(customerID != null) {
                customer = new CustomerObj();
                customer.ID = customerID;
                customer.name = cursor.getString(20);
                customer.photo = cursor.getString(21);
                customer.address = cursor.getString(22);
                customer.mobileNo = cursor.getString(23);
            }
            String receiveID = cursor.getString(12);
            if(receiveID != null) {
                ReceiveObj receive = new ReceiveObj();
                receive.ID = receiveID;
                receive.dDate = cursor.getString(13);
                receive.dTime = cursor.getString(14);
                receive.customer = customer;
                if(customer != null) {
                    remittance.isMarked = !remittance.isClaimed;
                }
                remittance.receive = receive;
            }
            String transferID = cursor.getString(15);
            if(transferID != null) {
                TransferObj transfer = new TransferObj();
                transfer.ID = transferID;
                transfer.dDate = cursor.getString(16);
                transfer.dTime = cursor.getString(17);
                transfer.receiver = cursor.getString(18);
                transfer.customer = customer;
                remittance.transfer = transfer;
            }
            remittanceList.add(remittance);
        }
        cursor.close();
        return remittanceList;
    }

    public static ArrayList<CustomerObj> loadCustomers(SQLiteAdapter db) {
        ArrayList<CustomerObj> customerList = new ArrayList<>();
        String table = Tables.getName(TB.CUSTOMER);
        String query = "SELECT ID, name, mobileNo, address, photo FROM " + table + " WHERE " +
                "isActive = 1 ORDER BY name COLLATE NOCASE";
        Cursor cursor = db.read(query);
        while(cursor.moveToNext()) {
            CustomerObj customer = new CustomerObj();
            customer.ID = cursor.getString(0);
            customer.name = cursor.getString(1);
            customer.mobileNo = cursor.getString(2);
            customer.address = cursor.getString(3);
            customer.photo = cursor.getString(4);
            customerList.add(customer);
        }
        cursor.close();
        return customerList;
    }

    public static ArrayList<SalesToDateObj> loadSalesToDate(SQLiteAdapter db, String date, int type) {
        ArrayList<SalesToDateObj> stdList = new ArrayList<>();
        int maxDays = CodePanUtils.getNoOfDays(date);
        String table = Tables.getName(TB.REMITTANCE);
        String split[] = date.split("-");
        String year = split[0];
        String month = split[1];
        int minDay = Integer.parseInt(split[2]);
        SQLiteQuery query = new SQLiteQuery();
        for(int d = 1; d <= maxDays; d++) {
            SalesToDateObj std = new SalesToDateObj();
            String day = String.format(Locale.ENGLISH, "%02d", d);
            String smDate = year + "-" + month + "-" + day;
            query.clearAll();
            query.add(new Condition("smDate", smDate));
            if(type != RemittanceType.DEFAULT) {
                query.add(new Condition("type", type));
            }
            String sql = "SELECT SUM(amount) FROM " + table + " WHERE " + query.getConditions();
            std.day = d;
            std.amount = db.getFloat(sql);
            std.isMin = minDay == d;
            stdList.add(std);
        }
        return stdList;
    }
}
