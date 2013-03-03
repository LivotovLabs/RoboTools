package eu.livotov.labs.android.robotools.async;

import android.os.AsyncTask;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 03/03/2013
 */
public abstract class RTAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{

    public RTAsyncTask()
    {
    }

    public abstract Result performExecutionThread(final Params... parameters);

    public abstract void onExecutionStarted();

    public abstract void onExecutionFinished(final Result result);

    public abstract void onExecutionError(final Throwable error);

    public abstract void onExecutionAborted();

    public void abort()
    {
        cancel(true);
    }

    protected Result doInBackground(final Params... parameters)
    {
        try
        {
            final Result res = performExecutionThread(parameters);

            if (isCancelled())
            {
                return null;
            } else
            {
                return res;
            }
        } catch (Throwable err)
        {
            onExecutionError(err);
            return null;
        }
    }

    protected void onPreExecute()
    {
        onExecutionStarted();
    }

    protected void onCancelled(final Result result)
    {
        onExecutionAborted();
    }

    protected void onPostExecute(final Result result)
    {
        if (result != null)
        {
            onExecutionFinished(result);
        }
    }

    public void executeAsync(Params... params)
    {
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
    }
}
