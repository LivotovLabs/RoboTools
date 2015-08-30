package eu.livotov.labs.android.robotools.app.injector.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Injects and inflates layout and optionally menu and title. for {@link android.app.Activity} or {@link android.app.Fragment}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectLayout {

    /**
     * @return layout res id
     */
     int value();

    /**
//     * @return id menu res if
//     */
       int menu() default 0;

    /**
     * @return title string res id to be set to action bar title of activity
     */
     int title() default 0;

    static final class MetaData {
        public final int layout;
        public final int title;
        public final int menu;

        public MetaData(InjectLayout inject) {
            this.layout = inject.value();
            this.title = inject.title();
            this.menu = inject.menu();
        }
    }
}
