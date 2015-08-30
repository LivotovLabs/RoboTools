package eu.livotov.labs.android.robotools.app.injector.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects current application instance {@link android.app.Application}.
 *
 * Applicable to {@link android.app.Activity}, {@link android.app.Fragment}, {@link android.app.Service}
 * Не применима в статик-контексте.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectApp {

}
