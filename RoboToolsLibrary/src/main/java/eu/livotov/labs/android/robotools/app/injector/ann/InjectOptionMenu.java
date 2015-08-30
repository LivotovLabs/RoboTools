package eu.livotov.labs.android.robotools.app.injector.ann;

import android.support.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects option menu item of {@link android.app.Activity} или {@link android.app.Fragment}.
 * Annotated field must be of type {@link android.view.MenuItem}
 * Works only if the menu was injected by the InjectLayout annotation
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectOptionMenu {

    /**
     * @return id menu item ID
     */
    @IdRes int value();

}
