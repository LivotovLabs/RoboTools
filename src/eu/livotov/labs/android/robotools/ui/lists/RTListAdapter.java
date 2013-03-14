package eu.livotov.labs.android.robotools.ui.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import eu.livotov.labs.android.robotools.async.RTAsyncTask;

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

    private RTListAdapterEventListener listAdapterEventListener;

    protected Context ctx;

    protected abstract RTListHolder<T> createListHolder();

    protected abstract Collection<T> loadDataInBackgroundThread();

    protected abstract int getListItemLayoutResource(int itemViewType);

    protected abstract View createListItemView(int itemViewType);


    public RTListAdapter(final Context ctx)
    {
        super();
        this.ctx = ctx;
    }

    public RTListAdapterEventListener getListAdapterEventListener()
    {
        return listAdapterEventListener;
    }

    public void setListAdapterEventListener(final RTListAdapterEventListener listAdapterEventListener)
    {
        this.listAdapterEventListener = listAdapterEventListener;
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

            public void onExecutionError(final Throwable error)
            {
                onDataRefreshEnded();
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
        T item = data.get(i);

        if (view == null)
        {
            final int itemViewType = getItemViewType(i);
            final int resId = getListItemLayoutResource(itemViewType);

            if (resId > 0)
            {
                view = LayoutInflater.from(ctx).inflate(resId, null);
            } else
            {
                view = createListItemView(itemViewType);
            }
            createListHolder().attachToView(view);
        }

        ((RTListHolder<T>) view.getTag()).set(item);
        return view;
    }

    protected void onDataRefreshStarted()
    {
        if (listAdapterEventListener != null)
        {
            listAdapterEventListener.onDataRefreshStarted();
        }
    }

    protected void onDataRefreshEnded()
    {
        if (listAdapterEventListener != null)
        {
            listAdapterEventListener.onDataRefreshEnded();
        }
    }

    public interface RTListAdapterEventListener
    {

        void onDataRefreshStarted();

        void onDataRefreshEnded();
    }
}
