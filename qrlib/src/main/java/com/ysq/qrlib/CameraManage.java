package com.ysq.qrlib;

import android.hardware.Camera;
import android.view.SurfaceView;

import java.util.Collection;
import java.util.List;

public class CameraManage implements Camera.AutoFocusCallback {

    private static int sWidth;
    private static int sHeight;

    private static Camera mCamera;

    private static CameraManage sInstance = new CameraManage();

    protected int[] mPreviewSize;

    private CameraManage() {
        mPreviewSize = initPreviewSize();
    }

    /**
     * 获取RetrofitClient对象
     *
     * @return RetrofitClient对象
     */
    public static CameraManage get() {
        return sInstance;
    }


    public synchronized void open() {
        if (mCamera == null) {
            mCamera = Camera.open(0);

        }
    }

    public synchronized void close() {
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
    private int[] initPreviewSize() {
        if (sWidth == 0) {
            open();
            List<Camera.Size> supportedPreviewSizes =
                    mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Size previewSize = getPreviewSize(supportedPreviewSizes);
            sWidth = previewSize.width;
            sHeight = previewSize.height;
            close();
            return new int[]{previewSize.width, previewSize.height};
        } else {
            return new int[]{sWidth, sHeight};
        }
    }

    public int[] getPreviewSize() {
        return mPreviewSize;
    }


    private Camera.Size getPreviewSize(List<Camera.Size> sizeList) {
        float minRatio = Float.MAX_VALUE;
        int targetSize = 1280 * 720;
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

    /**
     * 配置相机
     */
    public synchronized void startPreview(SurfaceView surfaceView, Camera.PreviewCallback previewCallback) {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(mPreviewSize[0], mPreviewSize[1]);
                parameters.setZoom(1);
                parameters.setExposureCompensation(-2);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(surfaceView.getHolder());
                // Important: Call startPreview() to start updating the preview
                // surface. Preview must be started before you can take a picture.
                mCamera.setOneShotPreviewCallback(previewCallback);
                mCamera.startPreview();
                autoFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void resetPreviewCallback(Camera.PreviewCallback previewCallback) {
        if (mCamera != null)
            mCamera.setOneShotPreviewCallback(previewCallback);
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
    public synchronized void onAutoFocus(boolean success, Camera camera) {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(this);
        }
    }


    public synchronized void openFlashlight() {
        if (mCamera != null) {
            doSetTorch(mCamera, true);
        }
    }

    public synchronized void closeFlashlight() {
        if (mCamera != null) {
            doSetTorch(mCamera, false);
        }

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
