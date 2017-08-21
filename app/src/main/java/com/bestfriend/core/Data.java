package com.bestfriend.core;

import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.ReceiveObj;
import com.bestfriend.model.RemittanceObj;
import com.bestfriend.schema.Tables;
import com.bestfriend.schema.Tables.TB;
import com.codepan.database.SQLiteAdapter;

import net.sqlcipher.Cursor;

import java.util.ArrayList;

public class Data {

    public static ArrayList<RemittanceObj> loadRemittance(SQLiteAdapter db) {
        ArrayList<RemittanceObj> remittanceList = new ArrayList<>();
        String h = Tables.getName(TB.REMITTANCE);
        String d = Tables.getName(TB.RECEIVE);
        String c = Tables.getName(TB.CUSTOMER);
        String query = "SELECT h.ID, h.dDate, h.dTime, h.smDate, h.smTime, h.charge, h.type, " +
                "h.amount, h.referenceNo, h.balance, h.mobileNo, h.isClaimed, d.ID, d.name, c.ID, " +
                "c.firstName, c.lastName, c.photo, c.address, c.mobileNo FROM " + h + " h LEFT " +
                "JOIN " + d + " d ON d.remittanceID = h.ID LEFT JOIN " + c + " c " +
                "ON c.ID = d.customerID ORDER BY h.ID DESC";
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
            String receiveID = cursor.getString(12);
            if(receiveID != null) {
                ReceiveObj receive = new ReceiveObj();
                receive.ID = receiveID;
                receive.name = cursor.getString(13);
                String customerID = cursor.getString(14);
                if(customerID != null) {
                    CustomerObj customer = new CustomerObj();
                    customer.ID = customerID;
                    customer.firstName = cursor.getString(15);
                    customer.lastName = cursor.getString(16);
                    customer.fullName = receive.name;
                    customer.photo = cursor.getString(17);
                    customer.address = cursor.getString(18);
                    customer.mobileNo = cursor.getString(19);
                    receive.customer = customer;
                }
                remittance.receive = receive;
            }
            remittanceList.add(remittance);
        }
        cursor.close();
        return remittanceList;
    }
}
