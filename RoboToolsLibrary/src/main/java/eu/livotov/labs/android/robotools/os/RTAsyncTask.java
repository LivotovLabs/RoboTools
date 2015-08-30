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
 * Better AsyncTask clone, written from scratch and not depending on android api
 *
 * @param <Params>   Input args type
 * @param <Progress> Progress data type
 * @param <Result>   Return data type
 */
@SuppressWarnings("unused")
public abstract class RTAsyncTask<Params, Progress, Result>
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
    private static final long REJECTED_TASK_RESUBMIT_TIMEOUT = 1000;

    private static final BlockingQueue<Runnable> asyncExecutionPool = new LinkedBlockingQueue<Runnable>(128);
    private final static RTRunloop serialExecutionPool = new RTRunloop();
    private static final Handler sHandler = new Handler();
    private final Object mLock = new Object();
    private AtomicBoolean mCancelled = new AtomicBoolean(false);

    private static final ThreadFactory sThreadFactory = new ThreadFactory()
    {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r)
        {
            return new Thread(r, "RTAsyncTask #" + mCount.getAndIncrement());
        }
    };

    public static final Executor sPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, asyncExecutionPool, sThreadFactory, new RejectedExecutionHandler()
    {
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor)
        {
            sHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    executor.execute(r);
                }
            }, REJECTED_TASK_RESUBMIT_TIMEOUT);
        }
    });

    /**
     * Called in main thread before the async task body is executed
     */
    protected void onPreExecute()
    {

    }

    /**
     * Called in main thread to publish progress update
     *
     * @param progress current progress value which was passed by main task body via the {@link #publishProgress(Progress)} method.
     */
    protected void onProgressUpdate(Progress progress)
    {

    }

    /**
     * Called in main thread when the task successfully completes
     *
     * @param result task result, returned by the {@link #doInBackground(Params[])} method.
     */
    protected void onPostExecute(Result result)
    {

    }

    /**
     * Called in main thread when any uncaught exception happens in the {@link #doInBackground(Params[])} method during its execution
     *
     * @param t uncaught exception, happened in {@link #doInBackground(Params[])} method
     */
    protected void onError(Throwable t)
    {

    }

    /**
     * Called in main thread when the task is cancelled
     *
     * @param result result, returned by {@link #doInBackground(Params[])}.
     */
    protected void onCanceled(Result result)
    {

    }

    /**
     * Use this method to publish a task execution progress update
     *
     * @param progress current progress state
     */
    public void publishProgress(Progress progress)
    {
        if (!mCancelled.get())
        {
            AsyncResult<Progress, Result> result = new AsyncResult<Progress, Result>(this);
            result.progress = progress;
            sHandler.dispatchProgressUpdate(result);
        }
    }

    /**
     * Cancels this task
     */
    public void cancel()
    {
        mCancelled.set(true);
    }

    /**
     * Check the cancellation status of the task. It is {@link #doInBackground(Params[])} code responsibility to check cancellation status from
     * time to time and abort long execution when task is marked as cancelled.
     *
     * @return true, if task was requested to be cancelled by someone.
     */
    public boolean isCanceled()
    {
        return mCancelled.get();
    }

    /**
     * Starts this task execution asynchronously.
     * <p/>
     * All tasks started by this method are run in parallel on a thread pool, until pool capacity is exhausted. All new tasks over the pool capacity will be put on hold and
     * executed as soon as pool becomes free.
     *
     * @param params task args for {@link #doInBackground(Params[])}.
     */
    public void execPool(final Params... params)
    {
        sPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                execInCurrThread(params);
            }
        });
    }

    /**
     * Starts this task in serial execution mode.
     * <p/>
     * Tasks, started by this method are put into a separate queue and executed one by one in a serial manner.
     *
     * @param params arguments for {@link #doInBackground(Params[])}.
     */
    public void execSerial(final Params... params)
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
                execInCurrThread(params);
            }
        });
    }

    /**
     * Starts this task in the current (calling) thread.
     *
     * @param params arguments for {@link #doInBackground(Params[])}.
     */
    public void execInCurrThread(Params... params)
    {
        synchronized (mLock)
        {
            AsyncResult<Progress, Result> result = new AsyncResult<Progress, Result>(this);
            if (!mCancelled.get())
            {
                sHandler.dispatchPreExecute(result);
                if (!mCancelled.get())
                {
                    try
                    {
                        result.result = doInBackground(params);
                        if (!mCancelled.get())
                        {
                            sHandler.dispatchPostExecute(result);
                        }
                        else
                        {
                            sHandler.dispatchCancel(result);
                        }
                    }
                    catch (Throwable throwable)
                    {
                        if (!mCancelled.get())
                        {
                            result.t = throwable;
                            sHandler.dispatchError(result);
                        }
                        else
                        {
                            sHandler.dispatchCancel(result);
                        }
                    }
                }
                else
                {
                    sHandler.dispatchCancel(result);
                }
            }
            else
            {
                sHandler.dispatchCancel(result);
            }
        }
    }

    /**
     * Main task body. All long-term background operation must be performed here
     *
     * @param args task arguments, passed by the execXXX method
     * @throws java.lang.Throwable in any error situation. This will be automatically handled and routed to {@link #onError(Throwable)} method.
     */
    protected abstract Result doInBackground(Params... args) throws Throwable;


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
                        obj.task.onProgressUpdate(obj.progress);
                        break;
                    case MESSAGE_ERROR:
                        obj.task.onError(obj.t);
                        break;
                    case MESSAGE_CANCEL:
                        obj.task.onCanceled(obj.result);
                        break;
                    case MESSAGE_POST_EXECUTE:
                        obj.task.onPostExecute(obj.result);
                        break;
                }
            }
        }
    }

    static class AsyncResult<Progress, Result>
    {

        final RTAsyncTask task;

        Throwable t;
        Progress progress;
        Result result;

        AsyncResult(RTAsyncTask task)
        {
            this.task = task;
        }
    }
}
