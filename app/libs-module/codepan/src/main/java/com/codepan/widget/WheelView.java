package com.codepan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.codepan.R;
import com.codepan.callback.Interface.OnWheelSpinningCallback;
import com.codepan.callback.Interface.OnWheelStopCallback;

public class WheelView extends View {

	private final double MAX_SPEED = 180D;
	private final long SPIN_DURATION = 3000;
	private final long FPS = 10;

	private OnWheelSpinningCallback wheelSpinningCallback;
	private OnWheelStopCallback wheelStopCallback;
	private double deceleration = 0D;
	private double velocity = 0D;
	private double previous = 0D;
	private double current = 0D;
	private double delta = 0D;
	private long spinDuration = 0;
	private boolean isCancelled;
	private boolean isSpinning;
	private float degree = 0;
	private Bitmap bitmap;
	private Bitmap scaled;

	public WheelView(Context context) {
		super(context);
	}

	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.wheelView);
		int resId = ta.getResourceId(R.styleable.wheelView_src, 0);
		this.bitmap = BitmapFactory.decodeResource(ta.getResources(), resId);
		ta.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(scaled == null) {
			int width = getMeasuredWidth();
			int height = getMeasuredWidth();
			this.scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);
		}
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float left = getWidth() / 2 - getWidth() / 2;
		float top = getHeight() / 2 - getWidth() / 2;
		canvas.rotate(degree, getWidth() / 2, getHeight() / 2);
		canvas.drawBitmap(scaled, left, top, null);
	}

	public void spin() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				velocity -= deceleration;
				degree += velocity;
				spinDuration -= FPS;
				invalidate();
				if(spinDuration > 0) {
					handler.postDelayed(this, FPS);
					if(wheelSpinningCallback != null) {
						float current = degree % 360;
						wheelSpinningCallback.onWheelSpinning(current);
					}
					isSpinning = true;
				}
				else {
					float result = degree % 360;
					if(wheelStopCallback != null && !isCancelled) {
						wheelStopCallback.onWheelStop(result);
					}
					degree = result;
					deceleration = 0D;
					isSpinning = false;
					isCancelled = false;
				}
			}
		}, FPS);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		final float xc = getWidth() / 2;
		final float yc = getHeight() / 2;
		long start = 0L;
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				start = SystemClock.elapsedRealtime();
				current = Math.toDegrees(Math.atan2(x - xc, yc - y));
				current = current < 0 ? 360 + current : current;
				if(isSpinning) {
					spinDuration = 0;
					degree = degree % 360;
					isCancelled = true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				previous = current;
				current = Math.toDegrees(Math.atan2(x - xc, yc - y));
				current = current < 0 ? 360 + current : current;
				double diff = current - previous;
				diff = Math.abs(diff) > 270 ? 360 - Math.abs(diff) : diff;
				degree += diff;
				delta += diff;
				invalidate();
				if(wheelSpinningCallback != null) {
					float current = degree % 360;
					wheelSpinningCallback.onWheelSpinning(current);
				}
				break;
			case MotionEvent.ACTION_UP:
				long duration = SystemClock.elapsedRealtime() - start;
				long time = duration / 1000000;
				velocity = (delta / time) * FPS;
				velocity = velocity > MAX_SPEED ? MAX_SPEED : velocity;
				spinDuration = SPIN_DURATION;
				deceleration = velocity / (SPIN_DURATION / FPS);
				previous = 0D;
				current = 0D;
				delta = 0D;
				spin();
				break;
		}
		return true;
	}

	public void setOnWheelStopCallback(OnWheelStopCallback wheelStopCallback) {
		this.wheelStopCallback = wheelStopCallback;
	}

	public void setOnWheelSpinningCallback(OnWheelSpinningCallback wheelSpinningCallback) {
		this.wheelSpinningCallback = wheelSpinningCallback;
	}
}
