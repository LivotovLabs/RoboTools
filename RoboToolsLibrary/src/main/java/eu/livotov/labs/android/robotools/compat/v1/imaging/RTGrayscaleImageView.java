package eu.livotov.labs.android.robotools.compat.v1.imaging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 25.03.13
 */
public class RTGrayscaleImageView extends ImageView
{

    public RTGrayscaleImageView(final Context context)
    {
        super(context);
    }

    public RTGrayscaleImageView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RTGrayscaleImageView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setImageResource(final int resId)
    {
        if (resId > 0)
        {
            super.setImageBitmap(RTBitmaps.toGrayscale(BitmapFactory.decodeResource(getContext().getResources(), resId)));
        } else
        {
            super.setImageResource(resId);
        }
    }

    public void setImageBitmap(final Bitmap bm)
    {
        if (bm != null)
        {
            super.setImageBitmap(RTBitmaps.toGrayscale(bm));
        } else
        {
            super.setImageResource(0);
        }
    }
}
