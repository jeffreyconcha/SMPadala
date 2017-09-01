package com.bestfriend.core;

import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.RemittanceObj;
import com.bestfriend.schema.Tables;
import com.bestfriend.schema.Tables.TB;
import com.codepan.database.SQLiteAdapter;

import net.sqlcipher.Cursor;

import java.util.ArrayList;

public class Data {

    public static ArrayList<RemittanceObj> loadRemittance(SQLiteAdapter db) {
        ArrayList<RemittanceObj> remittanceList = new ArrayList<>();
        String r = Tables.getName(TB.REMITTANCE);
        String c = Tables.getName(TB.CUSTOMER);
        String query = "SELECT r.ID, r.dDate, r.dTime, r.smDate, r.smTime, r.charge, r.type, " +
                "r.amount, r.referenceNo, r.balance, r.mobileNo, r.isClaimed, c.ID, c.name, " +
                "c.photo, c.address, c.mobileNo FROM " + r + " h LEFT JOIN " + c + " c " +
                "ON c.ID = d.customerID ORDER BY r.ID DESC";
        Cursor cursor = db.read(query);
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
