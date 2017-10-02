package com.codepan.database;

public class FieldValue {

	public enum Value {
		NULL
	}

	private final int TRUE = 1;
	private final int FALSE = 0;

	public String field, value;

	public FieldValue(Field field, Field value) {
		setField(field);
		setValue(value);
	}

	public FieldValue(String field, String value) {
		this.value = value != null ? "'" + value
				.replace("'", "''") + "'" : "NULL";
		this.field = field;
	}

	public FieldValue(String field, Value value) {
		this.value = String.valueOf(value);
		this.field = field;
	}

	public FieldValue(String field, long value) {
		this.value = String.valueOf(value);
		this.field = field;
	}

	public FieldValue(String field, int value) {
		this.value = String.valueOf(value);
		this.field = field;
	}

	public FieldValue(String field, double value) {
		this.value = String.valueOf(value);
		this.field = field;
	}

	public FieldValue(String field, float value) {
		this.value = String.valueOf(value);
		this.field = field;
	}

	public FieldValue(String field, boolean value) {
		this.field = field;
		if(value) {
			this.value = String.valueOf(TRUE);
		}
		else {
			this.value = String.valueOf(FALSE);
		}
	}

	public FieldValue(Field field, String value) {
		this.value = value != null ? "'" + value
				.replace("'", "''") + "'" : "NULL";
		setField(field);
	}

	public FieldValue(Field field, long value) {
		this.value = String.valueOf(value);
		setField(field);
	}

	public FieldValue(Field field, int value) {
		this.value = String.valueOf(value);
		setField(field);
	}

	public FieldValue(Field field, double value) {
		this.value = String.valueOf(value);
		setField(field);
	}

	public FieldValue(Field field, float value) {
		this.value = String.valueOf(value);
		setField(field);
	}

	public FieldValue(Field field, boolean value) {
		setField(field);
		if(value) {
			this.value = String.valueOf(TRUE);
		}
		else {
			this.value = String.valueOf(FALSE);
		}
	}

	private void setField(Field field) {
		if(field != null) {
			Table table = field.table;
			if(table != null) {
				this.field = table.as + "." + table.name;
			}
			else {
				this.field = field.field;
			}
		}
	}

	private void setValue(Field value) {
		if(value != null) {
			Table table = value.table;
			if(table != null) {
				this.value = table.as + "." + table.name;
			}
			else {
				this.value = value.field;
			}
		}
	}
}
