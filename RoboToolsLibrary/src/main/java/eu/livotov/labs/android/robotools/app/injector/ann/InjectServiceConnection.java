package eu.livotov.labs.android.robotools.app.injector.ann;

import android.app.Activity;
import android.app.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects and manages the service connection {@link android.content.ServiceConnection}.
 *
 * Annotated field must be a subclass of {@link android.content.ServiceConnection}.
 *
 * Important: you must instantiate your service connection manually in order to become managed by this annotation.
 *
 * Applicable to {@link android.app.Activity}, {@link android.app.Fragment}, {@link android.app.Service}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectServiceConnection {

    /**
     * @return класс вызываемого сервиса.
     */
    Class<? extends Service> value();

    /**
     * Флаг для запуска подключения к сервису.
     */
    int flag() default Activity.BIND_AUTO_CREATE;

    static final class MetaData {

        public final int flag;
        public final Class<? extends Service> clazz;

        public MetaData(InjectServiceConnection connection) {
            flag = connection.flag();
            clazz = connection.value();
        }

    }
}
