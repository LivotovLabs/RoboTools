package eu.livotov.labs.android.robotools.compat.v1.async;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 03/03/2013
 */
public abstract class RTAsyncTask<Params, Progress, Result> extends RTBaseAsyncTask<Params, Progress, Result>
{

    private Throwable error;

    public RTAsyncTask()
    {
    }

    public abstract Result performExecutionThread(final Params... parameters) throws Exception;

    public abstract void onExecutionStarted();

    public abstract void onExecutionFinished(final Result result);

    public abstract void onExecutionFailed(final Throwable error);

    public abstract void onExecutionAborted();

    public void abort()
    {
        cancel(true);
    }

    protected Result doInBackground(final Params... parameters)
    {
        try
        {
            Thread.sleep(100);
            error = null;
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
            error = err;
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
        if (error != null)
        {
            onExecutionFailed(error);
        } else
        {
            onExecutionFinished(result);
        }
    }
}
