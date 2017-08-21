package com.codepan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.codepan.R;
import com.codepan.cache.TypefaceCache;

public class ProgressWheel extends View {

	private int layout_height = 0;
	private int layout_width = 0;
	private int fullRadius = 100;
	private int circleRadius = 80;
	private int barHeight = 60;
	private int barWidth = 20;
	private int rimWidth = 20;
	private int textSize = 20;
	private float contourSize = 0;
	private int paddingTop = 5;
	private int paddingBottom = 5;
	private int paddingLeft = 5;
	private int paddingRight = 5;
	private int barColor = 0xAA000000;
	private int contourColor = 0xAA000000;
	private int circleColor = 0x00000000;
	private int rimColor = 0xAADDDDDD;
	private int textColor = 0xFF000000;
	private Paint barPaint = new Paint();
	private Paint circlePaint = new Paint();
	private Paint rimPaint = new Paint();
	private Paint textPaint = new Paint();
	private Paint contourPaint = new Paint();
	private RectF rectBounds = new RectF();
	private RectF circleBounds = new RectF();
	private RectF circleOuterContour = new RectF();
	private RectF circleInnerContour = new RectF();
	private int spinSpeed = 2;
	private int delayMillis = 0;
	private int progress = 0;
	private int max = 0;
	boolean isSpinning = false;
	private String[] splitText = {};

