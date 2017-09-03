package com.bestfriend.core;

import android.util.Log;

import com.bestfriend.constant.RemittanceStatus;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.ReceiveObj;
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
                                                          String status, String start, int type, int limit) {
        ArrayList<RemittanceObj> remittanceList = new ArrayList<>();
        SQLiteQuery query = new SQLiteQuery();
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
        String d = Tables.getName(TB.RECEIVE);
        String c = Tables.getName(TB.CUSTOMER);
        String sql = "SELECT h.ID, h.dDate, h.dTime, h.smDate, h.smTime, h.charge, h.type, " +
                "h.amount, h.referenceNo, h.balance, h.mobileNo, h.isClaimed, d.ID, d.dDate, " +
                "d.dTime, c.ID, c.name, c.photo, c.address, c.mobileNo FROM " + h + " h LEFT " +
                "JOIN " + d + " d ON d.remittanceID = h.ID LEFT JOIN " + c + " c ON " +
                "c.ID = d.customerID " + condition + "ORDER BY h.ID DESC LIMIT " + limit;
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
            String receiveID = cursor.getString(12);
            if(receiveID != null) {
                ReceiveObj receive = new ReceiveObj();
                receive.ID = receiveID;
                receive.dDate = cursor.getString(13);
                receive.dTime = cursor.getString(14);
                String customerID = cursor.getString(15);
                if(customerID != null) {
                    CustomerObj customer = new CustomerObj();
                    customer.ID = customerID;
                    customer.name = cursor.getString(16);
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

    public static ArrayList<CustomerObj> loadCustomers(SQLiteAdapter db) {
        ArrayList<CustomerObj> customerList = new ArrayList<>();
        String table = Tables.getName(TB.CUSTOMER);
        String query = "SELECT ID, name, mobileNo, address, photo FROM " + table + " ORDER BY name " +
                "COLLATE NOCASE";
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
}
