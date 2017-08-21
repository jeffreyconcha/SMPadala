package com.codepan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.codepan.R;
import com.codepan.constant.Reference;

public class LockableScrollView extends ScrollView {

	private boolean isScrollable = true;
	private boolean isSquare = false;
	private int reference;

	public LockableScrollView(Context context) {
		super(context);
	}

	public LockableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.codePan);
		reference = ta.getInt(R.styleable.codePan_reference, Reference.DYNAMIC);
		isSquare = ta.getBoolean(R.styleable.codePan_setSquare, false);
		isScrollable = ta.getBoolean(R.styleable.codePan_setScrollable, false);
		ta.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(isSquare) {
			int width = getMeasuredWidth();
			int height = getMeasuredHeight();
			int dimension = 0;
			switch(reference) {
				case Reference.DYNAMIC:
					dimension = width > height ? width : height;
					break;
				case Reference.WIDTH:
					dimension = width;
					break;
				case Reference.HEIGHT:
					dimension = height;
					break;
			}
			setMeasuredDimension(dimension, dimension);
		}
	}

	public void setScrollingEnabled(boolean enabled) {
		isScrollable = enabled;
	}

	public boolean isScrollable() {
		return isScrollable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				return isScrollable && super.onTouchEvent(ev);
			default:
				return super.onTouchEvent(ev);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(!isScrollable) return false;
		else return super.onInterceptTouchEvent(ev);
	}
}
