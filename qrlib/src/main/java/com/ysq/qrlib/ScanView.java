package com.ysq.qrlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class ScanView extends View {

    private static final int DEFAULT_MASK_COLOR = 0x80000000;

    private static final int DEFAULT_SCAN_AREA_WIDTH_DP = 240;

    private static final int DEFAULT_CORNER_WIDTH_DP = 2;

    private static final int DEFAULT_CORNER_LENGTH_DP = 16;

    private static final int DEFAULT_SCAN_LINE_WIDTH_DP = 1;

    private static final int DEFAULT_SCAN_LINE_COLOR = 0xfff9642a;

    private static final int DEFAULT_ANIM_DURATION = 2000;

    private static final int DEFAULT_TIP_TEXT_SIZE = 14;

    private static final int DEFAULT_TIP_TEXT_COLOR = Color.WHITE;

    private static final int DEFAULT_TIP_TEXT_MARGIN_TOP_DP = 16;

    private float mMoveStepDistance;

    private Rect mFramingRect;
    private float mScanLineTop;
    private Paint mPaint;
    private TextPaint mTipPaint;

    private int mMaskColor;
    private int mCornerColor;
    private int mCornerLength;
    private int mCornerWidth;
    private int mScanAreaWidth;
    private int mTopOffset;
    private int mScanLineWidth;
    private int mScanLineColor;
    private int mScanLineMargin;
    private boolean mIsShowDefaultScanLineDrawable;
    private Bitmap mCustomScanLineBitmap;
    private int mAnimTime;
    private String mTipText;
    private int mTipTextSize;
    private int mTipTextColor;
    private int mTipTextMargin;
    private boolean mIsShowTipTextAsSingleLine;
    private float mHalfCornerWidth;
    private StaticLayout mTipTextSl;
    private boolean mShouldStop;


    public ScanView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mMaskColor = DEFAULT_MASK_COLOR;
        mCornerColor = Color.WHITE;
        mCornerLength = dp2px(context, DEFAULT_CORNER_LENGTH_DP);
        mCornerWidth = dp2px(context, DEFAULT_CORNER_WIDTH_DP);
        mScanLineWidth = dp2px(context, DEFAULT_SCAN_LINE_WIDTH_DP);
        mScanLineColor = DEFAULT_SCAN_LINE_COLOR;
        mScanAreaWidth = dp2px(context, DEFAULT_SCAN_AREA_WIDTH_DP);
        mScanLineMargin = dp2px(context, 8);
        mIsShowDefaultScanLineDrawable = false;
        mAnimTime = DEFAULT_ANIM_DURATION;
        mTipTextSize = sp2px(context, DEFAULT_TIP_TEXT_SIZE);
        mTipTextColor = DEFAULT_TIP_TEXT_COLOR;
        mTipTextMargin = dp2px(context, DEFAULT_TIP_TEXT_MARGIN_TOP_DP);
        mTipPaint = new TextPaint();
        mTipPaint.setAntiAlias(true);
        initCustomAttrs(context, attrs);
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanView);
            final int count = typedArray.getIndexCount();
            for (int i = 0; i < count; i++) {
                initCustomAttr(typedArray.getIndex(i), typedArray);
            }
            typedArray.recycle();
        }
        afterInitCustomAttrs();
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.ScanView_qrcv_topOffset) {
            mTopOffset = typedArray.getDimensionPixelSize(attr, mTopOffset);
        } else if (attr == R.styleable.ScanView_qrcv_cornerSize) {
            mCornerWidth = typedArray.getDimensionPixelSize(attr, mCornerWidth);
        } else if (attr == R.styleable.ScanView_qrcv_cornerLength) {
            mCornerLength = typedArray.getDimensionPixelSize(attr, mCornerLength);
        } else if (attr == R.styleable.ScanView_qrcv_scanLineSize) {
            mScanLineWidth = typedArray.getDimensionPixelSize(attr, mScanLineWidth);
        } else if (attr == R.styleable.ScanView_qrcv_scanWidth) {
            mScanAreaWidth = typedArray.getDimensionPixelSize(attr, mScanAreaWidth);
        } else if (attr == R.styleable.ScanView_qrcv_maskColor) {
            mMaskColor = typedArray.getColor(attr, mMaskColor);
        } else if (attr == R.styleable.ScanView_qrcv_cornerColor) {
            mCornerColor = typedArray.getColor(attr, mCornerColor);
        } else if (attr == R.styleable.ScanView_qrcv_scanLineColor) {
            mScanLineColor = typedArray.getColor(attr, mScanLineColor);
        } else if (attr == R.styleable.ScanView_qrcv_scanLineMargin) {
            mScanLineMargin = typedArray.getDimensionPixelSize(attr, mScanLineMargin);
        } else if (attr == R.styleable.ScanView_qrcv_isShowDefaultScanLineDrawable) {
            mIsShowDefaultScanLineDrawable = typedArray.getBoolean(attr, mIsShowDefaultScanLineDrawable);
        } else if (attr == R.styleable.ScanView_qrcv_customScanLineDrawable) {
            Drawable customDrawable = typedArray.getDrawable(attr);
            if (customDrawable != null)
                mCustomScanLineBitmap = ((BitmapDrawable) customDrawable).getBitmap();
        } else if (attr == R.styleable.ScanView_qrcv_animTime) {
            mAnimTime = typedArray.getInteger(attr, mAnimTime);
        } else if (attr == R.styleable.ScanView_qrcv_qrCodeTipText) {
            mTipText = typedArray.getString(attr);
        } else if (attr == R.styleable.ScanView_qrcv_tipTextSize) {
            mTipTextSize = typedArray.getDimensionPixelSize(attr, mTipTextSize);
        } else if (attr == R.styleable.ScanView_qrcv_tipTextColor) {
            mTipTextColor = typedArray.getColor(attr, mTipTextColor);
        } else if (attr == R.styleable.ScanView_qrcv_tipTextMargin) {
            mTipTextMargin = typedArray.getDimensionPixelSize(attr, mTipTextMargin);
        } else if (attr == R.styleable.ScanView_qrcv_isShowTipTextAsSingleLine) {
            mIsShowTipTextAsSingleLine = typedArray.getBoolean(attr, mIsShowTipTextAsSingleLine);
        }
    }

    private void afterInitCustomAttrs() {
        if (mIsShowDefaultScanLineDrawable) {
            mCustomScanLineBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.qrcode_default_scan_line);
            mCustomScanLineBitmap = makeTintBitmap(mCustomScanLineBitmap, mScanLineColor);
        }
        mHalfCornerWidth = mCornerWidth / 2f;
        mTipPaint.setTextSize(mTipTextSize);
        mTipPaint.setColor(mTipTextColor);
        mMoveStepDistance = (float) mScanAreaWidth / mAnimTime * 1000 / 60;
        if (!TextUtils.isEmpty(mTipText)) {
            if (mIsShowTipTextAsSingleLine) {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, getScreenResolution(getContext()).x
                        , Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            } else {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, mScanAreaWidth
                        , Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calFramingRect();
    }

    private void calFramingRect() {
        int leftOffset = (getWidth() - mScanAreaWidth) / 2;
        mFramingRect = new Rect(leftOffset, (getHeight() - mScanAreaWidth) / 2 + mTopOffset, leftOffset
                + mScanAreaWidth, (getHeight() - mScanAreaWidth) / 2 + mTopOffset + mScanAreaWidth);
        mScanLineTop = mFramingRect.top + mHalfCornerWidth;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFramingRect == null) {
            return;
        }
        // 画遮罩层
        drawMask(canvas);
        // 画四个直角的线
        drawCornerLine(canvas);
        // 画提示文本
        drawTipText(canvas);
        // 画扫描线
        drawScanLine(canvas);
        // 移动扫描线的位置
        if (!mShouldStop)
            moveScanLine();
    }


    public void stop() {
        mShouldStop = true;
    }


    /**
     * 画遮罩层
     */
    private void drawMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        if (mMaskColor != Color.TRANSPARENT) {
            mPaint.setColor(mMaskColor);
            canvas.drawRect(0, 0, width, mFramingRect.top - 1, mPaint);
            canvas.drawRect(0, mFramingRect.top - 1, mFramingRect.left - 1, mFramingRect.bottom, mPaint);
            canvas.drawRect(mFramingRect.right, mFramingRect.top - 1, width, mFramingRect.bottom, mPaint);
            canvas.drawRect(0, mFramingRect.bottom, width, height, mPaint);
        }
    }

    /**
     * 画四个直角的线
     */
    private void drawCornerLine(Canvas canvas) {
        if (mHalfCornerWidth > 0) {
            mPaint.setColor(mCornerColor);
            mPaint.setStrokeWidth(mCornerWidth);
            canvas.drawLine(mFramingRect.left - mHalfCornerWidth, mFramingRect.top, mFramingRect.left
                    - mHalfCornerWidth + mCornerLength, mFramingRect.top, mPaint);
            canvas.drawLine(mFramingRect.left, mFramingRect.top - mHalfCornerWidth, mFramingRect.left
                    , mFramingRect.top - mHalfCornerWidth + mCornerLength, mPaint);
            canvas.drawLine(mFramingRect.right + mHalfCornerWidth, mFramingRect.top, mFramingRect.right
                    + mHalfCornerWidth - mCornerLength, mFramingRect.top, mPaint);
            canvas.drawLine(mFramingRect.right, mFramingRect.top - mHalfCornerWidth, mFramingRect.right
                    , mFramingRect.top - mHalfCornerWidth + mCornerLength, mPaint);
            canvas.drawLine(mFramingRect.left - mHalfCornerWidth, mFramingRect.bottom, mFramingRect.left
                    - mHalfCornerWidth + mCornerLength, mFramingRect.bottom, mPaint);
            canvas.drawLine(mFramingRect.left, mFramingRect.bottom + mHalfCornerWidth, mFramingRect.left
                    , mFramingRect.bottom + mHalfCornerWidth - mCornerLength, mPaint);
            canvas.drawLine(mFramingRect.right + mHalfCornerWidth, mFramingRect.bottom, mFramingRect.right
                    + mHalfCornerWidth - mCornerLength, mFramingRect.bottom, mPaint);
            canvas.drawLine(mFramingRect.right, mFramingRect.bottom + mHalfCornerWidth, mFramingRect.right
                    , mFramingRect.bottom + mHalfCornerWidth - mCornerLength, mPaint);
        }
    }

    /**
     * 画提示文本
     */
    private void drawTipText(Canvas canvas) {
        if (TextUtils.isEmpty(mTipText) || mTipTextSl == null) {
            return;
        }
        canvas.save();
        if (mIsShowTipTextAsSingleLine) {
            canvas.translate(0, mFramingRect.bottom + mTipTextMargin);
        } else {
            canvas.translate(mFramingRect.left, mFramingRect.bottom + mTipTextMargin);
        }
        mTipTextSl.draw(canvas);
        canvas.restore();

    }

    /**
     * 画扫描线
     */
    private void drawScanLine(Canvas canvas) {
        if (mCustomScanLineBitmap != null) {
            RectF lineRect = new RectF(mFramingRect.left + mScanLineMargin, mScanLineTop, mFramingRect.right
                    - mScanLineMargin, mScanLineTop + mCustomScanLineBitmap.getHeight());
            canvas.drawBitmap(mCustomScanLineBitmap, null, lineRect, mPaint);
        } else {
            mPaint.setColor(mScanLineColor);
            canvas.drawRect(mFramingRect.left + mScanLineMargin, mScanLineTop, mFramingRect.right - mScanLineMargin
                    , mScanLineTop + mScanLineWidth, mPaint);
        }
    }

    /**
     * 移动扫描线的位置
     */
    private void moveScanLine() {
        // 处理非网格扫描图片的情况
        mScanLineTop += mMoveStepDistance;
        int scanLineSize = mScanLineWidth;
        if (mCustomScanLineBitmap != null) {
            scanLineSize = mCustomScanLineBitmap.getHeight();
        }
        if (mScanLineTop + scanLineSize > mFramingRect.bottom - mHalfCornerWidth) {
            mScanLineTop = mFramingRect.top + mHalfCornerWidth + 0.5f;
        }
        invalidate(mFramingRect.left, mFramingRect.top, mFramingRect.right, mFramingRect.bottom);
    }


    private Point getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenResolution = new Point();
        display.getSize(screenResolution);
        return screenResolution;
    }

    private static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue
                , context.getResources().getDisplayMetrics());
    }

    private static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue
                , context.getResources().getDisplayMetrics());
    }

    private Bitmap makeTintBitmap(Bitmap inputBitmap, int tintColor) {
        if (inputBitmap == null) {
            return null;
        }
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getWidth(), inputBitmap.getHeight()
                , inputBitmap.getConfig());
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inputBitmap, 0, 0, paint);
        return outputBitmap;
    }

    public Rect getScanBoxAreaRect() {
        return mFramingRect;
//        Log.i("test", "ll:" + mFramingRect.left + ",rr:" + mFramingRect.right + ",tt:" + mFramingRect.top + ",bb:" + mFramingRect.bottom);
//        Rect rect = new Rect();
//        float ratio0 = 1.0f * previewWidth / getMeasuredHeight();
//        rect.left = (int) (mFramingRect.top * ratio0);
//        rect.right = (int) (mFramingRect.bottom * ratio0);
//        float ratio1 = 1.0f * previewHeight / getMeasuredWidth();
//        rect.top = (int) (mFramingRect.left * ratio1);
//        rect.bottom = (int) (mFramingRect.right * ratio1);
//        return rect;
    }
}