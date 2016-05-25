package eu.livotov.labs.android.robotools.graphics;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;


public class RTPlaceholder {

    public static Drawable whiteRect(int w, int h) {
        ShapeDrawable rect = new ShapeDrawable(new RectShape());
        rect.setIntrinsicHeight(h);
        rect.setIntrinsicWidth(w);
        rect.getPaint().setColor(Color.WHITE);
        return rect;
    }

    public static Drawable rect(int w, int h, int color) {
        ShapeDrawable rect = new ShapeDrawable(new RectShape());
        rect.setIntrinsicHeight(h);
        rect.setIntrinsicWidth(w);
        rect.getPaint().setColor(color);
        return rect;
    }

    public Drawable round(int diameter, int color) {
        ShapeDrawable rect = new ShapeDrawable(new OvalShape());
        rect.setIntrinsicHeight(diameter);
        rect.setIntrinsicWidth(diameter);
        rect.getPaint().setColor(color);
        return rect;
    }

}
