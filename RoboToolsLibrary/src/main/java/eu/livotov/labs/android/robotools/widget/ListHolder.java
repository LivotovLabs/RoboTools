package eu.livotov.labs.android.robotools.widget;

import android.view.View;
import android.view.ViewGroup;

import eu.livotov.labs.android.robotools.injector.Injector;

public abstract class ListHolder<T> {

    protected View mRootView;

    /**
     * Инициазирует View
     * @param parent родительский View для инфлатинга и инициализации
     * @return View для возврата
     */
    public ListHolder<T> inflate(ViewGroup parent) {
        mRootView = Injector.init(this, parent);
        if(mRootView != null) {
            mRootView.setTag(this);
        }
        return this;
    }

    /**
     * Заполняет View в соответствии с занным значением.
     * @param data значение для заполнения
     * @return rootView
     */
    public abstract View set(T data);

}
