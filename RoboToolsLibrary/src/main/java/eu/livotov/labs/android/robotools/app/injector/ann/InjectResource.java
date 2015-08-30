package eu.livotov.labs.android.robotools.app.injector.ann;

import android.animation.Animator;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.view.animation.Animation;
import android.view.animation.Interpolator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Injects resource value
 * <p/>
 * The following resource types are supported:
 * <p/>
 * {@link CharSequence}, {@link String} -- R.string
 * {@link CharSequence[]}, {@link String[]} -- R.array
 * <p/>
 * {@link android.graphics.drawable.Drawable} -- R.drawable
 * {@link boolean} -- R.bool
 * {@link int} -- R.color
 * {@link float} -- R.dimen
 * {@link android.content.res.XmlResourceParser} -- R.xml
 * <p/>
 * Applicable to {@link android.app.Activity}, {@link android.app.Fragment}, {@link android.app.Service}, {@link android.app.Application}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectResource {

    /**
     * @return id of resource to inject
     */
    @AnyRes int value();

    static class MetaData {
        public final int id;
        public final Type type;

        public MetaData(InjectResource resource, Field field) {
            this.id = resource.value();
            Class clazz = field.getType();

            if (CharSequence.class == clazz) {
                type = Type.CharSequence;
            } else if (String.class == clazz) {
                type = Type.String;
            } else if (CharSequence[].class == clazz) {
                type = Type.CharSequenceArray;
            } else if (String[].class == clazz) {
                type = Type.StringArray;
            } else if (Drawable.class.isAssignableFrom(clazz)) {
                type = Type.Drawable;
            } else if (int.class == clazz) {
                type = Type.Int_;
            } else if (float.class == clazz) {
                type = Type.Float_;
            } else if (boolean.class == clazz) {
                type = Type.Boolean_;
            } else if (int[].class == clazz) {
                type = Type.IntArray;
            } else if (Animation.class.isAssignableFrom(clazz)) {
                type = Type.Animation;
            } else if (Interpolator.class.isAssignableFrom(clazz)) {
                type = Type.Interpolator;
            } else if (Animator.class.isAssignableFrom(clazz)) {
                type = Type.Animator;
            } else if (XmlResourceParser.class == clazz) {
                type = Type.XmlResourceParser;
            } else if (Movie.class == clazz) {
                type = Type.Movie;
            } else if (ColorStateList.class == clazz) {
                type = Type.ColorStateList;
            } else if (Boolean.class == clazz) {
                type = Type.Boolean;
            } else if (Integer.class == clazz) {
                type = Type.Integer;
            } else if (Float.class == clazz) {
                type = Type.Float;
            } else {
                type = null;
            }
        }

        public static enum Type {
            CharSequence,
            String,
            CharSequenceArray,
            StringArray,
            Drawable,
            Int_,
            Float_,
            Boolean_,
            IntArray,
            Animation,
            Interpolator,
            Animator,
            XmlResourceParser,
            Movie,
            ColorStateList,
            Boolean,
            Integer,
            Float
        }
    }

}
