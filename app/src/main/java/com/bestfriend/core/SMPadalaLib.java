package com.bestfriend.core;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.bestfriend.constant.App;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.ReceiveObj;
import com.bestfriend.model.RemittanceObj;
import com.bestfriend.schema.Tables;
import com.bestfriend.smpadala.AlertDialogFragment;
import com.bestfriend.smpadala.R;
import com.codepan.database.FieldValue;
import com.codepan.database.SQLiteAdapter;
import com.codepan.database.SQLiteBinder;
import com.codepan.database.SQLiteQuery;
import com.codepan.utils.CodePanUtils;

import net.sqlcipher.Cursor;

import java.util.Arrays;
import java.util.List;

import static com.bestfriend.schema.Tables.TB;
import static com.bestfriend.schema.Tables.create;
import static com.bestfriend.schema.Tables.getName;
import static com.codepan.database.FieldValue.Value;

public class SMPadalaLib {

	public static void alertDialog(final FragmentActivity activity, String title, String message) {
		final FragmentManager manager = activity.getSupportFragmentManager();
		final AlertDialogFragment alert = new AlertDialogFragment();
		alert.setDialogTitle(title);
		alert.setDialogMessage(message);
		alert.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(View view) {
				manager.popBackStack();
			}
		});
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
			String table = getName(tb);
			binder.createTable(table, create(tb));
		}
		binder.finish();
	}

	public static void updateTables(SQLiteAdapter db, int o, int n) {
		SQLiteBinder binder = new SQLiteBinder(db);
		String column = "isCancelled";
		String table = Tables.getName(TB.RECEIVE);
		if(!db.isColumnExists(table, column)) {
			binder.addColumn(table, column, 0);
		}
		column = "isActive";
		table = Tables.getName(TB.CUSTOMER);
		if(!db.isColumnExists(table, column)) {
			binder.addColumn(table, column, 1);
		}
		if(o == 6 && n == 7) {
			table = Tables.getName(TB.REMITTANCE);
			SQLiteQuery query = new SQLiteQuery();
			query.add(new FieldValue("balance", Value.NULL));
			query.add(new FieldValue("referenceNo", "aa42aee79b9e"));
			binder.update(table, query, 502);
		}
		binder.finish();
	}

	public static boolean hasRemittance(SQLiteAdapter db) {
		String table = Tables.getName(TB.REMITTANCE);
		String query = "SELECT ID FROM " + table + " LIMIT 1";
		return db.isRecordExists(query);
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

	public static boolean undoTransaction(SQLiteAdapter db, RemittanceObj remittance) {
		SQLiteBinder binder = new SQLiteBinder(db);
		String h = Tables.getName(TB.REMITTANCE);
		String d = Tables.getName(TB.RECEIVE);
		SQLiteQuery query = new SQLiteQuery();
		query.add(new FieldValue("isClaimed", false));
		binder.update(h, query, remittance.ID);
		query.clearAll();
		ReceiveObj receive = remittance.receive;
		query.add(new FieldValue("isCancelled", true));
		binder.update(d, query, receive.ID);
		return binder.finish();
	}

	public static boolean backUpData(Context context, boolean external) {
		return CodePanUtils.extractDatabase(context, App.DB_BACKUP, App.DB, external);
	}

	public static CustomerObj addCustomer(SQLiteAdapter db, String name, String mobileNo,
			String address, String photo) {
		CustomerObj customer = new CustomerObj();
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
		if(customer.ID != null) {
			customer.name = name;
			customer.address = address;
			customer.mobileNo = mobileNo;
			customer.photo = photo;
		}
		return customer;
	}

	public static CustomerObj editCustomer(SQLiteAdapter db, String name, String mobileNo,
			String address, String photo, String customerID) {
		CustomerObj customer = new CustomerObj();
		SQLiteBinder binder = new SQLiteBinder(db);
		String table = Tables.getName(TB.CUSTOMER);
		SQLiteQuery query = new SQLiteQuery();
		query.add(new FieldValue("name", name));
		query.add(new FieldValue("mobileNo", mobileNo));
		query.add(new FieldValue("address", address));
		query.add(new FieldValue("photo", photo));
		binder.update(table, query, customerID);
		boolean result = binder.finish();
		if(result) {
			customer.ID = customerID;
			customer.name = name;
			customer.address = address;
			customer.mobileNo = mobileNo;
			customer.photo = photo;
		}
		return customer;
	}

	public static boolean claim(SQLiteAdapter db, String remittanceID) {
		SQLiteBinder binder = new SQLiteBinder(db);
		String table = Tables.getName(TB.REMITTANCE);
		String dDate = CodePanUtils.getDate();
		String dTime = CodePanUtils.getTime();
		SQLiteQuery query = new SQLiteQuery();
		query.add(new FieldValue("dDate", dDate));
		query.add(new FieldValue("dTime", dTime));
		query.add(new FieldValue("isClaimed", true));
		binder.update(table, query, remittanceID);
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

	public static ReceiveObj receive(SQLiteAdapter db, CustomerObj customer, String remittanceID,
			boolean isClaimed) {
		ReceiveObj receive = new ReceiveObj();
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
		if(!db.isRecordExists(sql)) {
			receive.ID = binder.insert(d, query);
			receive.dDate = dDate;
			receive.dTime = dTime;
		}
		else {
			Cursor cursor = db.read(sql);
			while(cursor.moveToNext()) {
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

	public static int getNoOfCustomers(SQLiteAdapter db) {
		String table = Tables.getName(TB.CUSTOMER);
		String query = "SELECT COUNT(ID) FROM " + table + " WHERE isActive = 1";
		return db.getInt(query);
	}
}
