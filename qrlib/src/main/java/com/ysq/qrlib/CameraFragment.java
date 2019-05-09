package com.ysq.qrlib;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class CameraFragment extends Fragment implements Camera.PreviewCallback
        , Camera.AutoFocusCallback, SurfaceHolder.Callback {

    protected Camera.Size mPreviewSize;

    private SurfaceView mSurfaceView;

    private FrameLayout mContainer;

    private Camera mCamera;

    private static ExecutorService mSingleExecutor = Executors.newSingleThreadExecutor();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            } else if (msg.what == 2) {
                addSurfaceView();
            }
            return false;
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContainer = (FrameLayout) inflater.inflate(R.layout.fragment_camera, container, false);
        return mContainer;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 88);
        } else {
            openCamera();
        }
    }

    public ViewGroup getContainer() {
        return mContainer;
    }


    public void onSurfaceAdded() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 88 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            new AlertDialog.Builder(getContext())
                    .setCancelable(false)
                    .setTitle(R.string.ysq_permissions_carmea_deny_title)
                    .setMessage(R.string.ysq_permissions_carmea_deny_message)
                    .setPositiveButton(R.string.ysq_permissions_carmea_deny_button, null)
                    .show();
        }
    }

    /**
     * 尝试打开相机
     */
    private void openCamera() {
        mSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera = Camera.open(0);
                    setPrevieSize();
                } catch (Exception e) {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = getString(R.string.ysq_camera_open_failed);
                    mHandler.sendMessage(message);
                }
            }
        });

    }

    /**
     * 相机关闭，并释放
     */
    private void stop() {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();
            mCamera = null;
        }

    }


    /**
     * 获取最佳相机预览分辨率
     */
    private void setPrevieSize() {
        List<Camera.Size> supportedPreviewSizes =
                mCamera.getParameters().getSupportedPreviewSizes();
        mPreviewSize = getPreviewSize(supportedPreviewSizes);
        Message message = Message.obtain();
        message.what = 2;
        mHandler.sendMessage(message);
    }

    /**
     * 添加surfaceView
     */
    private void addSurfaceView() {
        int width = mContainer.getWidth();
        int height = mContainer.getHeight();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);
        if (width * mPreviewSize.width < height * mPreviewSize.height) {
            layoutParams.width = mPreviewSize.height * height / mPreviewSize.width;
            layoutParams.height = height;
        } else {
            layoutParams.width = width;
            layoutParams.height = mPreviewSize.width * width / mPreviewSize.height;
        }

        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.setLayoutParams(layoutParams);
        mSurfaceView.getHolder().addCallback(this);
        mContainer.addView(mSurfaceView);
        onSurfaceAdded();
    }


    protected int getSurfaceWidth() {
        return mContainer.getWidth();
    }

    protected int getSurfaceHeigth() {
        return mContainer.getHeight();
    }


    /**
     * 配置相机
     */
    public void configCamera() throws IOException {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.setOneShotPreviewCallback(this);
            mCamera.startPreview();
            autoFocus();
        }
    }

    /**
     * 自动对焦
     */
    private void autoFocus() {
        if (mCamera != null) {
            List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mCamera.cancelAutoFocus();
                Camera.Parameters p = mCamera.getParameters();
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(p);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                mCamera.autoFocus(this);
            }
        }
    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(this);
        }
    }


    private Camera.Size getPreviewSize(List<Camera.Size> sizeList) {
        float minRatio = Float.MAX_VALUE;
        int targetSize = 800 * 600;
        int position = 0;
        for (Camera.Size size : sizeList) {
            float ratio;
            if (size.width * size.height > targetSize) {
                ratio = (float) size.width * size.height / targetSize;
            } else {
                ratio = (float) targetSize / size.width / size.height;
            }
            if (ratio < minRatio) {
                minRatio = ratio;
                position = sizeList.indexOf(size);
            }
        }
        return sizeList.get(position);
    }


    protected void resetPreviewCallback() {
        mSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null)
                    mCamera.setOneShotPreviewCallback(CameraFragment.this);
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    configCamera();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                stop();
            }
        });
    }

    public void openFlashlight() {
        mSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) {
                    doSetTorch(mCamera, true);
                }
            }
        });
    }

    public void closeFlashlight() {
        mSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) {
                    doSetTorch(mCamera, false);
                }
            }
        });

    }

    /**
     * 打开关闭闪光灯
     *
     * @param camera     相机对象
     * @param newSetting 相机开关
     */
    private void doSetTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        String flashMode;
        /* 是否支持闪光灯 */
        if (newSetting) {
            flashMode = findSettableValue(parameters.getSupportedFlashModes()
                    , Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON);
        } else {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_OFF);
        }
        if (flashMode != null) {
            parameters.setFlashMode(flashMode);
        }
        camera.setParameters(parameters);
    }

    private String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        return result;
    }
}
