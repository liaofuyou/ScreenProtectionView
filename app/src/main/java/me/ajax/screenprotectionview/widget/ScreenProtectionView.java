package me.ajax.screenprotectionview.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.ajax.screenprotectionview.R;

import static me.ajax.screenprotectionview.utils.GeometryUtils.polarX;
import static me.ajax.screenprotectionview.utils.GeometryUtils.polarY;


/**
 * Created by aj on 2018/4/2
 */

public class ScreenProtectionView extends ViewGroup {

    ViewDragHelper viewDragHelper;
    View wallpaperBgView;
    View notificationList;
    TextView timeText;

    public ScreenProtectionView(Context context) {
        super(context);
        init();
    }

    public ScreenProtectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScreenProtectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        viewDragHelper = ViewDragHelper.create(this, 1f, new DragCallback());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        wallpaperBgView = findViewById(R.id.wallpaper_bg_view);
        timeText = findViewById(R.id.time_text);
        notificationList = findViewById(R.id.notification_list);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChild(wallpaperBgView, widthMeasureSpec, heightMeasureSpec);
        measureChild(timeText, widthMeasureSpec, heightMeasureSpec);
        measureChild(notificationList, MeasureSpec.makeMeasureSpec(wSize - dp2Dx(16), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.AT_MOST));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //绑定拦截事件
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int hQuarter = height / 4;
        int hHalf = height / 2;
        int wHalf = width / 2;

        wallpaperBgView.layout(0, 0, r, b);

        viewLayoutByPolar(timeText, dp2Dx(130), 90);
        viewLayoutByPolar(notificationList, dp2Dx(240), 90);
    }


    void viewLayoutByPolar(View view, float p, float a) {
        int centerX = getMeasuredWidth() / 2 + (int) polarX(p, a);
        int centerY = (int) polarY(p, a);
        int wViewHalf = view.getMeasuredWidth() / 2;
        int hViewHalf = view.getMeasuredHeight() / 2;

        view.layout(centerX - wViewHalf, centerY - hViewHalf, centerX + wViewHalf, centerY + hViewHalf);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }


    class DragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == wallpaperBgView;// && !contentView.canScrollVertically(-1);
        }

        /**
         * clamp 固定住
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (top > 0) {
                top = 0;
            }
            return top;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            float fraction = Math.abs(top) / (float) getMeasuredHeight();

            float scale = 1 - fraction;
            if (scale < 0.5F) scale = 0;
            timeText.setScaleX(scale);
            timeText.setScaleY(scale);
            wallpaperBgView.setAlpha(1 - fraction);
            viewLayoutByPolar(notificationList, dp2Dx(240) * (1 - fraction), 90);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            int finalTop = 0;
            if (releasedChild.getY() < -getMeasuredHeight() / 2
                    || yvel < -1000) {
                finalTop = -getMeasuredHeight();
            }

            viewDragHelper.settleCapturedViewAt(0, finalTop);
            ViewCompat.postInvalidateOnAnimation(ScreenProtectionView.this);
        }
    }

    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(ScreenProtectionView.this);
        }
    }

    int dp2Dx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    void l(Object o) {
        Log.e("######", o.toString());
    }


}
