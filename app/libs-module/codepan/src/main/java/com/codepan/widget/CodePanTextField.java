package com.codepan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.codepan.R;
import com.codepan.cache.TypefaceCache;
import com.codepan.callback.Interface.OnKeyboardDismissCallback;
import com.codepan.utils.CodePanUtils;

public class CodePanTextField extends EditText {

	private Drawable backgroundPressed, backgroundEnabled, backgroundDisabled;
	private boolean enableStatePressed, autoHideKeyboard, autoClearFocus;
	private int textColorPressed, textColorEnabled, textColorDisabled;
	private OnKeyboardDismissCallback keyboardDismissCallback;
	private OnFocusChangeListener focusChangeListener;
	private Context context;

	public CodePanTextField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
		this.context = context;
	}

	public void init(final Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.codePan);
		autoHideKeyboard = ta.getBoolean(R.styleable.codePan_autoHideKeyboard, false);
		autoClearFocus = ta.getBoolean(R.styleable.codePan_autoClearFocus, false);
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
		enableStatePressed(enableStatePressed);
		autoHideKeyboard(autoHideKeyboard);
		ta.recycle();
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
			return true;
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

	public void autoHideKeyboard(boolean autoHideKeyboard) {
		if(autoHideKeyboard) {
			focusChangeListener = new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus && (focusSearch(FOCUS_DOWN) == null ||
							(focusSearch(FOCUS_DOWN) != null && !focusSearch(FOCUS_DOWN).hasFocus()))) {
						CodePanUtils.hideKeyboard(v, context);
					}
				}
			};
			setOnFocusChangeListener(focusChangeListener);
		}
	}

	public void enableStatePressed(boolean enableStatePressed) {
		this.enableStatePressed = enableStatePressed;
	}

	public void setOnKeyboardDismissCallback(OnKeyboardDismissCallback keyboardDismissCallback) {
		this.keyboardDismissCallback = keyboardDismissCallback;
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				if(event.getAction() == KeyEvent.ACTION_UP) {
					if(keyboardDismissCallback != null) {
						keyboardDismissCallback.onKeyboardDismiss();
					}
					if(autoClearFocus) {
						clearFocus();
					}
				}
				break;
		}
		return super.dispatchKeyEvent(event);
	}
}
