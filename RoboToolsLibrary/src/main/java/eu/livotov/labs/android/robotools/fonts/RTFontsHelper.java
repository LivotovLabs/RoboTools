package eu.livotov.labs.android.robotools.fonts;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Very simple helper for quick load and cache typeface
 */
public class RTFontsHelper
{
    private static final int WIDTH_PX = 200;
    private static final int HEIGHT_PX = 80;

    private static final LruCache<String, Typeface> sFontsCache = new LruCache<>(8);
    private static final Map<String, Boolean> supportMap = new HashMap<>();


    protected RTFontsHelper()
    {
    }

    public static Typeface getTypeface(Context context, final String name)
    {
        Typeface typeface = sFontsCache.get(name);

        if (typeface == null)
        {
            typeface = Typeface.createFromAsset(context.getResources().getAssets(), name);
            if (typeface == null)
            {
                Log.e("RTFontsHelper", "Cant init typeface: " + name);
                return null;
            }
            sFontsCache.put(name, typeface);
        }
        return typeface;
    }

    /**
     * Checks if the given symbol or all symbols from the given string are supported on the device
     * @param cxt
     * @param text
     * @return
     */
    public static boolean isSupported(Context cxt, String text)
    {
        if (!supportMap.containsKey(text))
        {
            int w = WIDTH_PX, h = HEIGHT_PX;
            Resources resources = cxt.getResources();
            float scale = resources.getDisplayMetrics().density;
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
            Bitmap orig = bitmap.copy(conf, false);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(0, 0, 0));
            paint.setTextSize((int) (14 * scale));

            // draw text to the Canvas center
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int x = (bitmap.getWidth() - bounds.width()) / 2;
            int y = (bitmap.getHeight() + bounds.height()) / 2;

            canvas.drawText(text, x, y, paint);
            supportMap.put(text, !orig.sameAs(bitmap));

            orig.recycle();
            bitmap.recycle();
        }

        return supportMap.get(text);
    }
}
