package eu.livotov.labs.android.robotools.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;
import eu.livotov.labs.android.robotools.graphics.Bitmaps;

public class GrayscaleImageView extends ImageView {

    public GrayscaleImageView(final Context context) {
        super(context);
    }

    public GrayscaleImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public GrayscaleImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageResource(final int resId) {
        if (resId > 0) {
            super.setImageBitmap(Bitmaps.toGrayscale(BitmapFactory.decodeResource(getResources(), resId)));
        } else {
            super.setImageResource(resId);
        }
    }

    public void setImageBitmap(final Bitmap bm) {
        if (bm != null) {
            super.setImageBitmap(Bitmaps.toGrayscale(bm));
        } else {
            super.setImageResource(0);
        }
    }
}