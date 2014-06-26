package eu.livotov.labs.android.robotools.injector;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

/**
 * Аннотация позволяет подключить неограниченное количество
 * слушателей событий к View.
 *
 * Слушатели выбираются следующим образом:
 *  1. Если Активность или Фрагмент реализуют интерфейс слушателя, слушателем служат они.
 *  2. Если для аннотации явно задан класс слушателя, аннотация попытается его создать и использовать.
 *  Для вспешной работы слушатель должен быть доступным статическим или внешним классом с пустым конструктором.
 *  3. В противном случае будет осуществлен перебор по всем полям Активности и Фрагмента и поиск объекта с аннотацией
 *  {@link eu.livotov.labs.android.robotools.injector.Listener} и подходящим типом интерфейса.
 *  Первыо найденное поле будет служить в качестве Слушателя.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handle {

    Type[] value();

    Class handler() default Object.class;

    /**
     * Тип событий View, которые можно прослушать
     */
    public static enum Type {

        /**
         * {@link android.view.View.OnClickListener}
         */
        CLICK,

        /**
         * {@link android.widget.AdapterView.OnItemClickListener}
         */
        ITEM_CLICK,

        /**
         * {@link android.view.View.OnLongClickListener}
         */
        LONG_CLICK,

        /**
         * {@link android.view.View.OnCreateContextMenuListener}
         */
        CONTEXT_MENU,

        /**
         * {@link android.widget.AbsListView.OnScrollListener}
         */
        SCROLL,

        /**
         * {@link android.view.View.OnTouchListener}
         *
         */
        TOUCH,

        /**
         * {@link android.view.ViewTreeObserver.OnPreDrawListener}
         */
        PRE_DRAW,

        /**
         * {@link android.view.ViewTreeObserver.OnDrawListener}
         */
        DRAW,

        /**
         * {@link android.view.ViewTreeObserver.OnGlobalLayoutListener}
         */
        GLOBAL_LAYOUT,

        /**
         * {@link android.view.View.OnKeyListener}
         */
        KEY,

        /**
         * {@link android.widget.ListAdapter}
         */
        ADAPTER,

        /**
         * {@link android.text.TextWatcher}
         */
        TEXT_EDIT
    }
}
