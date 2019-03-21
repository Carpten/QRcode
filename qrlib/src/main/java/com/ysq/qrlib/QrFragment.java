package com.ysq.qrlib;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.ysq.cpp.DataHandler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrFragment extends CameraFragment {

    private ScanView mScanView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private MultiFormatReader mQrReader;


    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private boolean mBinarizerReverse;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            new AlertDialog.Builder(getContext())
                    .setMessage((String) msg.obj)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mExecutorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    resetPreviewCallback();
                                }
                            });
                        }
                    })
                    .show();
            return false;
        }
    });

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        initDecode();
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onSurfaceAdded() {
        super.onSurfaceAdded();
        mScanView = new ScanView(getContext(), null);
        getContainer().addView(mScanView);
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    byte[] rotatedData = new byte[data.length];
//                    for (int y = 0; y < mPreviewSize.height; y++) {
//                        for (int x = 0; x < mPreviewSize.width; x++) {
//                            rotatedData[x * mPreviewSize.height + mPreviewSize.height - y - 1]
//                                    = data[x + y * mPreviewSize.width];
//                        }
//                    }

                    byte[] rotatedData = new DataHandler().arrayFromJNI(
                            data, mPreviewSize.width, mPreviewSize.height);
                    int pW = mPreviewSize.height;
                    int pH = mPreviewSize.width;

                    Rect rect = mScanView.getScanBoxAreaRect();
                    int w = pW * rect.width() / getSurfaceWidth();
                    int h = pH * rect.height() / getSurfaceHeigth();
                    int l = (pW - w) / 2;
                    int t = pH * rect.top / getSurfaceHeigth();


                    PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                            rotatedData, pW, pH, l, t, w, h);
                    BinaryBitmap bitmap;
                    if (mBinarizerReverse) {
                        bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    } else {
                        bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                    }
                    mBinarizerReverse = !mBinarizerReverse;
                    Result result = mQrReader.decodeWithState(bitmap);
                    if (!TextUtils.isEmpty(result.getText())) {
                        Message obtain = Message.obtain();
                        obtain.obj = result.getText();
                        mHandler.sendMessage(obtain);
                    } else {
                        resetPreviewCallback();
                    }
                } catch (Exception e) {
                    resetPreviewCallback();
                } finally {
                    mQrReader.reset();
                }

            }
        });
    }


    private void initDecode() {
        mQrReader = new MultiFormatReader();

        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>(2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        if (decodeFormats.isEmpty()) {
            decodeFormats = new Vector<>();
            Vector<BarcodeFormat> ONE_D_FORMATS = new Vector<>(1);
            ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
            Vector<BarcodeFormat> QR_CODE_FORMATS = new Vector<>(1);
            QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
            decodeFormats.addAll(ONE_D_FORMATS);
            decodeFormats.addAll(QR_CODE_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        mQrReader.setHints(hints);
    }
}
