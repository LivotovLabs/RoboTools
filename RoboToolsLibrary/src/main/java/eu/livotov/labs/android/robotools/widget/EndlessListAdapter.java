package eu.livotov.labs.android.robotools.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.livotov.labs.android.robotools.os.AsyncTask;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EndlessListAdapter<T> extends ListAdapter<T> {

    private View pendingView = null;
    private View tapToLoadView = null;
    private AtomicBoolean keepOnAppending = new AtomicBoolean(false);
    private AtomicBoolean appendingInProgress = new AtomicBoolean(false);

    public EndlessListAdapter(Context ctx) {
        super(ctx);
    }

    abstract protected int getLoadingViewItemLayoutResource();

    abstract protected int getTapToLoadViewItemLayoutResource();

    abstract protected Collection<T> loadEndlessBatchInBackground(int currentPayloadItemsCount);

    abstract protected int getRealViewTypeCount();

    abstract protected int getRealItemViewType(int position);

    public T getItem(int position) {
        if (position >= super.getCount()) {
            return null;
        }
        return (super.getItem(position));
    }

    public int getCount() {
        if (keepOnAppending.get()) {
            return (super.getCount() + 1);
        }
        return (super.getCount());
    }

    public int getViewTypeCount() {
        return (getRealViewTypeCount() + 1);
    }

    public int getItemViewType(int position) {
        if (position == super.getCount()) {
            return (IGNORE_ITEM_VIEW_TYPE);
        }
        return (getRealItemViewType(position));
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return position < super.getCount() && super.isEnabled(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == super.getCount() && keepOnAppending.get()) {
            if (pendingView == null) {
                pendingView = getPendingView(parent);
            }

            if (tapToLoadView == null && getTapToLoadViewItemLayoutResource() > 0) {
                tapToLoadView = getTapToLoadView(parent);
            }

            if (getTapToLoadViewItemLayoutResource() <= 0) {
                executeAsyncTask();
                return pendingView;
            } else {
                if (appendingInProgress.get()) {
                    return pendingView;
                } else {
                    return tapToLoadView;
                }
            }
        }

        return super.getView(position, convertView, parent);
    }

    public void refresh() {
        new AsyncTask<Object, Object, Collection<T>>() {
            public Collection<T> doInBackground(final Object... parameters) {
                return loadDataInBackgroundThread();
            }

            @Override
            protected void onPreExecute() {
                if (showProgressAtFirstLoad()) {
                    setKeepOnAppending(true);
                    appendingInProgress.set(true);
                }
                onDataRefreshStarted();
            }

            @Override
            protected void onPostExecute(Collection<T> ts) {
                clear();
                if (size() > 0) {
                    setKeepOnAppending(true);
                }
                addAll(ts);
                appendingInProgress.set(false);
                onDataRefreshEnded();
            }

            @Override
            protected void onError(Throwable t) {
                appendingInProgress.set(false);
                onDataRefreshFailed(t);
            }

            @Override
            protected void onCanceled(Collection<T> ts) {
                appendingInProgress.set(false);
                onDataRefreshEnded();
            }
        }.execPool();
    }

    private void executeAsyncTask() {
        if (appendingInProgress.get()) {
            return;
        }

        final int realCount = super.getCount();

        new AsyncTask<Void, Void, Collection<T>>() {

            public Collection<T> doInBackground(final Void... parameters) {
                if (realCount == 0) {
                    return loadDataInBackgroundThread();
                } else {
                    return loadEndlessBatchInBackground(realCount);
                }
            }

            @Override
            protected void onPreExecute() {
                appendingInProgress.set(true);
                onDataRefreshStarted();
            }

            @Override
            protected void onPostExecute(Collection<T> res) {
                appendingInProgress.set(false);
                onDataRefreshEnded();
                setKeepOnAppending(showAppendingAfterLoad(res));
                pendingView = null;
                addAll(res);
            }

            @Override
            protected void onError(Throwable t) {
                appendingInProgress.set(false);
                onDataRefreshFailed(t);
                setKeepOnAppending(false);
            }

            @Override
            protected void onCanceled(Collection<T> ts) {
                appendingInProgress.set(false);
                onDataRefreshEnded();
                setKeepOnAppending(false);
            }

        }.execPool();
    }

    public boolean showProgressAtFirstLoad() {
        return false;
    }

    protected boolean showAppendingAfterLoad(Collection<T> res) {
        return res.size() > 0;
    }

    protected View getPendingView(ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(getLoadingViewItemLayoutResource(), parent, false);
    }

    protected View getTapToLoadView(ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(getTapToLoadViewItemLayoutResource(), parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                executeAsyncTask();
                notifyDataSetChanged();
            }
        });

        return view;
    }

    private void setKeepOnAppending(boolean newValue) {
        boolean same = (newValue == keepOnAppending.get());

        keepOnAppending.set(newValue);

        if (!same) {
            notifyDataSetChanged();
        }
    }

    public boolean isKeepOnAppending() {
        return keepOnAppending.get();
    }
}
