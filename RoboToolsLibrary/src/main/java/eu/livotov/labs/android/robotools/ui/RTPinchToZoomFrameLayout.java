package eu.livotov.labs.android.robotools.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import eu.livotov.labs.android.robotools.R;

/**
 * Created by dlivotov on 03/09/2015.
 */
public class RTPinchToZoomFrameLayout extends FrameLayout
{
    private View scaleHud;
    private ScaleGestureDetector mScaleDetector;
    private float scaleFactor = 1.0f;
    private float newScaleFactorToApply = 0;
    private float minScaleFactor = 0.1f;
    private float maxScaleFactor = 2.0f;
    private boolean persistentScaleFactor = false;

    private ScaleHudEventListener scaleHudEventListener;
    private ScaleEventListener scaleEventListener;

    public RTPinchToZoomFrameLayout(Context context)
    {
        super(context);
    }

    public RTPinchToZoomFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RTPinchToZoomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public RTPinchToZoomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ScaleHudEventListener getScaleHudEventListener()
    {
        return scaleHudEventListener;
    }

    public void setScaleHudEventListener(ScaleHudEventListener scaleHudEventListener)
    {
        this.scaleHudEventListener = scaleHudEventListener;
    }

    public ScaleEventListener getScaleEventListener()
    {
        return scaleEventListener;
    }

    public void setScaleEventListener(ScaleEventListener scaleEventListener)
    {
        this.scaleEventListener = scaleEventListener;
    }

    public boolean isPersistentScaleFactor()
    {
        return persistentScaleFactor;
    }

    public void setPersistentScaleFactor(boolean persistentScaleFactor)
    {
        this.persistentScaleFactor = persistentScaleFactor;
    }

    public float getScaleFactor()
    {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor)
    {
        if (scaleFactor>=minScaleFactor && scaleFactor<=maxScaleFactor)
        {
            this.scaleFactor = scaleFactor;
        } else
        {
            throw new IllegalArgumentException(String.format("Scale factor %s is out of its min - max bounds: %s - %s", scaleFactor, minScaleFactor, maxScaleFactor));
        }
    }

    public float getMinScaleFactor()
    {
        return minScaleFactor;
    }

    public void setMinScaleFactor(float minScaleFactor)
    {
        if (minScaleFactor>0 && minScaleFactor<maxScaleFactor)
        {
            this.minScaleFactor = minScaleFactor;
        } else
        {
            throw new IllegalArgumentException(String.format("Min scale factor must be greater than zero and less then max scale factor %s. You specified %s", maxScaleFactor, minScaleFactor));
        }
    }

    public float getMaxScaleFactor()
    {
        return maxScaleFactor;
    }

    public void setMaxScaleFactor(float maxScaleFactor)
    {
        if (maxScaleFactor>minScaleFactor)
        {
            this.maxScaleFactor = maxScaleFactor;
        } else
        {
            throw new IllegalArgumentException(String.format("Max scale factor must be greater than min scale factor %s. You specified %s", minScaleFactor, maxScaleFactor));
        }
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        if (getChildCount()!=2)
        {
            throw new IllegalArgumentException("RTPinchToZoomFrameLayout must contain excatly 2 children: first one is the main content, second one is the hud view. Your child count is " + getChildCount());
        }

        scaleHud = getChildAt(1);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        if (scaleHud.getVisibility() == VISIBLE && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL))
        {
            hideScaleHUD();
        }

        mScaleDetector.onTouchEvent(event);

        if (scaleHud.getVisibility() == VISIBLE && event.getAction() == MotionEvent.ACTION_MOVE)
        {
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        mScaleDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 2)
        {
            return true;
        }

        return false;
    }

    private void displayScaleHUD()
    {
        newScaleFactorToApply = scaleFactor;
        scaleHud.setVisibility(View.VISIBLE);

        if (scaleHudEventListener != null)
        {
            scaleHudEventListener.onHudShown(this);
        }
    }

    private void updateScaleHUD(final float previousScaleFactorToApply)
    {
        if (newScaleFactorToApply == 0)
        {
            newScaleFactorToApply = scaleFactor;
        }

        if (scaleHudEventListener != null)
        {
            scaleHudEventListener.onScaleChanging(this, scaleFactor, previousScaleFactorToApply, newScaleFactorToApply);
        }
    }

    private void hideScaleHUD()
    {
        scaleHud.setVisibility(View.INVISIBLE);
        float oldScaleFactor = scaleFactor;

        if (newScaleFactorToApply != 0.0f)
        {
            scaleFactor = newScaleFactorToApply;
            newScaleFactorToApply = 0;
        }

        if (scaleHudEventListener != null)
        {
            scaleHudEventListener.onHudHidden(this);
        }

        if (oldScaleFactor!=scaleFactor && scaleEventListener!=null)
        {
            scaleEventListener.onScaleChanged(oldScaleFactor, scaleFactor);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            if (scaleHud.getVisibility() != VISIBLE)
            {
                displayScaleHUD();
                if (!persistentScaleFactor)
                {
                    scaleFactor = minScaleFactor + (maxScaleFactor-minScaleFactor)/2;
                }
            }

            if (newScaleFactorToApply == 0.0f)
            {
                newScaleFactorToApply = scaleFactor;
            }

            final float prevScaleFactorToApply = newScaleFactorToApply;
            newScaleFactorToApply *= detector.getScaleFactor();

            if (newScaleFactorToApply < minScaleFactor)
            {
                newScaleFactorToApply = minScaleFactor;
            }

            if (newScaleFactorToApply > maxScaleFactor)
            {
                newScaleFactorToApply = maxScaleFactor;
            }

            updateScaleHUD(prevScaleFactorToApply);
            return true;
        }
    }

    public interface ScaleHudEventListener
    {
        void onHudShown(RTPinchToZoomFrameLayout host);

        void onHudHidden(RTPinchToZoomFrameLayout host);

        void onScaleChanging(RTPinchToZoomFrameLayout host, float initialScaleFactor, float previousScaleFactorBeingSet, float currentScaleFactorBeingSet);
    }

    public interface ScaleEventListener
    {
        void onScaleChanged(float previousScaleFactor, float newScaleFactor);
    }
}
