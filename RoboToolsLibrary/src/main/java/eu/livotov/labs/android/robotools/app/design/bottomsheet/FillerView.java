package eu.livotov.labs.android.robotools.app.design.bottomsheet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

class FillerView extends LinearLayout
{
    private View mMeasureTarget;


    public FillerView(Context context)
    {
        super(context);
    }


    public FillerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void setMeasureTarget(View lastViewSeen)
    {
        mMeasureTarget = lastViewSeen;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        if (null != mMeasureTarget)
        {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMeasureTarget.getMeasuredHeight(), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}