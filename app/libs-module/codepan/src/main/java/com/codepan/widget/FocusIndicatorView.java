package com.codepan.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class FocusIndicatorView extends View {

	private boolean haveTouch = false;
	private Rect touchArea;
	private Paint paint;

	public FocusIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setColor(0xeed7d7d7);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		haveTouch = false;
	}

	public void setHaveTouch(boolean val, Rect rect) {
		haveTouch = val;
		touchArea = rect;
	}

	public void setColor(int color) {
		paint.setColor(color);
	}


	@Override
	public void onDraw(Canvas canvas) {
		if(haveTouch) {
			canvas.drawRect(touchArea.left, touchArea.top, touchArea.right, touchArea.bottom, paint);
		}
	}
}
