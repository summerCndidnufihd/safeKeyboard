package com.gs.keyboard;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import java.lang.reflect.Method;

/**
 * SecurityEditText
 *
 * @author yidong (onlyloveyd@gmaill.com)
 * @date 2018/6/15 08:29
 */
@SuppressLint("AppCompatCustomView")
public class SecurityEditText extends EditText {
    private KeyboardDialog dialog;
    private KeyboardAttribute keyboardAttribute;
    private int mBorderSize =4;
    private Paint mPaint;

    private float mOffsetY;
    private boolean mShowCursor;
    private TextPaint mTextPaint;

    public SecurityEditText(Context context) {
        this(context, null);
        initView();
    }

    public SecurityEditText(Context context, @Nullable AttributeSet attrs) {
//        this(context, attrs, R.attr.editTextStyle);
        super(context, attrs);
        initView();
    }

    public SecurityEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        //初始化鍵盤樣式
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SecurityEditText);
        ColorStateList chooserSelectedColor = a.getColorStateList(R.styleable.SecurityEditText_chooserSelectedColor);
        ColorStateList chooserUnselectedColor = a.getColorStateList(R.styleable.SecurityEditText_chooserUnselectedColor);
        Drawable chooserBackground = a.getDrawable(R.styleable.SecurityEditText_chooserBackground);
        Drawable keyboardBackground = a.getDrawable(R.styleable.SecurityEditText_keyboardBackground);
        boolean isKeyPreview = a.getBoolean(R.styleable.SecurityEditText_keyPreview, true);
        a.recycle();
        keyboardAttribute = new KeyboardAttribute(chooserSelectedColor, chooserUnselectedColor, chooserBackground, keyboardBackground, isKeyPreview);
        initialize();
    }

    private void initialize() {
        initView();
        setClickable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setShowSoftInputOnFocus(false);
        } else {
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(this, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void initView() {
        setCursorVisible(false);
        setLongClickable(false);
        setTextIsSelectable(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderSize);
        mPaint.setColor(0xFFF6F6f6);

        mTextPaint = getPaint();
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.pincode_text_size));
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mOffsetY = (fontMetrics.bottom + fontMetrics.top) / 2;
    }


    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            hideSystemKeyboard();
            showSoftInput();
        } else {
            hideSoftKeyboard();
        }
    }

    @Override
    public boolean performClick() {
        if (this.isFocused()) {
            hideSystemKeyboard();
            showSoftInput();
        }
        return false;
    }

    private void hideSystemKeyboard() {
        InputMethodManager manager = (InputMethodManager) this.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    private void showSoftInput() {
        if (dialog == null) {
            dialog = KeyboardDialog.show(getContext(), this);
        } else {
            dialog.show();
        }
    }

    private void hideSoftKeyboard() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.isFocused()) {
            hideSystemKeyboard();
            showSoftInput();
        }
        startCursorBlink();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.isFocused()) {
            hideSoftKeyboard();
        }
        stopCursorBlink();
    }
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 11) {
                mShowCursor = !mShowCursor;
                String text = getText().toString().toUpperCase();
                if (text.length() < 6) {
                    invalidate();
                }
                sendEmptyMessageDelayed(11, 800);
            }
        }
    };
    private void startCursorBlink() {
        mHandler.removeMessages(11);
    }

    private void stopCursorBlink() {
        mHandler.removeMessages(11);
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus && hasFocus()) {
            this.post(() -> {
                hideSystemKeyboard();
                showSoftInput();
            });
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                return true;
            } else {
                return false;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
//    @Override
//    protected void onDraw(Canvas canvas) {
//        Log.d("LXP..","onDraw,.....");
//        super.onDraw(canvas);
//        int width = getResources().getDimensionPixelSize(R.dimen.pincode_single_width) - mBorderSize * 2;
//        int height = getResources().getDimensionPixelSize(R.dimen.pincode_height) - mBorderSize * 2;
//        int padding = getResources().getDimensionPixelSize(R.dimen.pincode_padding);
//        int left = mBorderSize;
//        int top = mBorderSize;
//        String text = getText().toString().toUpperCase();
//        int textLength = text.length();
//        if (!isRTL(getContext())) {
//            for (int i = 0; i < 6; i++) {
//                canvas.drawRect(left, top, left + width, top + height, mPaint);
//                if (textLength > i) {
//                    char ch = text.charAt(i);
//                    canvas.drawText(String.valueOf(ch), left + width / 2f, getHeight() / 2f - mOffsetY, mTextPaint);
//                } else if (mShowCursor && textLength == i) {
//                    mPaint.setColor(0xAA990000);
//                    canvas.drawLine(left + width / 2f, top + height / 4f, left + width / 2f, top + height * 3 / 4f, mPaint);
//                    mPaint.setColor(0xFFD2D2D2);
//                }
//                left += width + padding + mBorderSize * 2;
//            }
//        } else {
//            left += (width + padding + mBorderSize * 2) * 5;
//            for (int i = 0; i < 6; i++) {
//                canvas.drawRect(left, top, left + width, top + height, mPaint);
//                if (textLength > i) {
//                    char ch = text.charAt(i);
//                    canvas.drawText(String.valueOf(ch), left + width / 2f, getHeight() / 2f - mOffsetY, mTextPaint);
//                } else if (mShowCursor && textLength == i) {
//                    mPaint.setColor(0xFF990000);
//                    canvas.drawLine(left + width / 2f, top + height / 4f, left + width / 2f, top + height * 3 / 4f, mPaint);
//                    mPaint.setColor(0xFFD2D2D2);
//                }
//                left -= width + padding + mBorderSize * 2;
//            }
//        }
//    }

    public KeyboardAttribute getKeyboardAttribute() {
        return keyboardAttribute;
    }
}
