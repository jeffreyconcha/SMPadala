package com.codepan.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;

import static android.os.Build.VERSION_CODES;

public class FullscreenFragment extends Fragment implements Runnable {

	private Handler handler = new Handler();
	private boolean withNavigation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerSystemUiVisibility();
	}

	public boolean isImmersiveAvailable() {
		return VERSION.SDK_INT >= 19;
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus) {
			handler.removeCallbacks(this);
			handler.postDelayed(this, 300);
		}
		else {
			handler.removeCallbacks(this);
		}
	}

	public void onKeyDown(int keyCode) {
		if((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
				keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
			handler.removeCallbacks(this);
			handler.postDelayed(this, 500);
		}
	}

	@Override
	public void onStop() {
		handler.removeCallbacks(this);
		super.onStop();
	}

	@Override
	public void run() {
		setFullScreen(getActivity());
	}

	private void setFullScreen(Activity activity) {
		int flags = 0;
		if(VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
		}
		if(isImmersiveAvailable()) {
			if(!withNavigation) {
				flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
						View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
			else {
				flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
		}
		activity.getWindow().getDecorView().setSystemUiVisibility(flags);
	}

	private void exitFullScreen(Activity activity) {
		activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}

	protected void exitFullScreen() {
		unregisterSystemUiVisibility();
		exitFullScreen(getActivity());
	}

	protected void setFullScreen() {
		registerSystemUiVisibility();
		setFullScreen(getActivity());
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void registerSystemUiVisibility() {
		final View decorView = getActivity().getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
					setFullScreen(getActivity());
				}
			}
		});
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void unregisterSystemUiVisibility() {
		final View decorView = getActivity().getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(null);
	}

	public void setWithNavigation(boolean withNavigation) {
		this.withNavigation = withNavigation;
	}
}
