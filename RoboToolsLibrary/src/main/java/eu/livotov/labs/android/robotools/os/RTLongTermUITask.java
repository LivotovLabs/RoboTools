package eu.livotov.labs.android.robotools.os;

/**
 * Created by dlivotov on 25/09/2015.
 */
public abstract class RTLongTermUITask extends RTAsyncTask
{
    boolean sentProgressEvents = true;
    boolean sendErrorEvent = true;
    boolean sendSuccessEvent = true;
    boolean serialExecution = false;
    boolean stickyDelivery = false;

    public abstract void onTaskExecutionThread();

    public abstract Object buildSuccessEvent();

    public abstract Object buildErrorEvent(Throwable err);

    public abstract Object buildProgressEvent(ProgressState state);

    protected abstract Object publishEvent(Object event, boolean stickyDelivery);

    public RTLongTermUITask withStickyDelivery()
    {
        stickyDelivery = true;
        return this;
    }

    public RTLongTermUITask withoutStickyDelivery()
    {
        stickyDelivery = false;
        return this;
    }

    public RTLongTermUITask withProgressEvents()
    {
        sentProgressEvents = true;
        return this;
    }

    public RTLongTermUITask withoutProgressEvents()
    {
        sentProgressEvents = false;
        return this;
    }

    public RTLongTermUITask withErrorEvent()
    {
        sendErrorEvent = false;
        return this;
    }

    public RTLongTermUITask withoutErrorEvent()
    {
        sendErrorEvent = false;
        return this;
    }

    public RTLongTermUITask withSuccessEvent()
    {
        sendSuccessEvent = true;
        return this;
    }

    public RTLongTermUITask withoutSuccessEvent()
    {
        sendSuccessEvent = false;
        return this;
    }

    public RTLongTermUITask withSerialExecution()
    {
        serialExecution = true;
        return this;
    }

    public RTLongTermUITask withAsyncExecution()
    {
        serialExecution = false;
        return this;
    }

    public void start()
    {
        if (serialExecution)
        {
            execSerial();
        }
        else
        {
            execPool();
        }
    }

    @Override
    protected void onPreExecute()
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressState.Start), false);
        }
    }

    @Override
    protected void onProgressUpdate()
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressState.Update), false);
        }
    }

    @Override
    protected void onPostExecute()
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressState.Stop), false);
        }

        if (sendSuccessEvent)
        {
            publishEvent(buildSuccessEvent(), stickyDelivery);
        }
    }

    @Override
    protected void onError(Throwable t)
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressState.Stop), false);
        }

        if (sendErrorEvent)
        {
            publishEvent(buildErrorEvent(t), stickyDelivery);
        }
    }

    @Override
    protected void onCanceled()
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressState.Stop), false);
        }
    }

    @Override
    protected void doInBackground() throws Throwable
    {
        onTaskExecutionThread();
    }

    public enum ProgressState
    {
        Start, Update, Stop
    }
}
