package eu.livotov.labs.android.robotools.graphics;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * ImageView which is always square
 */
public class RTSquareImageView extends ImageView
{


    public RTSquareImageView(Context context)
    {
        this(context, null);
    }

    public RTSquareImageView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public RTSquareImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
