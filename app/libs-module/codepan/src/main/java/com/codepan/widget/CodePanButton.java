package com.codepan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.codepan.R;
import com.codepan.cache.TypefaceCache;

public class CodePanButton extends Button {

	private Drawable backgroundPressed, backgroundEnabled, backgroundDisabled;
	private int textColorPressed, textColorEnabled, textColorDisabled;
	private boolean enableStatePressed;

	public CodePanButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.codePan);
		enableStatePressed = ta.getBoolean(R.styleable.codePan_enableStatePressed, false);
		textColorPressed = ta.getColor(R.styleable.codePan_textColorPressed, getCurrentTextColor());
		textColorEnabled = ta.getColor(R.styleable.codePan_textColorEnabled, getCurrentTextColor());
		textColorDisabled = ta.getColor(R.styleable.codePan_textColorDisabled, getCurrentTextColor());
		backgroundPressed = ta.getDrawable(R.styleable.codePan_backgroundPressed);
		backgroundEnabled = ta.getDrawable(R.styleable.codePan_backgroundEnabled);
		backgroundDisabled = ta.getDrawable(R.styleable.codePan_backgroundDisabled);
		String typeface = ta.getString(R.styleable.codePan_typeface);
		if(typeface != null) {
			setTypeface(TypefaceCache.get(getContext().getAssets(), typeface));
		}
		setTextColor(textColorEnabled);
		setBackgroundState(backgroundEnabled);
		ta.recycle();
		enableStatePressed(enableStatePressed);
	}

	public void setBackgroundState(Drawable background) {
		if(background != null) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				setBackground(background);
			}
			else {
				setBackgroundDrawable(background);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(enableStatePressed && isEnabled()) {
			switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					setTextColor(textColorPressed);
					setBackgroundState(backgroundPressed);
					break;
				case MotionEvent.ACTION_UP:
					setTextColor(textColorEnabled);
					setBackgroundState(backgroundEnabled);
					break;
			}
			return super.onTouchEvent(event);
		}
		else {
			return super.onTouchEvent(event);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if(enabled) {
			setTextColor(textColorEnabled);
			setBackgroundState(backgroundEnabled);
		}
		else {
			setTextColor(textColorDisabled);
			setBackgroundState(backgroundDisabled);
		}
	}

	public void enableStatePressed(boolean enableStatePressed) {
		this.enableStatePressed = enableStatePressed;
	}
}
