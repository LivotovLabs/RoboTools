package eu.livotov.labs.android.robotools.os;

/**
 * Created by dlivotov on 25/09/2015.
 */
public abstract class RTLongTermUITask extends RTAsyncTask
{
    private boolean sentProgressEvents = true;
    private boolean sendErrorEvent = true;
    private boolean sendSuccessEvent = true;
    private boolean serialExecution = false;
    private boolean stickyDelivery = false;

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
            publishEvent(buildProgressEvent(ProgressUpdateType.OperationStart), false);
        }
    }

    /**
     * Publish provided event class via your event delivery subsystem you use in your app.
     *
     * @param event          event class to publish. This is the same class that was created by {@link #buildProgressEvent(ProgressUpdateType)}, {@link #buildErrorEvent(Throwable)} or {@link #buildSuccessEvent()} method
     * @param stickyDelivery sticky flag to indicate this event must be delivered also for recipients, currently not connected to your event subsystem.
     */
    protected abstract void publishEvent(Object event, boolean stickyDelivery);

    /**
     * Create event payload class to represent progress event
     *
     * @param state progress event type
     * @return progress status event class, which will be sent to subscribers
     */
    public abstract Object buildProgressEvent(ProgressUpdateType state);

    @Override
    protected void onProgressUpdate()
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressUpdateType.OperationProgressUpdate), false);
        }
    }

    @Override
    protected void onPostExecute()
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressUpdateType.OperationStopped), false);
        }

        if (sendSuccessEvent)
        {
            publishEvent(buildSuccessEvent(), stickyDelivery);
        }
    }

    /**
     * Create event payload class for task successfull execution
     *
     * @return successfull execution event class, which will be sent to subscribers
     */
    public abstract Object buildSuccessEvent();

    @Override
    protected void onError(Throwable t)
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressUpdateType.OperationStopped), false);
        }

        if (sendErrorEvent)
        {
            publishEvent(buildErrorEvent(t), stickyDelivery);
        }
    }

    /**
     * Create event payload class to represent task execution error
     *
     * @param err execution exception
     * @return failed execution event class, which will be sent to subscribers
     */
    public abstract Object buildErrorEvent(Throwable err);

    @Override
    protected void onCanceled()
    {
        if (sentProgressEvents)
        {
            publishEvent(buildProgressEvent(ProgressUpdateType.OperationStopped), false);
        }
    }

    @Override
    protected void doInBackground() throws Throwable
    {
        onTaskExecutionThread();
    }

    /**
     * Place your async code here. This method will be executed in a separate non-ui thread
     */
    public abstract void onTaskExecutionThread();

    public enum ProgressUpdateType
    {
        OperationStart, OperationProgressUpdate, OperationStopped
    }
}
