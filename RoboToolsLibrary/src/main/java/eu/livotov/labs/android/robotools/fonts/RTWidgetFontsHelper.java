package eu.livotov.labs.android.robotools.fonts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Small helper to solve the problem to display text with custom font in the {@link android.appwidget.AppWidgetProvider}
 * <br/> This helper build {@link Bitmap} object with given text and params.
 * <br/> So just set result bitmap into {@link android.widget.ImageView} in your widget
 * <br/>
 * @author Grishko Nikita
 *         on 24.12.2015.
 */
public class RTWidgetFontsHelper {

    private Context context;
    private String text;
    private int fontSizeSP = 18;
    private int color = Color.BLACK;
    private String typeface;

    private Map<String, Bitmap> mBitmapCache = new WeakHashMap<>();

    private RTWidgetFontsHelper(Context context) {
        this.context = context;
    }

    private static RTWidgetFontsHelper sInstance;

    public static RTWidgetFontsHelper with(Context context) {
        if (sInstance == null) {
            sInstance = new RTWidgetFontsHelper(context);
        }
        return sInstance;
    }

    /**
     * Set text to display
     */
    public RTWidgetFontsHelper text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Set text size
     */
    public RTWidgetFontsHelper size(int size) {
        this.fontSizeSP = size;
        return this;
    }

    /**
     * Set text color
     */
    public RTWidgetFontsHelper color(int resId) {
        if (context == null)
            throw new IllegalArgumentException("Context must be init first");
        this.color = context.getResources().getColor(resId);
        return this;
    }

    /**
     * Set typeface
     */
    public RTWidgetFontsHelper typeface(String typefaceName) {
        this.typeface = typefaceName;
        return this;
    }

    /**
     * Cache key for bitmaps, is one of params were changed new bitmap will be created and put into cache with new generated key
     */
    @NonNull
    private String key() {
        return String.format("%s%s%s%s", text, typeface, fontSizeSP, color);
    }

    /**
     * Creates text bitmap with given params: {@link #color}, {@link #text}, {@link #fontSizeSP}, {@link #typeface}
     */
    public Bitmap build() {
        if (this.context == null)
            throw new IllegalArgumentException("Context must be not null");
        if (TextUtils.isEmpty(this.text))
            throw new IllegalArgumentException("Text must be not empty");
        if (TextUtils.isEmpty(this.typeface))
            throw new IllegalArgumentException("Typeface must be not empty");
        return drawText(context, text, fontSizeSP, color, RTFontsHelper.getTypeface(context, typeface));
    }

    private static Bitmap drawText(Context context, String text, int fontSizeSP, int color, Typeface typeface) {
        if (!TextUtils.isEmpty(sInstance.key()) && sInstance.mBitmapCache.containsKey(sInstance.key())) {
            return sInstance.mBitmapCache.get(sInstance.key());
        }
        int fontSizePX = convertDipToPix(context, fontSizeSP);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(typeface);
        paint.setTextSize(fontSizePX);
        paint.setColor(color);

        int padding = (fontSizePX / 10);
        int width = (int) (paint.measureText(text) + padding * 2);
        int height = (int) (fontSizePX + padding * 2);

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(result);

        int theY = fontSizePX;

        canvas.drawText(text, padding, theY, paint);
        sInstance.mBitmapCache.put(sInstance.key(), result);
        return result;
    }

    private static int convertDipToPix(Context context, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dip, context.getResources().getDisplayMetrics());
    }

}
