package eu.livotov.labs.android.robotools.app.injector.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects appbar {@link android.app.ActionBar}.
 *
 * Type of annotated field must be a subclass of {@link android.app.ActionBar}
 * or the fields will be left blank with the logcat warning.
 *
 * Applicable to к {@link android.app.Activity} и {@link android.app.Fragment}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectActionBar {

}
