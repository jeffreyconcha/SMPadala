package com.codepan.database;

import java.util.ArrayList;

public class Table {

	public enum Join {
		LEFT,
		RIGHT
	}

	public ArrayList<Condition> conditionList;
	public String table, name, as;
	private boolean hasJoin;

	public Table(String name, String as) {
		this.table = name + " as " + as;
		this.name = name;
		this.as = as;
	}

	public Table join(Join join) {
		this.hasJoin = true;
		switch(join) {
			case LEFT:
				table = "LEFT JOIN " + table + " ON ";
				break;
			case RIGHT:
				table = "RIGHT JOIN " + table + " ON ";
				break;
		}
		return this;
	}

	public void add(Condition condition) {
		if(conditionList == null) {
			conditionList = new ArrayList<>();
		}
		conditionList.add(condition);
	}

	public void clear() {
		if(conditionList != null) {
			conditionList.clear();
		}
		this.hasJoin = false;
	}

	public void join(Join join, ArrayList<Condition> conditionList) {
		this.conditionList = conditionList;
		this.hasJoin = true;
		join(join);
	}

	public boolean hasJoin() {
		return this.hasJoin;
	}
}
