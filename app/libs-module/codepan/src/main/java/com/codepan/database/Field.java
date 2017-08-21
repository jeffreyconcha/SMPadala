package com.codepan.database;

import com.codepan.database.SQLiteQuery.DataType;

public class Field {

	public boolean isPrimaryKey, isDefault, withDataType;
	public String field, defText;
	public DataType type;
	public Table table;
	public int defInt;

	public Field(String field) {
		this.field = field;
	}

	public Field(String field, Table table) {
		this.field = table.as + "." + field;
		this.table = table;
	}

	public Field(String field, DataType type) {
		this.withDataType = true;
		this.field = field;
		this.type = type;
	}

	public Field(String field, String defText) {
		this.withDataType = true;
		this.isDefault = true;
		this.type = DataType.TEXT;
		this.defText = defText;
		this.field = field;
	}

	public Field(String field, int defInt) {
		this.withDataType = true;
		this.isDefault = true;
		this.field = field;
		this.type = DataType.INTEGER;
		this.defInt = defInt;
	}

	public Field(String field, boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
		this.type = DataType.INTEGER;
		this.withDataType = true;
		this.field = field;
	}

	public String getDataType() {
		String dataType = null;
		switch(type) {
			case INTEGER:
				dataType = "INTEGER";
				break;
			case TEXT:
				dataType = "TEXT";
				break;
		}
		return dataType;
	}

	public String getDefaultValue() {
		String defValue = null;
		switch(type) {
			case INTEGER:
				defValue = String.valueOf(defInt);
				break;
			case TEXT:
				defValue = defText;
				break;
		}
		return defValue;
	}
}
