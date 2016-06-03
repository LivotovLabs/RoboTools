package eu.livotov.labs.android.robotools.graphics;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;

/**
 * Small helper to quickly create simple drawables:
 * <br/> - Rect
 * <br/> - Square
 * <br/> - Round
 * <br/> - Circle
 */
public class RTDrawables {

    public static Drawable rect(int w, int h, int color) {
        ShapeDrawable rect = new ShapeDrawable(new RectShape());
        rect.setIntrinsicHeight(h);
        rect.setIntrinsicWidth(w);
        rect.getPaint().setAntiAlias(true);
        rect.getPaint().setColor(color);
        return rect;
    }

    public static Drawable round(int diameter, int color) {
        ShapeDrawable round = new ShapeDrawable(new OvalShape());
        round.setIntrinsicHeight(diameter);
        round.setIntrinsicWidth(diameter);
        round.getPaint().setAntiAlias(true);
        round.getPaint().setColor(color);
        return round;
    }

    public static Drawable circle(int diameter, int strokeWidth, int colorStroke) {
        ShapeDrawable round = new ShapeDrawable(new OvalShape());
        round.setIntrinsicHeight(diameter);
        round.setIntrinsicWidth(diameter);
        round.getPaint().setAntiAlias(true);
        round.getPaint().setStyle(Paint.Style.STROKE);
        round.getPaint().setColor(colorStroke);
        return round;
    }

    public static Drawable rectWhite(int w, int h) {
        return rect(w, h, Color.WHITE);
    }

    public static Drawable square(int size, int color) {
        return rect(size, size, color);
    }
}
