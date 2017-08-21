package com.bestfriend.core;

import com.bestfriend.constant.RemittanceType;
import com.bestfriend.schema.Tables;
import com.codepan.database.FieldValue;
import com.codepan.database.SQLiteAdapter;
import com.codepan.database.SQLiteBinder;
import com.codepan.database.SQLiteQuery;
import com.codepan.utils.CodePanUtils;

import java.util.Arrays;
import java.util.List;

import static com.bestfriend.schema.Tables.*;

public class SMPadalaLib {

    public static void createTables(SQLiteAdapter db) {
        SQLiteBinder binder = new SQLiteBinder(db);
        List<Tables.TB> tableList = Arrays.asList(TB.values());
        for(TB tb : tableList) {
            String table = getName(tb);
            binder.createTable(table, create(tb));
        }
        binder.finish();
    }

    public static boolean saveRemittance(SQLiteAdapter db, int type, String smDate, String smTime,
                                         String amount, String charge, String mobileNo,
                                         String balance, String referenceNo) {
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
        if(!db.isRecordExists(sql)) {
            binder.insert(table, query);
        }
        return binder.finish();
    }

    public static boolean updateBalance(SQLiteAdapter db, String balance) {
        SQLiteBinder binder = new SQLiteBinder(db);
        String table = Tables.getName(TB.REMITTANCE);
        String sql = "SELECT ID FROM " + table + " WHERE balance IS NULL AND type = '" +
                RemittanceType.RECEIVE + "' ORDER BY ID LIMIT 1";
        if(db.isRecordExists(sql)) {
            String remittanceID = db.getString(sql);
            SQLiteQuery query = new SQLiteQuery();
            query.add(new FieldValue("balance", balance));
            binder.update(table, query, remittanceID);
        }
        return binder.finish();
    }
}
