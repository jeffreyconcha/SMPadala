package com.codepan.database;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;

import static com.codepan.database.SQLiteQuery.DataType;

public class SQLiteBinder {

	private SQLiteAdapter db;

	public SQLiteBinder(SQLiteAdapter db) {
		this.db = db;
		this.db.beginTransaction();
	}

	public String insert(String table, ArrayList<FieldValue> fields) {
		SQLiteQuery query = new SQLiteQuery();
		query.setFieldValueList(fields);
		return insert(table, query);
	}

	public String insert(String table, SQLiteQuery query) {
		String sql = query.insert(table);
		SQLiteStatement insert = db.compileStatement(sql);
		long id = insert.executeInsert();
		insert.close();
		if(id == -1) {
			return null;
		}
		else {
			return String.valueOf(id);
		}
	}

	public void update(String table, ArrayList<FieldValue> fields, String recID) {
		SQLiteQuery query = new SQLiteQuery();
		query.setFieldValueList(fields);
		update(table, query, recID);
	}

	public void update(String table, ArrayList<FieldValue> fields, int recID) {
		SQLiteQuery query = new SQLiteQuery();
		query.setFieldValueList(fields);
		update(table, query, recID);
	}

	public void update(String table, ArrayList<FieldValue> fields, ArrayList<Condition> conditions) {
		SQLiteQuery query = new SQLiteQuery();
		query.setFieldValueList(fields);
		query.setConditionList(conditions);
		update(table, query);
	}

	public void update(String table, SQLiteQuery query) {
		String sql = query.update(table);
		SQLiteStatement update = db.compileStatement(sql);
		update.execute();
		update.close();
	}

	public void update(String table, SQLiteQuery query, String recID) {
		String sql = query.update(table, recID);
		SQLiteStatement update = db.compileStatement(sql);
		update.execute();
		update.close();
	}

	public void update(String table, SQLiteQuery query, int recID) {
		String sql = query.update(table, recID);
		SQLiteStatement update = db.compileStatement(sql);
		update.execute();
		update.close();
	}

	public void delete(String table, ArrayList<Condition> conditions) {
		SQLiteQuery query = new SQLiteQuery();
		query.setConditionList(conditions);
		delete(table, query);
	}

	public void delete(String table, SQLiteQuery query) {
		String sql = query.delete(table);
		SQLiteStatement delete = db.compileStatement(sql);
		delete.execute();
		delete.close();
	}

	public void addColumn(String table, String column, String defValue) {
		SQLiteQuery query = new SQLiteQuery();
		String sql = query.addColumn(table, column, defValue);
		SQLiteStatement alter = db.compileStatement(sql);
		alter.execute();
		alter.close();
	}

	public void addColumn(String table, String column, int defValue) {
		SQLiteQuery query = new SQLiteQuery();
		String sql = query.addColumn(table, column, defValue);
		SQLiteStatement alter = db.compileStatement(sql);
		alter.execute();
		alter.close();
	}

	public void addColumn(String table, DataType type, String column) {
		SQLiteQuery query = new SQLiteQuery();
		String sql = query.addColumn(table, type, column);
		SQLiteStatement alter = db.compileStatement(sql);
		alter.execute();
		alter.close();
	}

	public void renameTable(String oldName, String newName) {
		SQLiteQuery query = new SQLiteQuery();
		String sql = query.renameTable(oldName, newName);
		SQLiteStatement alter = db.compileStatement(sql);
		alter.execute();
		alter.close();
	}

	public void dropTable(String table) {
		SQLiteQuery query = new SQLiteQuery();
		String sql = query.dropTable(table);
		SQLiteStatement drop = db.compileStatement(sql);
		drop.execute();
		drop.close();
	}

	public void createTable(String table, SQLiteQuery query) {
		String sql = query.createTable(table);
		SQLiteStatement create = db.compileStatement(sql);
		create.execute();
		create.close();
	}

	public void execute(String sql) {
		SQLiteStatement statement = db.compileStatement(sql);
		statement.execute();
		statement.close();
	}

	public void truncate(String table) {
		SQLiteQuery query = new SQLiteQuery();
		String sql = query.delete(table);
		SQLiteStatement statement = db.compileStatement(sql);
		statement.execute();
		statement.close();
	}

	public boolean finish() {
		boolean result = false;
		try {
			db.setTransactionSuccessful();
			result = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			db.endTransaction();
		}
		return result;
	}
}
