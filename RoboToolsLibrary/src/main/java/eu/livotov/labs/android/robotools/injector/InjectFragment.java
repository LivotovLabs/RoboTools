package eu.livotov.labs.android.robotools.injector;

import android.app.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Применяется к полю в Активности или Фрагменте.
 * При запуске активности или фрагмента будет осуществена
 * инциализация фрагмента по выбранному ID.
 *
 * Если фрагмент отсутствует в разметке, программа сама
 * создаст и добавит его.
 *
 * Тип фрагмента - тип поля, к которому применена аннотация.
 *
 * Для Фрагментов работает только с версии 17(вложенные фрагменты появились именно там).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectFragment {
    int value();
    Class<? extends android.support.v4.app.Fragment> fragment();
}
