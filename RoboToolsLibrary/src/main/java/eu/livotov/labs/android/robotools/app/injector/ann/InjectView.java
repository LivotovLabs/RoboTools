package eu.livotov.labs.android.robotools.app.injector.ann;

import android.support.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Injects view instance (frompreviously loaded layout)
 * Annotated field must be a subclass of {@link android.view.View}.
 *
 * Applicable to {@link android.app.Activity}, {@link android.app.Fragment}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectView {

    /**
     * @return id View для инициализации.
     */
    @IdRes int value();

    static final class MetaData {
        public final int id;

        public MetaData(InjectView view) {
            this.id = view.value();
        }
    }
}
