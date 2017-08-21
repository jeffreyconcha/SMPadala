package com.codepan.callback;

import android.view.View;
import android.view.ViewGroup;

import com.codepan.database.SQLiteAdapter;

public class Interface {

	public interface OnCreateDatabaseCallback {
		void onCreateDatabase(SQLiteAdapter db);
	}

	public interface OnUpgradeDatabaseCallback {
		void onUpgradeDatabase(SQLiteAdapter db,
							   int oldVersion, int newVersion);
	}

	public interface OnCaptureCallback {
		void onCapture(String fileName);
	}

	public interface OnCameraErrorCallback {
		void onCameraError();
	}

	public interface OnSingleTapCallback {
		void onSingleTap();
	}

	public interface OnDoubleTapCallback {
		void onDoubleTap();
	}

	public interface OnItemClickCallback {
		void onItemClick(int position, View view, ViewGroup parent);
	}

	public interface OnItemLongClickCallback {
		void onItemLongClick(int position, View view, ViewGroup parent);
	}

	public interface OnKeyboardDismissCallback {
		void onKeyboardDismiss();
	}

	public interface OnPhotoZoomCallback {
		void onPhotoZoom(boolean isZoom);
	}

	public interface OnFragmentCallback {
		void onFragment(boolean status);
	}

	public interface OnHideKeyboardCallback {
		void onHideKeyboard();
	}

	public interface OnPermissionGrantedCallback {
		void onPermissionGranted(boolean isPermissionGranted);
	}

	public interface OnWheelStopCallback {
		void onWheelStop(float degree);
	}

	public interface OnWheelSpinningCallback {
		void onWheelSpinning(float degree);
	}

	public interface OnRefreshCallback {
		void onRefresh();
	}

	public interface OnBackPressedCallback {
		void onBackPressed();
	}

	public interface OnErrorCallback {
		void onError(String error, String params,
					 String response, boolean showError);
	}

	public interface OnSignCallback {
		void onSign(String fileName);
	}

	public interface OnSetIDCallback {
		void onSetID(String id);
	}

	public interface OnSetUpdateCallback {
		void onSetUpdate(boolean isUpdate);
	}
}
