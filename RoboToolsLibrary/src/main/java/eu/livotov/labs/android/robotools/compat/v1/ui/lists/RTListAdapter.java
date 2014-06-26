package eu.livotov.labs.android.robotools.compat.v1.ui.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import eu.livotov.labs.android.robotools.compat.v1.async.RTAsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 03/03/2013
 */
public abstract class RTListAdapter<T extends Object> extends BaseAdapter
{

    protected List<T> data = new ArrayList<T>();
    protected Context ctx;
    private List<RTListAdapterEventListener> listAdapterEventListeners = new ArrayList<RTListAdapterEventListener>();

    public RTListAdapter(final Context ctx)
    {
        super();
        this.ctx = ctx;
    }

    protected abstract RTListHolder<T> createListHolder(final int itemViewType);

    protected abstract Collection<T> loadDataInBackgroundThread();

    protected abstract int getListItemLayoutResource(final int itemViewType);

    protected View createListItemView(final int itemViewType)
    {
        return null;
    }

    public void addListAdapterEventListener(final RTListAdapterEventListener listAdapterEventListener)
    {
        this.listAdapterEventListeners.add(listAdapterEventListener);
    }

    public void setListAdapterEventListeners(final RTListAdapterEventListener listAdapterEventListeners)
    {
        addListAdapterEventListener(listAdapterEventListeners);
    }

    public void removeListAdapterEventListener(final RTListAdapterEventListener listAdapterEventListener)
    {
        this.listAdapterEventListeners.remove(listAdapterEventListener);
    }

    public void refresh()
    {
        new RTAsyncTask<Object, Object, Collection<T>>()
        {
            public Collection<T> performExecutionThread(final Object... parameters)
            {
                return loadDataInBackgroundThread();
            }

            public void onExecutionStarted()
            {
                onDataRefreshStarted();
            }

            public void onExecutionFinished(final Collection<T> ts)
            {
                data.clear();
                data.addAll(ts);
                notifyDataSetChanged();
                onDataRefreshEnded();
            }

            public void onExecutionFailed(final Throwable error)
            {
                onDataRefreshFailed(error);
            }

            public void onExecutionAborted()
            {
                onDataRefreshEnded();
            }
        }.executeAsync();
    }

    public int getCount()
    {
        return data.size();
    }

    public T getItem(final int i)
    {
        return data.get(i);
    }

    public long getItemId(final int i)
    {
        return (long) i;
    }

    public View getView(final int i, View view, final ViewGroup viewGroup)
    {
        T item = getItem(i);
        final int itemType = getItemViewType(i);

        if (view == null)
        {
            int resId = getListItemLayoutResource(itemType);

            if (resId > 0)
            {
                view = LayoutInflater.from(ctx).inflate(resId, null);
            } else
            {
                view = createListItemView(itemType);
            }

            createListHolder(itemType).attachToView(view);
        }

        ((RTListHolder<T>) view.getTag()).set(item, i, this);
        return view;
    }

    public Context getContext()
    {
        return ctx;
    }

    protected void onDataRefreshStarted()
    {
        for (RTListAdapterEventListener listener : listAdapterEventListeners)
        {
            listener.onDataRefreshStarted();
        }
    }

    protected void onDataRefreshEnded()
    {
        for (RTListAdapterEventListener listener : listAdapterEventListeners)
        {
            listener.onDataRefreshEnded();
        }
    }

    protected void onDataRefreshFailed(Throwable err)
    {
        for (RTListAdapterEventListener listener : listAdapterEventListeners)
        {
            listener.onDataRefreshFailed(err);
        }
    }

    public void appendInstantData(final Collection<T> res)
    {
        data.addAll(res);
        notifyDataSetChanged();
    }

    public interface RTListAdapterEventListener
    {

        void onDataRefreshStarted();

        void onDataRefreshEnded();

        void onDataRefreshFailed(Throwable err);
    }
}
