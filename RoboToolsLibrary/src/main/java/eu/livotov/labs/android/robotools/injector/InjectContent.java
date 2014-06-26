package eu.livotov.labs.android.robotools.injector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Позволяет явно задать layout для Activity или Fragment.
 * Применяется к Активности или Фрагменту.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectContent {
    int value();
}
