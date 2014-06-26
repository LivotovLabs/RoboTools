package eu.livotov.labs.android.robotools.injector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * При применении данной аннотации на полях активности или фрагмента
 * эти поля будут проинициализированны в соответствии с заданным ID.
 *
 * Аннотация применяется к полям Активности или Фрагмента.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectView {
    int value();
}
