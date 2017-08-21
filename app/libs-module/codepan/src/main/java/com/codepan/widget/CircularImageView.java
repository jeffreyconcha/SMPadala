package com.codepan.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.codepan.R;

public class CircularImageView extends ImageView {

	private BitmapShader shader;
	private Paint paintBorder;
	private int borderWidth;
	private int viewWidth;
	private int viewHeight;
	private Bitmap image;
	private Paint paint;

	public CircularImageView(Context context) {
		super(context);
		setup();
	}

	public CircularImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
		setup();
	}

	public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
		setup();
	}

	public void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.circular);
		borderWidth = ta.getInteger(R.styleable.circular_borderWidth, 0);
		ta.recycle();
	}

	private void setup() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paintBorder = new Paint();
		setBorderColor(Color.WHITE);
		paintBorder.setAntiAlias(true);
		this.setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
	}

	public void setShadowLayer(float radius, float dx, float dy, int color) {
		paintBorder.setShadowLayer(radius, dx, dy, color);
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
		this.invalidate();
	}

	public void setBorderColor(int borderColor) {
		if(paintBorder != null)
			paintBorder.setColor(borderColor);
		this.invalidate();
	}

	private void loadBitmap() {
		BitmapDrawable bitmapDrawable = (BitmapDrawable) this.getDrawable();
		if(bitmapDrawable != null)
			image = bitmapDrawable.getBitmap();
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		loadBitmap();
		if(image != null) {
			shader = new BitmapShader(Bitmap.createScaledBitmap(image,
					canvas.getWidth(), canvas.getHeight(), false),
					Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			paint.setShader(shader);
			int circleCenter = viewWidth / 2;
			canvas.drawCircle(circleCenter + borderWidth, circleCenter
							+ borderWidth, circleCenter + borderWidth - 4.0f,
					paintBorder);
			canvas.drawCircle(circleCenter + borderWidth, circleCenter
					+ borderWidth, circleCenter - 4.0f, paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureWidth(widthMeasureSpec);
		int height = measureHeight(heightMeasureSpec);
		viewWidth = width - (borderWidth * 2);
		viewHeight = height - (borderWidth * 2);
		setMeasuredDimension(width, height);
	}

	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if(specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		else {
			result = viewWidth;
		}
		return result;
	}

	private int measureHeight(int measureSpecHeight) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpecHeight);
		int specSize = MeasureSpec.getSize(measureSpecHeight);
		if(specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		else {
			result = viewHeight;
		}
		return (result + 2);
	}
}