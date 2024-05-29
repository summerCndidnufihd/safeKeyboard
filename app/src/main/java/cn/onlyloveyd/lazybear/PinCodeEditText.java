package cn.onlyloveyd.lazybear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.text.TextUtilsCompat;


import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by summer Rose on 2024/5/8
 * 有樣式的EditText
 */

@SuppressLint("AppCompatCustomView")
public class PinCodeEditText extends EditText {

    private static final int MSG_BLINK = 1001;
    private static final int BLINK_INTERVAL = 800;

    private Paint mPaint;
    private TextPaint mTextPaint;
    private int mBorderSize = 6;
    private float mOffsetY;
    private boolean mShowCursor;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_BLINK) {
                mShowCursor = !mShowCursor;
                String text = getText().toString().toUpperCase();
                if (text.length() < 8) {
                    invalidate();
                }
                sendEmptyMessageDelayed(MSG_BLINK, BLINK_INTERVAL);
            }
        }
    };
    private KeyboardAttribute keyboardAttribute;
    private KeyboardDialog dialog;
    private float centerY;
    public static int textLength;

    public PinCodeEditText(Context context) {
        super(context);
        initView();
    }

    public PinCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PinCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        //初始化鍵盤樣式
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PinCodeEditText);
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
        mPaint.setColor(getResources().getColor(R.color.main_gray));

        mTextPaint = getPaint();
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(30);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mOffsetY = (fontMetrics.bottom + fontMetrics.top) / 2;
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        Log.d("LXP...","   onWindowFocusChanged...  hasWindowFocus && hasFocus():"+hasWindowFocus+"   "+hasFocus());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("LXP...","   onKeyUp...");
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

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        Log.d("LXP...","   onFocusChanged...    focused："+focused );
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
        Log.d("LXP...","   performClick...");
        if (this.isFocused()) {
            hideSystemKeyboard();
            showSoftInput();
        }
        return false;
    }
    @Override
    public boolean onCheckIsTextEditor() {
        return false; // 这会告诉系统该视图不是一个文本编辑器，从而阻止系統键盘弹出
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
        Log.d("LXP...","   onAttachedToWindow...");
        super.onAttachedToWindow();
        if (this.isFocused()) {
            hideSystemKeyboard();
            showSoftInput();
        }
        startCursorBlink();
    }

    @Override
    public void onDetachedFromWindow() {
        Log.d("LXP...","   onDetachedFromWindow...");
        super.onDetachedFromWindow();
        if (this.isFocused()) {
            hideSoftKeyboard();
        }
        stopCursorBlink();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getResources().getDimensionPixelSize(R.dimen.dp_45) - mBorderSize * 2;
        int height = getResources().getDimensionPixelSize(R.dimen.dp_60) - mBorderSize * 2;
        int padding = getResources().getDimensionPixelSize(R.dimen.dp_6);
        int left = mBorderSize;
        int top = mBorderSize;
        String text = getText().toString().toUpperCase();
        textLength = text.length();
        Log.d("LXP..","text:"+textLength+ "    text:"+text);
//        if (!isRTL(getContext())) {
        for (int i = 0; i < 8; i++) {
            @SuppressLint("DrawAllocation") RectF rectF = new RectF(left, top, left + width, top + height);
            @SuppressLint("DrawAllocation") Path path = new Path();
            float rx = dpToPx(3);
            float ry = rx;
            path.addRoundRect(rectF, rx, ry, Path.Direction.CW);
            if(i <= textLength){
                mPaint.setColor(getResources().getColor(R.color.main_green));
            }else{
                mPaint.setColor(getResources().getColor(R.color.main_gray));
            }
            // 内部填充 Paint
            @SuppressLint("DrawAllocation") Paint mFillPaint = new Paint();
            mFillPaint.setStyle(Paint.Style.FILL); // 填充样式
            mFillPaint.setColor(Color.WHITE); // 内部颜色为白色
            canvas.drawPath(path, mFillPaint);
            canvas.drawPath(path, mPaint);
            if (textLength > i) {
                char ch = text.charAt(i);
                //这里调字体在框里的位置
                canvas.drawText(String.valueOf(ch), left + width / 2f, getHeight()/2f - mOffsetY+height/9f, mTextPaint);
            } else if (mShowCursor && textLength == i) {
                canvas.drawLine(left + width / 2f, top + height / 4f, left + width / 2f, top + height * 3 / 4f, mPaint);
            }
            left += width + padding + mBorderSize * 2;
        }
//        } else {
//            left += (width + padding + mBorderSize * 2) * 7;
//            for (int i = 0; i < 8; i++) {
//                @SuppressLint("DrawAllocation") RectF rectF = new RectF(left, top, left + width, top + height);
//                @SuppressLint("DrawAllocation") Path path = new Path();
//                float rx = dpToPx(3);
//                float ry = rx;
//                path.addRoundRect(rectF, rx, ry, Path.Direction.CW);
//                canvas.drawPath(path, mPaint);
//                mPaint.setColor(getResources().getColor(R.color.main_green));
//                if (textLength > i) {
//                    mPaint.setColor(getResources().getColor(R.color.main_gray));
//                    char ch = text.charAt(i);
//                    canvas.drawText(String.valueOf(ch), left + width / 2f, getHeight() / 2f*2, mTextPaint);
//                } else if (mShowCursor && textLength == i) {
//                    canvas.drawLine(left + width / 2f, top + height / 4f, left + width / 2f, top + height * 3 / 4f, mPaint);
//                }
//                left -= width + padding + mBorderSize * 2;
//            }
//        }
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }


    private void startCursorBlink() {
        mHandler.removeMessages(MSG_BLINK);
        mHandler.sendEmptyMessageDelayed(MSG_BLINK, BLINK_INTERVAL);
    }

    private void stopCursorBlink() {
        mHandler.removeMessages(MSG_BLINK);
    }

    private static boolean mSupportRTL;
    public static boolean isRTL(@NonNull Context context) {
        if (!mSupportRTL) {
            return false;
        }
        Configuration configuration = context.getResources().getConfiguration();
        Locale locale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    locale = configuration.getLocales().get(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (locale == null) {
                locale = configuration.locale;
            }
            return TextUtilsCompat.getLayoutDirectionFromLocale(locale) == LayoutDirection.RTL;
        }
        return false;
    }

    public KeyboardAttribute getKeyboardAttribute() {
        return keyboardAttribute;
    }
}
