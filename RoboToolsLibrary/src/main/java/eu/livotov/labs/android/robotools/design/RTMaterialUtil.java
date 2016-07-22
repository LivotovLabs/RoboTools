package eu.livotov.labs.android.robotools.design;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * Created by dlivotov on 10/07/2016.
 */

public class RTMaterialUtil
{

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarColorRes(final Activity activity, @ColorRes final int color)
    {
        setNavigationBarColor(activity, activity.getResources().getColor(color));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarColor(final Activity activity, @ColorInt final int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(color);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setSystemStatusBarColorRes(final Activity activity, @ColorRes final int color)
    {
        setSystemStatusBarColor(activity, activity.getResources().getColor(color));;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setSystemStatusBarColor(final Activity activity, @ColorInt final int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    /**
     * Tints toolbar's menu item icons with the specified tint color
     * @param toolbar
     * @param tintColorRes
     */
    public static void colorizeToolbarActions(Toolbar toolbar, @ColorRes int tintColorRes)
    {
        final int menuSize = toolbar.getMenu().size();

        for (int i = 0; i < menuSize; i++)
        {
            final MenuItem item = toolbar.getMenu().getItem(i);
            Drawable drawable = item.getIcon();

            if (drawable != null)
            {
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, toolbar.getContext().getResources().getColor(tintColorRes));
                item.setIcon(drawable);
            }
        }

        final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(toolbar.getContext().getResources().getColor(tintColorRes), PorterDuff.Mode.MULTIPLY);
        for (int i = 0; i < toolbar.getChildCount(); i++)
        {
            final View v = toolbar.getChildAt(i);

            if (v instanceof ImageButton)
            {
                //Action Bar back button
                ((ImageButton) v).getDrawable().setColorFilter(colorFilter);
            }
        }

        toolbar.invalidate();
    }

    /**
     * Sets toolbar's navigation icon with the specified tint color
     * @param toolbar
     * @param iconRes
     * @param tintColorRes
     */
    public static void setNavigationItem(Toolbar toolbar, @DrawableRes int iconRes, @ColorRes int tintColorRes)
    {
        final Drawable icon = DrawableCompat.wrap(toolbar.getContext().getResources().getDrawable(iconRes));
        DrawableCompat.setTint(icon, toolbar.getContext().getResources().getColor(tintColorRes));
        toolbar.setNavigationIcon(icon);
    }
}
