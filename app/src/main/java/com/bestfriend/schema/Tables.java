package com.bestfriend.schema;

import com.codepan.database.Field;
import com.codepan.database.SQLiteQuery;
import com.codepan.database.SQLiteQuery.DataType;

public class Tables {

	public enum TB {
		REMITTANCE,
		CUSTOMER,
		RECEIVE
	}

	public static SQLiteQuery create(TB tb) {
		SQLiteQuery query = new SQLiteQuery();
		switch(tb) {
			case CUSTOMER:
				query.add(new Field("ID", true));
				query.add(new Field("name", DataType.TEXT));
				query.add(new Field("photo", DataType.TEXT));
				query.add(new Field("address", DataType.TEXT));
				query.add(new Field("mobileNo", DataType.TEXT));
				query.add(new Field("isActive", 1));
				break;
			case REMITTANCE:
				query.add(new Field("ID", true));
				query.add(new Field("dDate", DataType.TEXT));
				query.add(new Field("dTime", DataType.TEXT));
				query.add(new Field("smDate", DataType.TEXT));
				query.add(new Field("smTime", DataType.TEXT));
				query.add(new Field("charge", DataType.TEXT));
				query.add(new Field("type", DataType.INTEGER));
				query.add(new Field("amount", DataType.TEXT));
				query.add(new Field("referenceNo", DataType.TEXT));
				query.add(new Field("balance", DataType.TEXT));
				query.add(new Field("mobileNo", DataType.TEXT));
				query.add(new Field("isClaimed", 0));
				break;
			case RECEIVE:
				query.add(new Field("ID", true));
				query.add(new Field("dDate", DataType.TEXT));
				query.add(new Field("dTime", DataType.TEXT));
				query.add(new Field("customerID", DataType.INTEGER));
				query.add(new Field("remittanceID", DataType.INTEGER));
				query.add(new Field("isCancelled", 0));
				break;
		}
		return query;
	}

	public static String getName(TB tb) {
		String name = null;
		switch(tb) {
			case CUSTOMER:
				name = "customer_tb";
				break;
			case REMITTANCE:
				name = "remittance_tb";
				break;
			case RECEIVE:
				name = "receive_tb";
				break;
		}
		return name;
	}
}