	public ProgressWheel(Context context, AttributeSet attrs) {
		super(context, attrs);
		parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.progressWheel));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = 0;
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
		int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();
		if(widthWithoutPadding > heigthWithoutPadding) {
			size = heigthWithoutPadding;
		}
		else {
			size = widthWithoutPadding;
		}
		setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		layout_width = w;
		layout_height = h;
		setupBounds();
		setupPaints();
		invalidate();
	}

	private void setupPaints() {
		barPaint.setColor(barColor);
		barPaint.setAntiAlias(true);
		barPaint.setStyle(Style.STROKE);
		barPaint.setStrokeWidth(barWidth);
		rimPaint.setColor(rimColor);
		rimPaint.setAntiAlias(true);
		rimPaint.setStyle(Style.STROKE);
		rimPaint.setStrokeWidth(rimWidth);
		circlePaint.setColor(circleColor);
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Style.FILL);
		textPaint.setColor(textColor);
		textPaint.setStyle(Style.FILL);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(textSize);
		contourPaint.setColor(contourColor);
		contourPaint.setAntiAlias(true);
		contourPaint.setStyle(Style.STROKE);
		contourPaint.setStrokeWidth(contourSize);
	}

	private void setupBounds() {
		int minValue = Math.min(layout_width, layout_height);
		int xOffset = layout_width - minValue;
		int yOffset = layout_height - minValue;
		paddingTop = this.getPaddingTop() + (yOffset / 2);
		paddingBottom = this.getPaddingBottom() + (yOffset / 2);
		paddingLeft = this.getPaddingLeft() + (xOffset / 2);
		paddingRight = this.getPaddingRight() + (xOffset / 2);
		int width = getWidth();
		int height = getHeight();
		rectBounds = new RectF(paddingLeft,
				paddingTop,
				width - paddingRight,
				height - paddingBottom);
		circleBounds = new RectF(paddingLeft + barWidth,
				paddingTop + barWidth,
				width - paddingRight - barWidth,
				height - paddingBottom - barWidth);
		circleInnerContour = new RectF(circleBounds.left + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.top + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.right - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.bottom - (rimWidth / 2.0f) - (contourSize / 2.0f));
		circleOuterContour = new RectF(circleBounds.left - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.top - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.right + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.bottom + (rimWidth / 2.0f) + (contourSize / 2.0f));
		fullRadius = (width - paddingRight - barWidth) / 2;
		circleRadius = (fullRadius - barWidth) + 1;
	}

	/**
	 * Parse the attributes passed to the view from the XML
	 *
	 * @param a the attributes to parse
	 */
	private void parseAttributes(TypedArray a) {
		barWidth = (int) a.getDimension(R.styleable.progressWheel_barWidth, barWidth);
		rimWidth = (int) a.getDimension(R.styleable.progressWheel_rimWidth, rimWidth);
		spinSpeed = (int) a.getDimension(R.styleable.progressWheel_spinSpeed, spinSpeed);
		delayMillis = a.getInteger(R.styleable.progressWheel_delayMillis, delayMillis);
		progress = a.getInteger(R.styleable.progressWheel_progress, progress);
		max = a.getInteger(R.styleable.progressWheel_max, max);
		if(delayMillis < 0) {
			delayMillis = 0;
		}
		barColor = a.getColor(R.styleable.progressWheel_barColor, barColor);
		barHeight = (int) a.getDimension(R.styleable.progressWheel_barHeight, barHeight);
		textSize = (int) a.getDimension(R.styleable.progressWheel_textSize, textSize);
		textColor = (int) a.getColor(R.styleable.progressWheel_textColor, textColor);
		if(a.hasValue(R.styleable.progressWheel_text)) {
			setText(a.getString(R.styleable.progressWheel_text));
		}
		if(a.hasValue(R.styleable.progressWheel_font)) {
			setFont(a.getString(R.styleable.progressWheel_font));
		}
		rimColor = (int) a.getColor(R.styleable.progressWheel_rimColor, rimColor);
		circleColor = (int) a.getColor(R.styleable.progressWheel_circleColor, circleColor);
		contourColor = a.getColor(R.styleable.progressWheel_contourColor, contourColor);
		contourSize = a.getDimension(R.styleable.progressWheel_contourSize, contourSize);
		a.recycle();
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawArc(circleBounds, 360, 360, false, circlePaint);
		canvas.drawArc(circleBounds, 360, 360, false, rimPaint);
		canvas.drawArc(circleOuterContour, 360, 360, false, contourPaint);
		canvas.drawArc(circleInnerContour, 360, 360, false, contourPaint);
		if(isSpinning) {
			canvas.drawArc(circleBounds, progress - 90, barHeight, false, barPaint);
		}
		else {
			if(max != 0) {
				float angle = ((float) progress / (float) max) * 360f;
				angle = angle > 360 ? 360 : angle;
				canvas.drawArc(circleBounds, -90, angle, false, barPaint);
			}
			else {
				canvas.drawArc(circleBounds, -90, progress, false, barPaint);
			}
		}
		float textHeight = textPaint.descent() - textPaint.ascent();
		float verticalTextOffset = (textHeight / 2) - textPaint.descent();
		for(String s : splitText) {
			float horizontalTextOffset = textPaint.measureText(s) / 2;
			canvas.drawText(s, this.getWidth() / 2 - horizontalTextOffset,
					this.getHeight() / 2 + verticalTextOffset, textPaint);
		}
		if(isSpinning) {
			scheduleRedraw();
		}
	}

	private void scheduleRedraw() {
		progress += spinSpeed;
		if(progress > 360) {
			progress = 0;
		}
		postInvalidateDelayed(delayMillis);
	}

	public boolean isSpinning() {
		return isSpinning;
	}

	public void resetCount() {
		progress = 0;
		setText("0%");
		invalidate();
	}

	public void stopSpinning() {
		isSpinning = false;
		progress = 0;
		postInvalidate();
	}

	public void spin() {
		isSpinning = true;
		postInvalidate();
	}

	public void incrementProgress() {
		isSpinning = false;
		progress++;
		if(progress > 360)
			progress = 0;
		postInvalidate();
	}

	public void setProgress(int progress) {
		isSpinning = false;
		this.progress = progress;
		postInvalidate();
	}

	public void setmax(int max) {
		this.max = max;
	}

	/**
	 * Set the text in the progress bar
	 * Doesn't invalidate the view
	 *
	 * @param text the text to show ('\n' constitutes a new line)
	 */
	public void setText(String text) {
		splitText = text.split("\n");
	}

	public void setFont(String font) {
		Typeface typeface = TypefaceCache.get(getContext().getAssets(), font);
		textPaint.setTypeface(typeface);
	}

	public int getCircleRadius() {
		return circleRadius;
	}

	public void setCircleRadius(int circleRadius) {
		this.circleRadius = circleRadius;
	}

	public int getbarHeight() {
		return barHeight;
	}

	public void setbarHeight(int barHeight) {
		this.barHeight = barHeight;
	}

	public int getBarWidth() {
		return barWidth;
	}

	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
		if(this.barPaint != null) {
			this.barPaint.setStrokeWidth(this.barWidth);
		}
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
		if(this.textPaint != null) {
			this.textPaint.setTextSize(this.textSize);
		}
	}

	public int getPaddingTop() {
		return paddingTop;
	}

	/**
	 * @return the current progress between 0.0 and 1.0,
	 * if the wheel is indeterminate, then the result is -1
	 */
	public float getProgress() {
		return isSpinning ? -1 : progress / 360.0f;
	}

	public void setPaddingTop(int paddingTop) {
		this.paddingTop = paddingTop;
	}

	public int getPaddingBottom() {
		return paddingBottom;
	}

	public void setPaddingBottom(int paddingBottom) {
		this.paddingBottom = paddingBottom;
	}

	public int getPaddingLeft() {
		return paddingLeft;
	}

	public void setPaddingLeft(int paddingLeft) {
		this.paddingLeft = paddingLeft;
	}

	public int getPaddingRight() {
		return paddingRight;
	}

	public void setPaddingRight(int paddingRight) {
		this.paddingRight = paddingRight;
	}

	public int getBarColor() {
		return barColor;
	}

	public void setBarColor(int barColor) {
		this.barColor = barColor;
		if(this.barPaint != null) {
			this.barPaint.setColor(this.barColor);
		}
	}

	public int getCircleColor() {
		return circleColor;
	}

	public void setCircleColor(int circleColor) {
		this.circleColor = circleColor;
		if(this.circlePaint != null) {
			this.circlePaint.setColor(this.circleColor);
		}
	}

	public int getRimColor() {
		return rimColor;
	}

	public void setRimColor(int rimColor) {
		this.rimColor = rimColor;
		if(this.rimPaint != null) {
			this.rimPaint.setColor(this.rimColor);
		}
	}

	public Shader getRimShader() {
		return rimPaint.getShader();
	}

	public void setRimShader(Shader shader) {
		this.rimPaint.setShader(shader);
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
		if(this.textPaint != null) {
			this.textPaint.setColor(this.textColor);
		}
	}

	public int getSpinSpeed() {
		return spinSpeed;
	}

	public void setSpinSpeed(int spinSpeed) {
		this.spinSpeed = spinSpeed;
	}

	public int getRimWidth() {
		return rimWidth;
	}

	public void setRimWidth(int rimWidth) {
		this.rimWidth = rimWidth;
		if(this.rimPaint != null) {
			this.rimPaint.setStrokeWidth(this.rimWidth);
		}
	}

	public int getDelayMillis() {
		return delayMillis;
	}

	public void setDelayMillis(int delayMillis) {
		this.delayMillis = delayMillis;
	}

	public int getContourColor() {
		return contourColor;
	}

	public void setContourColor(int contourColor) {
		this.contourColor = contourColor;
		if(contourPaint != null) {
			this.contourPaint.setColor(this.contourColor);
		}
	}

	public float getContourSize() {
		return this.contourSize;
	}

	public void setContourSize(float contourSize) {
		this.contourSize = contourSize;
		if(contourPaint != null) {
			this.contourPaint.setStrokeWidth(this.contourSize);
		}
	}
}