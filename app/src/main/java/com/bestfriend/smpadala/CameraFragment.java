package com.bestfriend.smpadala;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bestfriend.callback.Interface;
import com.bestfriend.callback.Interface.OnRetakeCameraCallback;
import com.bestfriend.callback.Interface.OnUsePhotoCallback;
import com.bestfriend.constant.App;
import com.codepan.cache.TypefaceCache;
import com.codepan.callback.Interface.OnCameraErrorCallback;
import com.codepan.callback.Interface.OnCaptureCallback;
import com.codepan.callback.Interface.OnFragmentCallback;
import com.codepan.camera.CameraSurfaceView;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.FocusIndicatorView;

public class CameraFragment extends Fragment implements OnClickListener, OnCaptureCallback,
        OnRetakeCameraCallback, OnCameraErrorCallback {

    private final String flashMode = Camera.Parameters.FLASH_MODE_OFF;
    private final long TRANS_DELAY = 300;
    private final long FADE_DELAY = 750;

    private CodePanButton btnCaptureCamera, btnSwitchCamera, btnBackCamera;
    private int cameraSelection, maxWidth, maxHeight;
    private OnUsePhotoCallback usePhotoCallback;
    private OnFragmentCallback fragmentCallback;
    private FragmentTransaction transaction;
    private CameraSurfaceView surfaceView;
    private FocusIndicatorView dvCamera;
    private FragmentManager manager;
    private FrameLayout flCamera;
    private MainActivity main;
    private Typeface typeface;
    private SQLiteAdapter db;
    private View vCamera;

    @Override
    public void onStart() {
        super.onStart();
        setOnBackStack(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        setOnBackStack(false);
    }

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
        String font = getString(R.string.proxima_nova_mid);
        typeface = TypefaceCache.get(main.getAssets(), font);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_layout, container, false);
        dvCamera = (FocusIndicatorView) view.findViewById(R.id.dvCamera);
        flCamera = (FrameLayout) view.findViewById(R.id.flCamera);
        btnCaptureCamera = (CodePanButton) view.findViewById(R.id.btnCaptureCamera);
        btnSwitchCamera = (CodePanButton) view.findViewById(R.id.btnSwitchCamera);
        btnBackCamera = (CodePanButton) view.findViewById(R.id.btnBackCamera);
        vCamera = view.findViewById(R.id.vCamera);
        btnCaptureCamera.setOnClickListener(this);
        btnSwitchCamera.setOnClickListener(this);
        btnBackCamera.setOnClickListener(this);
        resetCamera(TRANS_DELAY);
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
        if(vCamera != null) vCamera.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(surfaceView != null) surfaceView.stopCamera();
                Resources res = main.getResources();
                int color = res.getColor(R.color.white);
                int width = res.getInteger(R.integer.pic_width);
                int height = res.getInteger(R.integer.pic_height);
                int size = res.getDimensionPixelSize(R.dimen.fifteen);
                surfaceView = new CameraSurfaceView(main, CameraFragment.this,
                        cameraSelection, flashMode, App.FOLDER, maxWidth, maxHeight);
                surfaceView.setOnCaptureCallback(CameraFragment.this);
                surfaceView.setFocusIndicatorView(dvCamera);
                surfaceView.fullScreenToContainer(flCamera);
                surfaceView.timeStamp(typeface, size, color);
                surfaceView.setMaxPictureSize(width, height);
                if(flCamera.getChildCount() > 1) {
                    flCamera.removeViewAt(0);
                }
                flCamera.addView(surfaceView, 0);
                CodePanUtils.fadeOut(vCamera, FADE_DELAY);
                if(surfaceView.getNoOfCamera() == 1) {
                    btnSwitchCamera.setVisibility(View.GONE);
                }
            }
        }, delay);
    }

    @Override
    public void onCameraError() {
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle("Camera Failed");
        alert.setDialogMessage("Failed to load camera please try to restart your device.");
        alert.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.popBackStack();
                manager.popBackStack();
            }
        });
        transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);
        transaction.add(R.id.rlMain, alert);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void defaultFront(boolean isFront) {
        cameraSelection = isFront ? CameraInfo.CAMERA_FACING_FRONT :
                CameraInfo.CAMERA_FACING_BACK;
    }

    public void setOnFragmentCallback(OnFragmentCallback fragmentCallback) {
        this.fragmentCallback = fragmentCallback;
    }

    private void setOnBackStack(boolean isOnBackStack) {
        if(fragmentCallback != null) {
            fragmentCallback.onFragment(isOnBackStack);
        }
    }

    public void setOnUsePhotoCallback(OnUsePhotoCallback usePhotoCallback) {
        this.usePhotoCallback = usePhotoCallback;
    }
}
