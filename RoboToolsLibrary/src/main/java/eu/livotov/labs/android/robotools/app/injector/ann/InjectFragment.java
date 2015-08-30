package eu.livotov.labs.android.robotools.app.injector.ann;

import android.support.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects current fragment instance.  {@link android.app.Fragment}.
 * In case fragment is found by the supplied ID, it is bound. If not - new fragment will be instantiated and added (via transaction), then bound.
 *
 * Applicable to {@link android.app.Activity} or {@link android.app.Fragment} (only for nested subfragments)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectFragment {

    /**
     * @return ID, в который будет помещен фрагмент.
     */
    @IdRes int value(); // todo: добавить тег

}
