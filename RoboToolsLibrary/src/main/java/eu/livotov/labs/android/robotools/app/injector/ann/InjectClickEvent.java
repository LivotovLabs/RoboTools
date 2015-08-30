package eu.livotov.labs.android.robotools.app.injector.ann;

import android.support.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns {@link android.view.View.OnClickListener} to the annotated method.
 * Annotated method MUST NOT contains any parameters.
 *
 * Applicable to {@link android.app.Activity}, {@link android.app.Fragment}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectClickEvent {

    /**
     * View ID to bind onClick event from.
     */
    @IdRes int[] value();
}
