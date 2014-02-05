package eu.livotov.labs.android.robotools.ui.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.internal.v;
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
    private View tapToLoadView = null;
    private AtomicBoolean keepOnAppending = new AtomicBoolean(false);
    private AtomicBoolean appendingInProgress = new AtomicBoolean(false);


    public RTEndlessListAdapter(Context ctx)
    {
        super(ctx);
    }

    abstract protected int getLoadingViewItemLayoutResource();

    abstract protected int getTapToLoadViewItemLayoutResource();

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
            }

            if (tapToLoadView == null && getTapToLoadViewItemLayoutResource() > 0)
            {
                tapToLoadView = getTapToLoadView(parent);
            }

            if (getTapToLoadViewItemLayoutResource() <= 0)
            {
                executeAsyncTask();
                return pendingView;
            } else
            {
                if (appendingInProgress.get())
                {
                    return pendingView;
                } else
                {
                    return tapToLoadView;
                }
            }
        }

        return super.getView(position, convertView, parent);
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
                setKeepOnAppending(false);
                onDataRefreshStarted();
            }

            public void onExecutionFinished(final Collection<T> ts)
            {
                data.clear();
                if (ts.size() > 0)
                {
                    setKeepOnAppending(true);
                }
                addData(ts);
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

    private void executeAsyncTask()
    {
        if (appendingInProgress.get())
        {
            return;
        }

        final int realCount = super.getCount();

        new RTAsyncTask<Void, Void, Collection<T>>()
        {

            public Collection<T> performExecutionThread(final Void... parameters)
            {
                if (realCount == 0)
                {
                    return loadDataInBackgroundThread();
                } else
                {
                    return loadEndlessBatchInBackground(realCount);
                }
            }

            public void onExecutionStarted()
            {
                appendingInProgress.set(true);
                onDataRefreshStarted();
            }

            public void onExecutionFinished(final Collection<T> res)
            {
                appendingInProgress.set(false);
                onDataRefreshEnded();
                setKeepOnAppending(showAppendingAfterLoad(res));
                pendingView = null;
                addData(res);
            }

            public void onExecutionFailed(final Throwable error)
            {
                appendingInProgress.set(false);
                onDataRefreshFailed(error);
                setKeepOnAppending(false);
            }

            public void onExecutionAborted()
            {
                appendingInProgress.set(false);
                onDataRefreshEnded();
                setKeepOnAppending(false);
            }
        }.executeAsync();
    }

    protected void addData(Collection<T> res)
    {
        data.addAll(res);
        notifyDataSetChanged();
    }

    protected boolean showAppendingAfterLoad(Collection<T> res)
    {
        return res.size() > 0;
    }

    protected View getPendingView(ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(getLoadingViewItemLayoutResource(), parent, false);
    }

    protected View getTapToLoadView(ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(getTapToLoadViewItemLayoutResource(), parent, false);
        view.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(final View v)
            {
                executeAsyncTask();
                notifyDataSetChanged();
            }
        });

        return view;
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

    public boolean isKeepOnAppending()
    {
        return keepOnAppending.get();
    }
}
