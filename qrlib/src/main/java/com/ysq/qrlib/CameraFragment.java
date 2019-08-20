package com.ysq.qrlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public abstract class CameraFragment extends Fragment implements Camera.PreviewCallback
        , SurfaceHolder.Callback {
    protected int[] mPreviewSize;

    private SurfaceView mSurfaceView;

    private FrameLayout mContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContainer = (FrameLayout) inflater.inflate(R.layout.fragment_camera, container, false);
        //noinspection ConstantConditions
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 88);
        } else {
            openCamera();
        }
        return mContainer;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 88 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }


    private void openCamera() {
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mPreviewSize = CameraManage.get().getPreviewSize();
                        addSurfaceView();
                        mContainer.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });
    }

    public void onSurfaceAdded() {

    }

    public ViewGroup getContainer() {
        return mContainer;
    }

    /**
     * 添加surfaceView
     */
    private void addSurfaceView() {
        int width = mContainer.getWidth();
        int height = mContainer.getHeight();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);
        if (width * mPreviewSize[0] < height * mPreviewSize[1]) {
            layoutParams.width = mPreviewSize[1] * height / mPreviewSize[0];
            layoutParams.height = height;
        } else {
            layoutParams.width = width;
            layoutParams.height = mPreviewSize[0] * width / mPreviewSize[1];
        }
        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.setLayoutParams(layoutParams);
        mSurfaceView.getHolder().addCallback(this);
        mContainer.addView(mSurfaceView);
        onSurfaceAdded();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CameraManage.get().open();
        CameraManage.get().startPreview(mSurfaceView, this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraManage.get().close();
    }


    protected void resetPreviewCallback() {
        CameraManage.get().resetPreviewCallback(this);
    }
}
