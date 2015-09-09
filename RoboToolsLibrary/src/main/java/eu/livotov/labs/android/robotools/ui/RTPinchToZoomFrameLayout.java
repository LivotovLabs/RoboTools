package eu.livotov.labs.android.robotools.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import eu.livotov.labs.android.robotools.R;

/**
 * Created by dlivotov on 03/09/2015.
 */
public class RTPinchToZoomFrameLayout extends FrameLayout
{
//    private View scaleHud;
//    private ScaleGestureDetector mScaleDetector;
//    private float newScaleFactorToApply = 0;
//    private float scaleHudTitleOriginalSizeSp = 19.0f;
//    private float scaleHudSubtitleOriginalSizeSp = 15.0f;
//    private boolean liveScaleEnabled = true;
//
    public RTPinchToZoomFrameLayout(Context context)
    {
        super(context);
        initUI();
    }

    public RTPinchToZoomFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initUI();
    }

    public RTPinchToZoomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initUI();
    }


    @TargetApi(21)
    public RTPinchToZoomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        initUI();
    }

    private void initUI()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.robotools_ui_pinchtozoomframelayout, this);
//        scaleHud = findViewById(R.id.scaleHud);
//        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }
//
//    public boolean onTouchEvent(MotionEvent event)
//    {
//        if (scaleHud.getVisibility() == VISIBLE && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL))
//        {
//            hideScaleHUD();
//        }
//
//        mScaleDetector.onTouchEvent(event);
//
//        if (scaleHud.getVisibility() == VISIBLE && event.getAction() == MotionEvent.ACTION_MOVE)
//        {
//            return true;
//        }
//
//        return super.onTouchEvent(event);
//    }
//
//    private void hideScaleHUD()
//    {
//        if (newScaleFactorToApply != 0.0f)
//        {
//            WMEventScaleFactorChanged event = new WMEventScaleFactorChanged((float) App.getSettings().getFontScaleFactor(), newScaleFactorToApply);
//            App.getSettings().setFontScaleFactor(newScaleFactorToApply);
//            App.publish(event);
//            newScaleFactorToApply = 0;
//        }
//
//        scaleHud.setVisibility(View.INVISIBLE);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event)
//    {
//        mScaleDetector.onTouchEvent(event);
//
//        if (event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 2)
//        {
//            return true;
//        }
//
//        return false;
//    }
//
//    public void setMainContent(View view)
//    {
//        addView(view, 0);
//    }
//
//    private void displayScaleHUD()
//    {
//        scaleHud.setVisibility(View.VISIBLE);
//        newScaleFactorToApply = (float) App.getSettings().getFontScaleFactor();
//        updateScaleHUD();
//    }
//
//    private void updateScaleHUD()
//    {
//        if (newScaleFactorToApply == 0)
//        {
//            newScaleFactorToApply = (float) App.getSettings().getFontScaleFactor();
//        }
//
//        scaleHudTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, scaleHudTitleOriginalSizeSp * newScaleFactorToApply);
//        scaleHudSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, scaleHudSubtitleOriginalSizeSp * newScaleFactorToApply);
//    }
//
//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
//    {
//
//        @Override
//        public boolean onScale(ScaleGestureDetector detector)
//        {
//            if (!liveScaleEnabled)
//            {
//                return true;
//            }
//
//            if (scaleHud.getVisibility() != VISIBLE)
//            {
//                displayScaleHUD();
//            }
//
//            if (newScaleFactorToApply == 0.0f)
//            {
//                newScaleFactorToApply = (float) App.getSettings().getFontScaleFactor();
//            }
//
//            newScaleFactorToApply *= detector.getScaleFactor();
//
//            if (newScaleFactorToApply < 1.0f)
//            {
//                newScaleFactorToApply = 1.0f;
//            }
//
//            if (newScaleFactorToApply > 2.5f)
//            {
//                newScaleFactorToApply = 2.5f;
//            }
//
//            updateScaleHUD();
//            return true;
//        }
//    }
}
