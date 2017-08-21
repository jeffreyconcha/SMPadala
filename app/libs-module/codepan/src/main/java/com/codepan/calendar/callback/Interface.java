package com.codepan.calendar.callback;

import com.codepan.calendar.model.MonthObj;
import com.codepan.calendar.model.YearObj;
public class Interface {

	public interface OnPickDateCallback {
		void onPickDate(String date);
	}

	public interface OnPickMonthCallback {
		void onPickMonth(MonthObj month);
	}

	public interface OnPickYearCallback {
		void onPickYear(YearObj year);
	}

	public interface OnSelectDateCallback {
		void onSelectDate(String date);
	}
}
