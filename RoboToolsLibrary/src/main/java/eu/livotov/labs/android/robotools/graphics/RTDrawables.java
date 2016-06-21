package eu.livotov.labs.android.robotools.graphics;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.ColorInt;

/**
 * Helper to quickly create simple drawables
 */
public class RTDrawables
{

    /**
     * Creates new oval drawable
     * @param diameter diameter
     * @param color color int
     * @return oval drawable
     */
    public static Drawable makeRound(int diameter, @ColorInt int color)
    {
        ShapeDrawable round = new ShapeDrawable(new OvalShape());
        round.setIntrinsicHeight(diameter);
        round.setIntrinsicWidth(diameter);
        round.getPaint().setAntiAlias(true);
        round.getPaint().setColor(color);
        return round;
    }

    /**
     * Creates new stroked circle drawable
     * @param diameter
     * @param strokeWidth
     * @param colorStroke
     * @return
     */
    public static Drawable makeCircle(int diameter, int strokeWidth, @ColorInt int colorStroke)
    {
        ShapeDrawable round = new ShapeDrawable(new OvalShape());
        round.setIntrinsicHeight(diameter);
        round.setIntrinsicWidth(diameter);
        round.getPaint().setAntiAlias(true);
        round.getPaint().setStyle(Paint.Style.STROKE);
        round.getPaint().setStrokeWidth(strokeWidth);
        round.getPaint().setColor(colorStroke);
        return round;
    }

    /**
     * Creates new white colored rect
     * @param w
     * @param h
     * @return
     */
    public static Drawable makeRectOfWhiteColor(int w, int h)
    {
        return makeRect(w, h, Color.WHITE);
    }

    /**
     * Creates new rect
     * @param w
     * @param h
     * @param color
     * @return
     */
    public static Drawable makeRect(int w, int h, @ColorInt int color)
    {
        ShapeDrawable rect = new ShapeDrawable(new RectShape());
        rect.setIntrinsicHeight(h);
        rect.setIntrinsicWidth(w);
        rect.getPaint().setAntiAlias(true);
        rect.getPaint().setColor(color);
        return rect;
    }

    /**
     * Creates new square
     * @param size
     * @param color
     * @return
     */
    public static Drawable makeSquare(int size, int color)
    {
        return makeRect(size, size, color);
    }
}
