package eu.livotov.labs.android.robotools.ui.lists;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import eu.livotov.labs.android.robotools.async.RTAsyncTask;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 03/03/2013
 */
public abstract class RTEndlessListAdapter<T extends Object> extends RTListAdapter<T>
{

    private View pendingView = null;
    private AtomicBoolean keepOnAppending = new AtomicBoolean(true);
    private boolean isSerialized = false;


    public RTEndlessListAdapter(Context ctx)
    {
        super(ctx);
    }

    abstract protected int getLoadingViewItemLayoutResource();
    abstract protected Collection<T> loadEndlessBatchInBackground(int currentPayloadItemsCount);
    abstract protected int getRealViewTypeCount();
    abstract protected int getRealItemViewType(int position);

    public T getItem(int position)
    {
        if (position >= super.getCount())
        {
            return (null);
        }

        return (super.getItem(position));
    }

    public int getCount()
    {
        if (keepOnAppending.get())
        {
            return (super.getCount() + 1);
        }

        return (super.getCount());
    }

    public int getViewTypeCount()
    {
        return (getRealViewTypeCount() + 1);
    }

    public int getItemViewType(int position)
    {
        if (position == super.getCount())
        {
            return (IGNORE_ITEM_VIEW_TYPE);
        }

        return (getRealItemViewType(position));
    }

    public boolean areAllItemsEnabled()
    {
        return false; //(super.areAllItemsEnabled());
    }

    public boolean isEnabled(int position)
    {
        if (position >= super.getCount())
        {
            return (false);
        }
        return (super.isEnabled(position));
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (position == super.getCount() && keepOnAppending.get())
        {
            if (pendingView == null)
            {
                pendingView = getPendingView(parent);
                executeAsyncTask();
            }

            return (pendingView);
        }

        return (super.getView(position, convertView, parent));
    }

    private void executeAsyncTask()
    {
        final int realCount = super.getCount();

        new RTAsyncTask<Void, Void, Collection<T>>()
        {

            public Collection<T> performExecutionThread(final Void... parameters)
            {
                if (realCount==0)
                {
                    return loadDataInBackgroundThread();
                } else
                {
                    return loadEndlessBatchInBackground(realCount);
                }
            }

            public void onExecutionStarted()
            {
                onDataRefreshStarted();
            }

            public void onExecutionFinished(final Collection<T> res)
            {
                onDataRefreshEnded();

                if (res.size() > 0)
                {
                    setKeepOnAppending(true);
                } else
                {
                    setKeepOnAppending(false);
                }

                data.addAll(res);
                pendingView = null;
                notifyDataSetChanged();
            }

            public void onExecutionError(final Throwable error)
            {
                onDataRefreshFailed(error);
                setKeepOnAppending(true);
            }

            public void onExecutionAborted()
            {
                onDataRefreshEnded();
            }
        }.executeAsync();
    }

    protected View getPendingView(ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(getLoadingViewItemLayoutResource(), parent, false);
    }

    public void stopAppending()
    {
        setKeepOnAppending(false);
    }

    public void restartAppending()
    {
        setKeepOnAppending(true);
    }

    private void setKeepOnAppending(boolean newValue)
    {
        boolean same = (newValue == keepOnAppending.get());

        keepOnAppending.set(newValue);

        if (!same)
        {
            notifyDataSetChanged();
        }
    }

}
