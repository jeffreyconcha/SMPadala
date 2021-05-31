package com.bestfriend.smpadala;

import android.content.res.Resources;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bestfriend.callback.Interface.OnRetakeCameraCallback;
import com.bestfriend.callback.Interface.OnUsePhotoCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.DialogTag;
import com.codepan.app.CPFragment;
import com.codepan.callback.Interface.OnCameraErrorCallback;
import com.codepan.callback.Interface.OnCaptureCallback;
import com.codepan.database.SQLiteAdapter;
import com.codepan.model.StampData;
import com.codepan.permission.PermissionEvents;
import com.codepan.permission.PermissionHandler;
import com.codepan.permission.PermissionType;
import com.codepan.utils.CodePanUtils;
import com.codepan.utils.DateTime;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.FocusIndicatorView;
import com.codepan.widget.camera.CameraSurfaceView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CameraFragment extends CPFragment implements OnClickListener, OnCaptureCallback,
    OnRetakeCameraCallback, OnCameraErrorCallback, PermissionEvents {

    private final String flashMode = Camera.Parameters.FLASH_MODE_OFF;
    private final long TRANS_DELAY = 300;
    private final long FADE_DELAY = 750;

    private CodePanButton btnCaptureCamera, btnSwitchCamera, btnBackCamera;
    private int cameraSelection, maxWidth, maxHeight;
    private OnUsePhotoCallback usePhotoCallback;
    private CameraSurfaceView surfaceView;
    private FocusIndicatorView dvCamera;
    private FrameLayout flCamera;
    private MainActivity main;
    private SQLiteAdapter db;
    private View vCamera;

    @Override
    public void onResume() {
        super.onResume();
        if(surfaceView != null && surfaceView.getCamera() == null) {
            resetCamera(0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) getActivity();
        db = main.getDatabase();
        db.openConnection();
        maxWidth = CodePanUtils.getMaxWidth(main);
        maxHeight = CodePanUtils.getMaxHeight(main);
        manager = main.getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_layout, container, false);
        dvCamera = view.findViewById(R.id.dvCamera);
        flCamera = view.findViewById(R.id.flCamera);
        btnCaptureCamera = view.findViewById(R.id.btnCaptureCamera);
        btnSwitchCamera = view.findViewById(R.id.btnSwitchCamera);
        btnBackCamera = view.findViewById(R.id.btnBackCamera);
        vCamera = view.findViewById(R.id.vCamera);
        btnCaptureCamera.setOnClickListener(this);
        btnSwitchCamera.setOnClickListener(this);
        btnBackCamera.setOnClickListener(this);
        getHandler().checkPermissions();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnCaptureCamera:
                if(surfaceView != null && !surfaceView.isCaptured()) {
                    surfaceView.takePicture();
                }
                break;
            case R.id.btnSwitchCamera:
                if(cameraSelection == CameraInfo.CAMERA_FACING_FRONT) {
                    cameraSelection = CameraInfo.CAMERA_FACING_BACK;
                }
                else {
                    cameraSelection = CameraInfo.CAMERA_FACING_FRONT;
                }
                resetCamera(0);
                break;
            case R.id.btnBackCamera:
                manager.popBackStack();
                break;
        }
    }

    @Override
    public void onCapture(String fileName) {
        manager.popBackStack();
        if(usePhotoCallback != null) {
            usePhotoCallback.onUsePhoto(fileName);
        }
    }

    @Override
    public void onRetakeCamera() {
        manager.popBackStack();
        resetCamera(TRANS_DELAY);
    }

    public void resetCamera(long delay) {
        if (vCamera != null) vCamera.setVisibility(View.VISIBLE);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (surfaceView != null) surfaceView.stopCamera();
            Resources res = main.getResources();
            int width = res.getInteger(R.integer.pic_width);
            int height = res.getInteger(R.integer.pic_height);
            surfaceView = new CameraSurfaceView(main, CameraFragment.this,
                cameraSelection, flashMode, App.FOLDER, maxWidth, maxHeight);
            surfaceView.setOnCaptureCallback(CameraFragment.this);
            surfaceView.setFocusIndicatorView(dvCamera);
            surfaceView.fullScreenToContainer(flCamera);
            DateTime now = DateTime.Companion.now();
            String date = now.getReadableDate(true, true, false);
            String time = now.getReadableTime(true);
            ArrayList<StampData> stampList = new ArrayList<>();
            StampData stamp = new StampData();
            stamp.data = date + " " + time;
            stamp.alignment = Paint.Align.LEFT;
            surfaceView.setStampList(stampList);
            surfaceView.setMaxPictureSize(width, height);
            if (flCamera.getChildCount() > 1) {
                flCamera.removeViewAt(0);
            }
            flCamera.addView(surfaceView, 0);
            CodePanUtils.fadeOut(vCamera, FADE_DELAY);
            if (!surfaceView.canSwitchCamera()) {
                btnSwitchCamera.setVisibility(View.GONE);
            }
        }, delay);
    }

    @Override
    public void onCameraError() {
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle("Camera Failed");
        alert.setDialogMessage("Failed to load camera please try to restart your device.");
        alert.setPositiveButton("OK", view -> {
            manager.popBackStack();
            manager.popBackStack();
        });
        transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
            R.anim.fade_in, R.anim.fade_out);
        transaction.add(R.id.rlMain, alert);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setOnUsePhotoCallback(OnUsePhotoCallback usePhotoCallback) {
        this.usePhotoCallback = usePhotoCallback;
    }

    @NotNull
    @Override
    public PermissionHandler getHandler() {
        return new PermissionHandler(main, this,
            PermissionType.CAMERA,
            PermissionType.FILES_AND_MEDIA
        );
    }

    @Override
    public void onPermissionsResult(@NotNull PermissionHandler handler, boolean isGranted) {
        if (main.inBackStack(DialogTag.PERMISSION)) {
            manager.popBackStack();
        }
        if (isGranted) {
            resetCamera(TRANS_DELAY);
        }
        else {
            manager.popBackStack();
        }
    }

    @Override
    public void onShowPermissionRationale(@NotNull PermissionHandler handler, @NotNull PermissionType permission) {
        if (main.notInBackStack(DialogTag.PERMISSION)) {
            String appName = getString(R.string.app_name);
            String message = null;
            switch (permission) {
                case CAMERA:
                    message = text(R.string.permission_camera, appName);
                    break;
                case FILES_AND_MEDIA:
                    message = text(R.string.permission_files_and_media, appName);
                    break;
            }
            final AlertDialogFragment alert = new AlertDialogFragment();
            alert.setDialogTitle(R.string.permission_required);
            alert.setDialogMessage(message);
            alert.setPositiveButton(getString(R.string.settings), v -> {
                manager.popBackStack();
                handler.goToSettings();
            });
            alert.setNegativeButton(getString(R.string.cancel), v -> {
                manager.popBackStack();
                manager.popBackStack();
            });
            transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);
            transaction.add(R.id.rlMain, alert, DialogTag.PERMISSION);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

}
