package com.codepan.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

import com.codepan.callback.Interface.OnCameraErrorCallback;
import com.codepan.callback.Interface.OnCaptureCallback;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.FocusIndicatorView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PictureCallback {

	private final int CAMERA_ID = 1;
	private final int OPTIMAL_RESO = 307200;//640 x 480 = 900KB
	private final int HIGH_RESO = 921600;//1280 x 720 = 900KB

	private boolean isFrontCamInverted, hasAutoFocus, isCaptured, hasFlash,
			hasFrontCam, withTimeStamp, isScaled;
	private int cameraSelection, maxWidth, maxHeight, textSize, textColor,
			picWidth, picHeight, orientation;
	private FocusIndicatorView focusIndicatorView;
	private OnCaptureCallback captureCallback;
	private SurfaceHolder surfaceHolder;
	private String folder, prefix;
	private boolean isLandscape;
	private Typeface typeface;
	private Parameters params;
	private String flashMode;
	private Context context;
	private Camera camera;

	@SuppressWarnings("deprecation")
	public CameraSurfaceView(Context context, OnCameraErrorCallback cameraErrorCallback,
							 int cameraSelection, String flashMode, String folder,
							 int maxWidth, int maxHeight) {
		super(context);
		this.hasFrontCam = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
		this.hasAutoFocus = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
		this.camera = getAvailableCamera(cameraSelection);
		if(camera != null) {
			this.cameraSelection = cameraSelection;
			this.flashMode = flashMode;
			this.maxHeight = maxHeight;
			this.maxWidth = maxWidth;
			this.context = context;
			this.folder = folder;
			this.params = camera.getParameters();
			this.surfaceHolder = getHolder();
			this.surfaceHolder.addCallback(this);
			this.orientation = context.getResources().getConfiguration().orientation;
			this.isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;
			this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			List<String> flashModeList = params.getSupportedFlashModes();
			if(flashModeList != null && !flashModeList.isEmpty()) {
				for(String mode : flashModeList) {
					if(mode.equals(Parameters.FLASH_MODE_ON)) {
						this.hasFlash = true;
						break;
					}
				}
			}
		}
		else {
			if(cameraErrorCallback != null) {
				cameraErrorCallback.onCameraError();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if(hasAutoFocus) {
			camera.getParameters().setFocusMode(Parameters.FOCUS_MODE_AUTO);
			camera.autoFocus(null);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			params = camera.getParameters();
			if(Build.VERSION.SDK_INT >= 8) {
				int result = 90;
				if(hasFrontCam && cameraSelection == CameraInfo.CAMERA_FACING_FRONT) {
					CameraInfo info = new CameraInfo();
					Camera.getCameraInfo(CAMERA_ID, info);
					result = (info.orientation) % 360;
					result = (360 - result) % 360;
					isFrontCamInverted = result == 270;
				}
				else {
					result = 90;
				}
				result = isLandscape ? 0 : result;
				camera.setDisplayOrientation(result);
			}
			else {
				params.set("orientation", "portrait");
			}
			if(hasFlash && cameraSelection == CameraInfo.CAMERA_FACING_BACK) {
				params.setFlashMode(flashMode);
			}
			List<Size> previewSizes = params.getSupportedPreviewSizes();
			List<Size> pictureSizes = params.getSupportedPictureSizes();
			int previewWidth = 0;
			int previewHeight = 0;
			int pictureWidth = 0;
			int pictureHeight = 0;
			float maxOutputHeight = 0f;
			float preferredRatio = (float) maxWidth / (float) maxHeight;
			for(Size s : previewSizes) {
				float ratio = (float) s.width / (float) s.height;
				float outputHeight = isLandscape ? (float) maxWidth / ratio : (float) maxWidth * ratio;
				if(maxOutputHeight < outputHeight) {
					maxOutputHeight = outputHeight;
					previewHeight = s.height;
					previewWidth = s.width;
					if(ratio <= preferredRatio) {
						break;
					}
				}
			}
			List<Size> sizes = new ArrayList<>();
			for(Size s : pictureSizes) {
				int resolution = s.width * s.height;
				if(resolution <= HIGH_RESO && resolution >= OPTIMAL_RESO) {
					sizes.add(s);
				}
			}
			if(sizes.isEmpty()) {
				sizes = pictureSizes;
			}
			int count = sizes.size();
			Size first = sizes.get(0);
			Size last = sizes.get(count - 1);
			if(first.width * first.height > last.width * last.height) {
				pictureWidth = first.width;
				pictureHeight = first.height;
			}
			else {
				pictureWidth = last.width;
				pictureHeight = last.height;
			}
			params.setRotation(0);
			params.setPreviewSize(previewWidth, previewHeight);
			params.setPictureSize(pictureWidth, pictureHeight);
			camera.setParameters(params);
			camera.startPreview();
		}
		catch(Exception e) {
			e.printStackTrace();
			camera.release();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		if(camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
		surfaceHolder.removeCallback(this);
	}

	public Camera getAvailableCamera(int cameraSelection) {
		Camera camera = null;
		try {
			if(Camera.getNumberOfCameras() == 2) {
				camera = Camera.open(cameraSelection);
			}
			else {
				camera = Camera.open(0);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return camera;
	}

	public void takePicture() {
		if(camera != null) {
			isCaptured = true;
			camera.takePicture(null, null, this);
		}
	}

	public void reset() {
		if(camera != null) {
			camera.startPreview();
			isCaptured = false;
		}
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Bitmap src = BitmapFactory.decodeByteArray(data, 0, data.length);
		String fileName = System.currentTimeMillis() + ".jpg";
		if(prefix != null) {
			fileName = prefix + "-" + fileName;
		}
		if(isScaled) {
			src = scale(src);
		}
		Matrix matrix = new Matrix();
		matrix.postRotate(getImageRotation());
		Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
				src.getHeight(), matrix, true);
		if(withTimeStamp) {
			String dDate = CodePanUtils.getDate();
			String dTime = CodePanUtils.getTime();
			String date = CodePanUtils.getCalendarDate(dDate, true, true);
			String time = CodePanUtils.getNormalTime(dTime, true);
			String stamp = date + " " + time;
			bitmap = timeStamp(bitmap, stamp);
		}
		try {
			File dir = context.getDir(folder, Context.MODE_PRIVATE);
			File file = new File(dir, fileName);
			if(!dir.exists()) {
				dir.mkdir();
			}
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		captureCallback.onCapture(fileName);
	}

	public void setOnCaptureCallback(OnCaptureCallback captureCallback) {
		this.captureCallback = captureCallback;
	}

	private Bitmap timeStamp(Bitmap bitmap, String text) {
		float margin = 10f;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float y = height - textSize + margin;
		float ratio = (float) textSize / (float) maxHeight;
		float textSize = ratio * (float) height;
		Bitmap dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(dest);
		int shadow = Color.argb(96, 0, 0, 0);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTypeface(typeface);
		paint.setTextSize(textSize);
		paint.setColor(textColor);
		paint.setShadowLayer(2f, 2f, 2f, shadow);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawBitmap(bitmap, 0f, 0f, null);
		canvas.drawText(text, margin, y, paint);
		return dest;
	}

	private Bitmap scale(Bitmap bitmap) {
		int width = 0;
		int height = 0;
		int srcWidth = bitmap.getWidth();
		int srcHeight = bitmap.getHeight();
		if(srcWidth > picWidth) {
			width = picWidth;
			float ratio = (float) picWidth / (float) srcWidth;
			height = (int) ((float) bitmap.getHeight() * ratio);
		}
		else {
			if(srcHeight > picHeight) {
				height = picHeight;
				float ratio = (float) picHeight / (float) srcHeight;
				width = (int) ((float) bitmap.getWidth() * ratio);
			}
		}
		return Bitmap.createScaledBitmap(bitmap, width, height, false);
	}

	public void timeStamp(Typeface typeface, int textSize, int textColor) {
		this.withTimeStamp = true;
		this.textColor = textColor;
		this.textSize = textSize;
		this.typeface = typeface;
	}

	public void setMaxPictureSize(int width, int height) {
		this.isScaled = true;
		this.picWidth = width;
		this.picHeight = height;
	}

	public boolean hasFrontCamera() {
		return hasFrontCam;
	}

	public boolean isCaptured() {
		return isCaptured;
	}

	public boolean isFrontCamInverted() {
		return this.isFrontCamInverted;
	}

	public Camera getCamera() {
		return this.camera;
	}

	public int getNoOfCamera() {
		return Camera.getNumberOfCameras();
	}

	public boolean hasFlash() {
		return this.hasFlash;
	}

	public boolean hasAutoFocus() {
		return this.hasAutoFocus;
	}

	public void stopCamera() {
		if(camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
		if(surfaceHolder != null) {
			surfaceHolder.removeCallback(this);
		}
	}

	/**
	 * @param container must be nested in LinearLayout to enable
	 *                  centerCrop scale
	 */
	public void fullScreenToContainer(ViewGroup container) {
		if(camera != null) {
			Parameters params = camera.getParameters();
			float maxRatio = 0f;
			float maxOutputHeight = 0f;
			float preferredRatio = (float) maxWidth / (float) maxHeight;
			List<Size> sizes = params.getSupportedPreviewSizes();
			for(Size s : sizes) {
				float ratio = (float) s.width / (float) s.height;
				float outputHeight = isLandscape ? (float) maxWidth / ratio : (float) maxWidth * ratio;
				if(maxOutputHeight < outputHeight) {
					maxOutputHeight = outputHeight;
					maxRatio = ratio;
					if(ratio <= preferredRatio) {
						break;
					}
				}
			}
			LayoutParams lp = (LayoutParams) container.getLayoutParams();
			if(maxOutputHeight >= maxHeight) {
				lp.width = maxWidth;
				lp.height = (int) maxOutputHeight;
			}
			else {
				float maxOutputWidth = (float) maxHeight / maxRatio;
				lp.height = maxHeight;
				lp.width = (int) maxOutputWidth;
			}
		}
	}

	public int getImageRotation() {
		int rotation = 0;
		if(getNoOfCamera() == 2) {
			switch(cameraSelection) {
				case CameraInfo.CAMERA_FACING_FRONT:
					if(isFrontCamInverted) {
						rotation = 90;
					}
					else {
						rotation = 270;
					}
					break;
				case CameraInfo.CAMERA_FACING_BACK:
					rotation = 90;
					break;
			}
		}
		else {
			if(hasFrontCamera()) {
				rotation = 270;
			}
			else {
				rotation = 90;
			}
		}
		rotation = isLandscape ? 0 : rotation;
		return rotation;
	}

	@SuppressLint("NewApi")
	public void tapToFocus(final Rect tfocusRect) {
		if(camera != null) {
			try {
				List<Area> focusList = new ArrayList<>();
				Area focusArea = new Area(tfocusRect, 1000);
				focusList.add(focusArea);
				Camera.Parameters params = camera.getParameters();
				params.setFocusAreas(focusList);
				params.setMeteringAreas(focusList);
				camera.setParameters(params);
				camera.autoFocus(new AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						if(success) {
							String focusMode = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
							Parameters params = camera.getParameters();
							List<String> focusModeList = params.getSupportedFocusModes();
							if(focusModeList.contains(focusMode)) {
								if(!params.getFocusMode().equals(focusMode)) {
									params.setFocusMode(focusMode);
									if(params.getMaxNumFocusAreas() > 0) {
										params.setFocusAreas(null);
									}
									camera.setParameters(params);
									camera.startPreview();
								}
							}
						}
					}
				});
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(hasAutoFocus && cameraSelection == CameraInfo.CAMERA_FACING_BACK) {
			final int SIZE = 60;
			final int DELAY = 1000;
			int width = this.getWidth();
			int height = this.getHeight();
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				float x = event.getX();
				float y = event.getY();
				Rect touchRect = new Rect(
						(int) (x - SIZE),
						(int) (y - SIZE),
						(int) (x + SIZE),
						(int) (y + SIZE));
				int left = touchRect.left * 2000 / width - 1000;
				int top = touchRect.top * 2000 / height - 1000;
				int right = touchRect.right * 2000 / width - 1000;
				int bottom = touchRect.bottom * 2000 / height - 1000;
				final Rect targetFocusRect = new Rect(left, top, right, bottom);
				tapToFocus(targetFocusRect);
				if(focusIndicatorView != null) {
					focusIndicatorView.setHaveTouch(true, touchRect);
					focusIndicatorView.invalidate();
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							focusIndicatorView.setHaveTouch(false, new Rect(0, 0, 0, 0));
							focusIndicatorView.invalidate();
						}
					}, DELAY);
				}
			}
		}
		else {
			super.onTouchEvent(event);
		}
		return false;
	}

	public void setFocusIndicatorView(FocusIndicatorView focusIndicatorView) {
		this.focusIndicatorView = focusIndicatorView;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}