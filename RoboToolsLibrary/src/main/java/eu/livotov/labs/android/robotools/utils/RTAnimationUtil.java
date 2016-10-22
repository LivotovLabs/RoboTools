package eu.livotov.labs.android.robotools.utils;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by dlivotov on 22/10/2016.
 */

public class RTAnimationUtil
{
    public static void revealView(View oldView, View newView)
    {
        revealView(oldView, newView, 0, 0);
    }

    public static void revealView(View oldView, View newView, int w, int h)
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            revealViewAnimated(oldView, newView, w, h);
        }
        else
        {
            newView.setVisibility(View.VISIBLE);
            oldView.setVisibility(View.INVISIBLE);
        }
    }

    @TargetApi(21)
    private static void revealViewAnimated(View oldView, View newView, int w, int h)
    {
        final int width = w == 0 ? oldView.getWidth() : w;
        final int height = h == 0 ? oldView.getHeight() : h;
        float maxRadius = (float) Math.sqrt(width * width / 4 + height * height / 4);
        Animator reveal = ViewAnimationUtils.createCircularReveal(newView, width / 2, height / 2, 0, maxRadius);
        newView.setVisibility(View.VISIBLE);
        oldView.setVisibility(View.INVISIBLE);
        reveal.start();
    }
}
