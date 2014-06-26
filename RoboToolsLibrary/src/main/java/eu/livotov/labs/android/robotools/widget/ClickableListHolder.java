package eu.livotov.labs.android.robotools.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * Класс-адаптер от {@link android.view.View.OnClickListener} к {@link android.widget.AdapterView.OnItemClickListener}
 *
 * Использование:
 * 1) Наследуйте от него ваш ViewHolder.
 * 2) Передайте в конструктор ваш {@link ClickableListHolder.OnItemClickListener}
 * 3) В {@link android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)} вызывайте {@link #updatePosition(int)}
 * 4) Назначьте класс обработчиком всевозможный кнопок в вашем list_item
 * 5) В случае нажатия кнопки сработает {@link ClickableListHolder.OnItemClickListener#onItemClick(android.view.View, android.view.View, int)}
 */
public abstract class ClickableListHolder<T> extends ListHolder implements View.OnClickListener {

    private OnItemClickListener listener;
    private int position;

    public ClickableListHolder(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ClickableListHolder<T> updatePosition(int position) {
        this.position = position;
        return this;
    }

    @Override
    public void onClick(View v) {
        if(listener != null) {
            listener.onItemClick(mRootView, v, position);
        }
    }

    public static interface OnItemClickListener {

        void onItemClick(View itemView, View clickedView, int position);
    }

}
