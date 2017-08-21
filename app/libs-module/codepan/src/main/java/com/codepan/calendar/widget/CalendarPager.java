package com.codepan.calendar.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.codepan.R;
import com.codepan.calendar.adapter.CalendarDayAdapter;

public class CalendarPager extends ViewPager {

	private boolean isSet;

	public CalendarPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(!isSet) {
			View child = getChildAt(0);
			if(child instanceof GridView) {
				GridView gvCalendarDay = (GridView) child;
				ListAdapter adapter = gvCalendarDay.getAdapter();
				if(adapter instanceof CalendarDayAdapter) {
					CalendarDayAdapter day = (CalendarDayAdapter) adapter;
					int mWidth = day.getParent().getMeasuredWidth();
					int mHeight = day.getParent().getMeasuredHeight();
					int numCol = getResources().getInteger(R.integer.day_col);
					int numRow = getResources().getInteger(R.integer.day_row);
					int spacing = getResources().getDimensionPixelSize(R.dimen.cal_spacing);
					int width = (mWidth + spacing) * numCol;
					int height = (mHeight + spacing) * numRow;
					//getLayoutParams().width = width;
					getLayoutParams().height = height;
				}
			}
			isSet = true;
		}
	}

	public void reset() {
		this.isSet = false;
	}
}
