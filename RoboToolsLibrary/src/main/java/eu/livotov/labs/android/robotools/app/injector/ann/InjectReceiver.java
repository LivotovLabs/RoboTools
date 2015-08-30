package eu.livotov.labs.android.robotools.app.injector.ann;

import android.content.IntentFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects and manages {@link android.content.BroadcastReceiver}.
 * <p/>
 * Annotated field must be of type {@link android.content.BroadcastReceiver}.
 * <p/>
 * Important: you must instantiate the instance of annotated {@link android.content.BroadcastReceiver} field manually.
 * <p/>
 * Applicable to  {@link android.app.Activity}, {@link android.app.Fragment}, {@link android.app.Service}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectReceiver {

    String[] action() default {};

    String[] category() default {};

    static final class Processor {
        public static IntentFilter process(InjectReceiver receiver) {
            IntentFilter filter = new IntentFilter();
            String[] actions = receiver.action();
            String[] categories = receiver.category();

            if (actions != null) {
                for (String action : actions) {
                    filter.addAction(action);
                }
            }
            if (categories != null) {
                for (String category : categories) {
                    filter.addCategory(category);
                }
            }
            return filter;
        }
    }
}
