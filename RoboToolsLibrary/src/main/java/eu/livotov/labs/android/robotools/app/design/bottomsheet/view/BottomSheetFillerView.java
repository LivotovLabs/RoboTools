package eu.livotov.labs.android.robotools.app.design.bottomsheet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class BottomSheetFillerView extends LinearLayout
{
    private View mMeasureTarget;


    public BottomSheetFillerView(Context context)
    {
        super(context);
    }


    public BottomSheetFillerView(Context context, AttributeSet attrs)
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