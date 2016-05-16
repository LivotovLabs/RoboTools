package eu.livotov.labs.android.robotools.os;

import android.os.Looper;
import android.os.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AsyncTask reincarnation with the better and simplier API, written from scratch and not depending on any android api
 */
public abstract class RTAsyncTask
{

    /**
     * Device CPU/Cores count
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * Initial thread pool size, computed from the device CPU/Cores count
     */
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    /**
     * Max thread pool size, computed from the device CPU/Cores count
     */
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    /**
     * Default keep-alive timeout
     */
    private static final int KEEP_ALIVE = 1;

    /**
     * Time to put on hold parallel executor rejected task (rejected due to executor over-capacity) before submit it again for execution
     */
    private static final long REJECTED_TASK_RESUBMIT_TIMEOUT_MS = 1000;

    private static final BlockingQueue<Runnable> asyncExecutionPool = new LinkedBlockingQueue<Runnable>(128);
    private final static RTRunloop serialExecutionPool = new RTRunloop();
    private static final Handler handler = new Handler();
    private final Object lock = new Object();
    private AtomicBoolean taskCancelledFlag = new AtomicBoolean(false);

    private AsyncResult taskResult = new AsyncResult(this);

    private static final ThreadFactory threadFactory = new ThreadFactory()
    {
        private final AtomicInteger count = new AtomicInteger(1);

        public Thread newThread(Runnable r)
        {
            return new Thread(r, "RTAsyncTask #" + count.getAndIncrement());
        }
    };

