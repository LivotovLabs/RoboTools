package eu.livotov.labs.android.robotools.app.design.bottomsheet.layout;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;


public class BottomSheetHeaderLayout extends FrameLayout
{
    private int mHeaderWidth = 1;


    public BottomSheetHeaderLayout(Context context)
    {
        super(context);
    }


    public BottomSheetHeaderLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public BottomSheetHeaderLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setHeaderWidth(int width)
    {
        mHeaderWidth = width;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMeasureSpecNew = mHeaderWidth == 1 ? widthMeasureSpec : MeasureSpec.makeMeasureSpec(mHeaderWidth, MeasureSpec.getMode(widthMeasureSpec));
        super.onMeasure(widthMeasureSpecNew, heightMeasureSpec);
    }
}