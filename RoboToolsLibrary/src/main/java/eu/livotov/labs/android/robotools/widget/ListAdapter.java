package eu.livotov.labs.android.robotools.widget;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.livotov.labs.android.robotools.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ListAdapter<T> extends BaseCollectionAdapter<T> {

    protected List<T> data = new ArrayList<T>();
    protected Context ctx;
    private List<RTListAdapterEventListener> listAdapterEventListeners = new ArrayList<RTListAdapterEventListener>();

    public ListAdapter(final Context ctx) {
        this.ctx = ctx;
    }

    protected abstract ClickableListHolder<T> createListHolder(final int itemViewType, View view);

    protected abstract Collection<T> loadDataInBackgroundThread();

    protected abstract int getListItemLayoutResource(final int itemViewType);

    protected View createListItemView(final int itemViewType) {
        return null;
    }

    public void addListAdapterEventListener(final RTListAdapterEventListener listAdapterEventListener) {
        this.listAdapterEventListeners.add(listAdapterEventListener);
    }

    public void setListAdapterEventListeners(final RTListAdapterEventListener listAdapterEventListeners) {
        addListAdapterEventListener(listAdapterEventListeners);
    }

    public void removeListAdapterEventListener(final RTListAdapterEventListener listAdapterEventListener) {
        this.listAdapterEventListeners.remove(listAdapterEventListener);
    }

    public void refresh() {
        new AsyncTask<Object, Object, Collection<T>>() {
            public Collection<T> doInBackground(final Object... parameters) {
                return loadDataInBackgroundThread();
            }

            @Override
            protected void onPreExecute() {
                onDataRefreshStarted();
            }

            @Override
            protected void onCanceled(Collection<T> ts) {
                onDataRefreshEnded();
            }

            @Override
            protected void onError(Throwable t) {
                onDataRefreshFailed(t);
            }

            @Override
            protected void onPostExecute(Collection<T> ts) {
                clear();
                addAll(ts);
                onDataRefreshEnded();
            }

        }.execPool();
    }

    public int getCount() {
        return data.size();
    }

    public T getItem(final int i) {
        return data.get(i);
    }

    public long getItemId(final int i) {
        return (long) i;
    }

    public View getView(final int i, View view, final ViewGroup viewGroup) {
        T item = getItem(i);
        final int itemType = getItemViewType(i);

        if (view == null) {
            int resId = getListItemLayoutResource(itemType);

            if (resId > 0) {
                view = LayoutInflater.from(ctx).inflate(resId, null);
            } else {
                view = createListItemView(itemType);
            }
            createListHolder(itemType, view);
        }

        ((ClickableListHolder<T>) view.getTag()).set(item);
        ((ClickableListHolder<T>) view.getTag()).updatePosition(i);
        return view;
    }

    public Context getContext() {
        return ctx;
    }

    protected void onDataRefreshStarted() {
        for (RTListAdapterEventListener listener : listAdapterEventListeners) {
            listener.onDataRefreshStarted();
        }
    }

    protected void onDataRefreshEnded() {
        for (RTListAdapterEventListener listener : listAdapterEventListeners) {
            listener.onDataRefreshEnded();
        }
    }

    protected void onDataRefreshFailed(Throwable err) {
        for (RTListAdapterEventListener listener : listAdapterEventListeners) {
            listener.onDataRefreshFailed(err);
        }
    }

    public void appendInstantData(final Collection<T> res) {
        data.addAll(res);
        notifyDataSetChanged();
    }

    public interface RTListAdapterEventListener {

        void onDataRefreshStarted();

        void onDataRefreshEnded();

        void onDataRefreshFailed(Throwable err);
    }
}
