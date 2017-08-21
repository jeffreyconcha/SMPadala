package com.codepan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.codepan.R;
import com.codepan.constant.Reference;

public class SquareImageView extends ImageView {

	private int reference;

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.codePan);
		reference = ta.getInt(R.styleable.codePan_reference, Reference.DYNAMIC);
		ta.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