    public static final Executor threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, asyncExecutionPool, threadFactory, new RejectedExecutionHandler()
    {
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor)
        {
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    executor.execute(r);
                }
            }, REJECTED_TASK_RESUBMIT_TIMEOUT_MS);
        }
    });

    /**
     * Called in task creator thread before the async body is executed
     */
    protected void onPreExecute()
    {

    }

    /**
     * Called in task creator thread when async task body provides some progress info about the process being executed.
     * <p>In comparison to old AsyncTask, here we don't pass any progress data. Simply compute actual progress of your RTAsyncClass implementation and publish it wherewer you need (UI, event, etc)</p>
     */
    protected void onProgressUpdate()
    {

    }

    /**
     * Called in task creator thread when the task successfully completes
     * <p>In comparison to old AsyncTask we don't put any result here. Simply grab result from your current RTAsyncTask implementation fields and do whatever you need with it.</p>
     */
    protected void onPostExecute()
    {

    }

    /**
     * Called in task creator thread when any uncaught exception happens in the {@link #doInBackground()} method during its execution
     *
     * @param t uncaught exception, happened in {@link #doInBackground()} method
     */
    protected void onError(Throwable t)
    {

    }

    /**
     * Called in task creator thread when the task was cancelled and task's async process finished its execution
     */
    protected void onCanceled()
    {

    }

    /**
     * Use this method to send a progress updated event from your async thread body ( {@link #doInBackground()} method). Calling publishProgress() will cause
     * {@link #onProgressUpdate()} method to be executed in the task creator thread.
     */
    public void publishProgress()
    {
        if (!taskCancelledFlag.get())
        {
            handler.dispatchProgressUpdate(taskResult);
        }
    }

    /**
     * Request the task to cancel. Note, this does not mean immediate cancellation.
     * Task will be cancelled if the task async body in {@link #doInBackground()} method will check cancellation flag and respond accordingly.
     */
    public void cancel()
    {
        taskCancelledFlag.set(true);
    }

    /**
     * Check the cancellation request for this task. It is {@link #doInBackground()} code responsibility to check cancellation status from
     * time to time and exit the method ahead of schedule when the task is marked as cancelled.
     *
     * @return true, if task was requested to be cancelled by someone.
     */
    public boolean isCanceled()
    {
        return taskCancelledFlag.get();
    }

    /**
     * Starts this task execution asynchronously.
     * <p/>
     * All tasks started by this method are run in parallel on a thread pool, until pool capacity is exhausted. All new tasks over the pool capacity will be put on hold and
     * executed as soon as pool becomes free.
     */
    public void execPool()
    {
        threadPoolExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                execInCurrThread();
            }
        });
    }

    /**
     * Starts this task in serial execution mode.
     * <p/>
     * Tasks, started by this method are put into a separate queue and executed one by one in a serial manner.
     */
    public void execSerial()
    {
        if (!serialExecutionPool.isStarted())
        {
            serialExecutionPool.start();
        }
        serialExecutionPool.post(new Runnable()
        {
            @Override
            public void run()
            {
                execInCurrThread();
            }
        });
    }

    /**
     * Starts this task in the current (calling) thread.
     */
    public void execInCurrThread()
    {
        synchronized (lock)
        {
            if (!taskCancelledFlag.get())
            {
                handler.dispatchPreExecute(taskResult);
                if (!taskCancelledFlag.get())
                {
                    try
                    {
                        doInBackground();

                        if (!taskCancelledFlag.get())
                        {
                            handler.dispatchPostExecute(taskResult);
                        }
                        else
                        {
                            handler.dispatchCancel(taskResult);
                        }
                    }
                    catch (Throwable throwable)
                    {
                        if (!taskCancelledFlag.get())
                        {
                            taskResult.error = throwable;
                            handler.dispatchError(taskResult);
                        }
                        else
                        {
                            handler.dispatchCancel(taskResult);
                        }
                    }
                }
                else
                {
                    handler.dispatchCancel(taskResult);
                }
            }
            else
            {
                handler.dispatchCancel(taskResult);
            }
        }
    }

    /**
     * Main task body. All long-term background operation must be performed here
     *
     * @throws java.lang.Throwable in any error situation. This will be automatically handled and routed to {@link #onError(Throwable)} method.
     */
    protected abstract void doInBackground() throws Throwable;


    static class Handler extends android.os.Handler
    {

        static final int MESSAGE_PRE_EXECUTE = 1;
        static final int MESSAGE_PROGRESS = 2;
        static final int MESSAGE_ERROR = 3;
        static final int MESSAGE_CANCEL = 4;
        static final int MESSAGE_POST_EXECUTE = 5;

        Handler()
        {
            super(Looper.getMainLooper());
        }

        void dispatchPreExecute(AsyncResult result)
        {
            sendMessage(result, MESSAGE_PRE_EXECUTE);
        }

        void sendMessage(AsyncResult result, int code)
        {
            Message m = obtainMessage(code);
            m.obj = result;
            m.sendToTarget();
        }

        void dispatchProgressUpdate(AsyncResult result)
        {
            sendMessage(result, MESSAGE_PROGRESS);
        }

        void dispatchError(AsyncResult result)
        {
            sendMessage(result, MESSAGE_ERROR);
        }

        void dispatchCancel(AsyncResult result)
        {
            sendMessage(result, MESSAGE_CANCEL);
        }

        void dispatchPostExecute(AsyncResult result)
        {
            sendMessage(result, MESSAGE_POST_EXECUTE);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg)
        {
            AsyncResult obj = (AsyncResult) msg.obj;
            if (obj != null)
            {
                switch (msg.what)
                {
                    case MESSAGE_PRE_EXECUTE:
                        obj.task.onPreExecute();
                        break;
                    case MESSAGE_PROGRESS:
                        obj.task.onProgressUpdate();
                        break;
                    case MESSAGE_ERROR:
                        obj.task.onError(obj.error);
                        break;
                    case MESSAGE_CANCEL:
                        obj.task.onCanceled();
                        break;
                    case MESSAGE_POST_EXECUTE:
                        obj.task.onPostExecute();
                        break;
                }
            }
        }
    }

    static class AsyncResult
    {

        final RTAsyncTask task;
        Throwable error;

        AsyncResult(RTAsyncTask task)
        {
            this.task = task;
        }
    }
}
