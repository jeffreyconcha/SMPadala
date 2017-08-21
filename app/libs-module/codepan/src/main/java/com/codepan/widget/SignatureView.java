package com.codepan.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SignatureView extends SurfaceView implements OnTouchListener, Callback {

	private List<List<Dot>> dots = new ArrayList<>();
	public Paint paint;

	public SignatureView(Context context) {
		super(context);
		if(!isInEditMode()) {
			init();
		}
	}

	public SignatureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(!isInEditMode()) {
			init();
		}
	}

	private void init() {
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3);
		this.setOnTouchListener(this);
		this.getHolder().addCallback(this);
	}

	public void setStrokeWidth(float width) {
		paint.setStrokeWidth(width);
		this.invalidate();
	}

	public void setColor(int color) {
		paint.setColor(color);
		this.invalidate();
	}

	public void clear() {
		dots.clear();
		this.invalidate();
	}

	private class Dot {
		public float X = 0;
		public float Y = 0;

		public Dot(float x, float y) {
			X = x;
			Y = y;
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				dots.add(new ArrayList<Dot>());
				dots.get(dots.size() - 1).add(new Dot(event.getX(), event.getY()));
				this.invalidate();
				break;
			case MotionEvent.ACTION_UP:
				dots.get(dots.size() - 1).add(new Dot(event.getX(), event.getY()));
				this.invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				dots.get(dots.size() - 1).add(new Dot(event.getX(), event.getY()));
				this.invalidate();
				break;
		}
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for(List<Dot> dots : this.dots) {
			for(int i = 0; i < dots.size(); i++) {
				if(i - 1 == -1)
					continue;
				canvas.drawLine(dots.get(i - 1).X, dots.get(i - 1).Y, dots.get(i).X, dots.get(i).Y, paint);
			}
		}
	}

	public Bitmap getBitmap(int width, int height) {
		Bitmap src = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(src);
		this.draw(canvas);
		return Bitmap.createScaledBitmap(src, width, height, true);
	}

	public boolean exportFile(String strPath, String strFile, int width, int height) {
		this.invalidate();
		File path = new File(strPath);
		path.mkdirs();
		if(!strFile.toLowerCase(Locale.ENGLISH).contains(".jpg")) {
			strFile += ".jpg";
		}
		try {
			File file = new File(path, strFile);
			FileOutputStream out = new FileOutputStream(file);
			this.setBackgroundColor(Color.WHITE);
			this.getBitmap(width, height).compress(Bitmap.CompressFormat.JPEG, 90, out);
			return true;
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isEmpty() {
		return dots.isEmpty();
	}

	public void setBitmap(Bitmap bitmap) {
		Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(mutableBitmap);
		this.draw(canvas);
	}
}