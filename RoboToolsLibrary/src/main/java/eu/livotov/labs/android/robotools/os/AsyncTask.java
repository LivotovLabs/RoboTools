package eu.livotov.labs.android.robotools.os;

import android.os.Looper;
import android.os.Message;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Улучшенная версия AsyncTask.
 *
 * Более быстрая работа, поддержка нескольких видов исполнения и отловаисключений.
 *
 * @param <Params> тип входных аргументов.
 * @param <Progress> тип промежуточных результатов.
 * @param <Result> тип возвращаемого значения.
 */
@SuppressWarnings("unused")
public abstract class AsyncTask<Params, Progress, Result> {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);
    private final static Runloop sRunLoop = new Runloop();

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    public static final Executor sPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    private static final Handler sHandler = new Handler();
    private final Object mLock = new Object();
    private AtomicBoolean mCancelled = new AtomicBoolean(false);

    /**
     * Вызывается перед выполнением задачи в главном потоке.
     */
    protected void onPreExecute() {

    }

    /**
     * Выполняется ассихронно операции в рабочем потоке.
     * Здесь вы должны выполнить все ассинхронную работу.
     * @param args переданные при исполнении аргументы.
     * @throws java.lang.Throwable при любой исключительной ситуации. Обработать его вы сможете в {@link #onError(Throwable)}.
     */
    protected abstract Result doInBackground(Params... args) throws Throwable;

    /**
     * Вызывается, когда задача публикует прогресс из рабочего потока.
     * Выполняется в главном потоке.
     * @param progress объект с информацией о прогрессе, переданный в {@link #publishProgress(Progress)}.
     */
    protected void onProgressUpdate(Progress progress) {

    }

    /**
     * Вызывается в главном потоке при успешном завершении задачи.
     * @param result результат, возвращенный методом {@link #doInBackground(Params[])}.
     */
    protected void onPostExecute(Result result) {

    }

    /**
     * Вызывается в главном потоке при возникновении исключительной ситуации в фоновом потоке.
     * @param t исключение, брошенное методом {@link #doInBackground(Params[])}.
     */
    protected void onError(Throwable t) {

    }

    /**
     * Вызывается в главном потоке при отмене задачи в процессе ее выполнения.
     * @param result результат, возвращенный методом {@link #doInBackground(Params[])}.
     */
    protected void onCanceled(Result result) {

    }

    /**
     * Отправляет в главный поток информацию о прогрессе задачи.
     * @param progress значение прогресса.
     */
    public void publishProgress(Progress progress) {
        if(!mCancelled.get()) {
            AsyncResult<Progress, Result> result = new AsyncResult<Progress, Result>(this);
            result.progress = progress;
            sHandler.dispatchProgressUpdate(result);
        }
    }

    /**
     * Отмечает задачу отмененной.
     *
     * Если задача отменена, перестают вызываться любые статус-методы, кроме {@link #onCanceled(Result)}.
     */
    public void cancel() {
        mCancelled.set(true);
    }

    /**
     * Возвращает статус задачи.
     * @return true, если задача была отменена, false во всех остальных случаях.
     */
    public boolean isCanceled() {
        return mCancelled.get();
    }

    /**
     * Выполняет задачу ассинхронно.
     *
     * Задачи, отправленные на выполнение этим методом,
     * помещаются в пул потоком и управляются уже его логикой.
     *
     * @param params аргументы для {@link #doInBackground(Params[])}.
     */
    public void execPool(final Params... params) {
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                execInCurrThread(params);
            }
        });
    }

    /**
     * Выполняет задачу ассинхронно.
     *
     * Задачи, отправленные на выполнение этим методом, помещаются в очередь и
     * выполняются единственным потоком по мере их поступления из очереди.
     *
     * @param params аргументы для {@link #doInBackground(Params[])}.
     */
    public void execSerial(final Params... params) {
        if(!sRunLoop.isStarted()) {
            sRunLoop.start();
        }
        sRunLoop.post(new Runnable() {
            @Override
            public void run() {
                execInCurrThread(params);
            }
        });
    }

    /**
     * Выполняет задачу в текущем потоке.
     * @param params аргументы для {@link #doInBackground(Params[])}.
     */
    public void execInCurrThread(Params... params) {
        synchronized (mLock) {
            AsyncResult<Progress, Result> result = new AsyncResult<Progress, Result>(this);
            if(!mCancelled.get()) {
                sHandler.dispatchPreExecute(result);
                if(!mCancelled.get()) {
                    try {
                        result.result = doInBackground(params);
                        if(!mCancelled.get()) {
                            sHandler.dispatchPostExecute(result);
                        } else {
                            sHandler.dispatchCancel(result);
                        }
                    } catch (Throwable throwable) {
                        if(!mCancelled.get()) {
                            result.t = throwable;
                            sHandler.dispatchError(result);
                        } else {
                            sHandler.dispatchCancel(result);
                        }
                    }
                } else {
                    sHandler.dispatchCancel(result);
                }
            } else {
                sHandler.dispatchCancel(result);
            }
        }
    }

    static class Handler extends android.os.Handler {

        static final int MESSAGE_PRE_EXECUTE = 1;
        static final int MESSAGE_PROGRESS = 2;
        static final int MESSAGE_ERROR = 3;
        static final int MESSAGE_CANCEL = 4;
        static final int MESSAGE_POST_EXECUTE = 5;

        Handler() {
            super(Looper.getMainLooper());
        }

        void dispatchPreExecute(AsyncResult result) {
            sendMessage(result, MESSAGE_PRE_EXECUTE);
        }

        void dispatchProgressUpdate(AsyncResult result) {
            sendMessage(result, MESSAGE_PROGRESS);
        }

        void dispatchError(AsyncResult result) {
            sendMessage(result, MESSAGE_ERROR);
        }

        void dispatchCancel(AsyncResult result) {
            sendMessage(result, MESSAGE_CANCEL);
        }

        void dispatchPostExecute(AsyncResult result) {
            sendMessage(result, MESSAGE_POST_EXECUTE);
        }

        void sendMessage(AsyncResult result, int code) {
            Message m = obtainMessage(code);
            m.obj = result;
            m.sendToTarget();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            AsyncResult obj = (AsyncResult) msg.obj;
            if(obj != null) {
                switch (msg.what) {
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

    static class AsyncResult<Progress, Result> {

        final AsyncTask task;

        Throwable t;
        Progress progress;
        Result result;

        AsyncResult(AsyncTask task) {
            this.task = task;
        }
    }
}
