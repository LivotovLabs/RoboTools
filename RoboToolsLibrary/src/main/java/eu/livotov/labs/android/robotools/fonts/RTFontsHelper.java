package eu.livotov.labs.android.robotools.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Very simple helper for quick load and cache typeface
 */
public class RTFontsHelper
{

    private static LruCache<String, Typeface> sFontsCache = new LruCache<>(8);


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
}
