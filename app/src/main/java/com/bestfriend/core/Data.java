package com.bestfriend.core;

import android.util.Log;

import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.RemittanceObj;
import com.bestfriend.schema.Tables;
import com.bestfriend.schema.Tables.TB;
import com.codepan.database.Condition;
import com.codepan.database.Condition.Operator;
import com.codepan.database.SQLiteAdapter;
import com.codepan.database.SQLiteQuery;

import net.sqlcipher.Cursor;

import java.util.ArrayList;

public class Data {

    public static ArrayList<RemittanceObj> loadRemittance(SQLiteAdapter db, String search, String smDate,
                                                          String start, int limit) {
        ArrayList<RemittanceObj> remittanceList = new ArrayList<>();
        SQLiteQuery query = new SQLiteQuery();
        if(smDate != null) {
            query.add(new Condition("r.smDate", smDate));
        }
        if(search != null) {
            query.add(new Condition("r.referenceNo", search, Operator.LIKE));
        }
        if(start != null) {
            query.add(new Condition("r.ID", start, Operator.LESS_THAN));
        }
        String condition = query.hasConditions() ? " WHERE " + query.getConditions() + " " : "";
        String r = Tables.getName(TB.REMITTANCE);
        String c = Tables.getName(TB.CUSTOMER);
        String sql = "SELECT r.ID, r.dDate, r.dTime, r.smDate, r.smTime, r.charge, r.type, " +
                "r.amount, r.referenceNo, r.balance, r.mobileNo, r.isClaimed, c.ID, c.name, " +
                "c.photo, c.address, c.mobileNo FROM " + r + " r LEFT JOIN " + c + " c " +
                "ON c.ID = r.customerID " + condition + "ORDER BY r.ID DESC LIMIT " + limit;
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
            String customerID = cursor.getString(12);
            if(customerID != null) {
                CustomerObj customer = new CustomerObj();
                customer.ID = customerID;
                customer.name = cursor.getString(13);
                customer.photo = cursor.getString(14);
                customer.address = cursor.getString(15);
                customer.mobileNo = cursor.getString(16);
                remittance.customer = customer;
            }
            remittanceList.add(remittance);
        }
        cursor.close();
        return remittanceList;
    }
}
