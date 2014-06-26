package eu.livotov.labs.android.robotools.content;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import eu.livotov.labs.android.robotools.os.Runloop;

import java.io.IOException;
import java.util.concurrent.CancellationException;

/**
 * This class handles request queue and watches for events taking place in it.
 */
@SuppressWarnings("unused")
public class RequestQueue {
    private static final int ATTEMPTS_COUNT = 3;
    private static RequestQueue sInstance;
    private final ConnectivityManager mConnectivityManager;

    private Runloop mRunloop;

    private RequestQueue(Context context) {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static RequestQueue with(Context context) {
        if(context == null) {
            throw new NullPointerException("Context must be not null");
        }
        if(sInstance == null) {
            sInstance = new RequestQueue(context);
        }
        return sInstance;
    }

    boolean isNetworkAvailable() {
        final NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        return !(networkInfo == null || !networkInfo.isConnectedOrConnecting());
    }

    void exec(Runnable executor, long delay) {
        if (mRunloop == null) {
            mRunloop = new Runloop();
            mRunloop.start();
        }
        mRunloop.post(executor, delay);
    }

    void exec(Runnable executor) {
        exec(executor, 0);
    }

    public <T> void exec(final RestRequest<T> request, final Callback<T> callback, long delay) {
        exec(new RequestExecutor<T>(request, callback), delay);
    }

    public <T> void exec(final RestRequest<T> request, final Callback<? super T> callback) {
        exec(new RequestExecutor<T>(request, callback), 0);
    }

    public <T> void exec(final Codeblock<T> request, final Callback<T> callback, long delay) {
        exec(new CodeblockExecutor<T>(request, callback), delay);
    }

    public <T> void exec(final Codeblock<T> request, final Callback<? super T> callback) {
        exec(new CodeblockExecutor<T>(request, callback), 0);
    }

    /**
     * This class implements the transfer of data from a background thread to the main.
     * Also it's can process validation, authorization, and captcha errors.
     */
    class CallbackHandler<T> extends Handler {

        final static int MESSAGE_ADDED = 0;
        final static int MESSAGE_PRE_EXECUTE = 1;
        final static int MESSAGE_RESULT = 2;
        final static int MESSAGE_ERROR = 3;
        final static int MESSAGE_POST_EXECUTE = 4;

        private final eu.livotov.labs.android.robotools.content.Callback<? super T> mCallback;

        public CallbackHandler(eu.livotov.labs.android.robotools.content.Callback<? super T> callback, Looper looper) {
            super(looper);
            mCallback = callback;
        }

        void dispatchAdded() {
            sendMessage(obtainMessage(MESSAGE_ADDED));
        }

        void dispatchPreExecute() {
            sendMessage(obtainMessage(MESSAGE_PRE_EXECUTE));
        }

        void dispatchPostExecute() {
            sendMessage(obtainMessage(MESSAGE_POST_EXECUTE));
        }

        void dispatchResult(T result) {
            Message message = obtainMessage(MESSAGE_RESULT);
            message.obj = result;
            sendMessage(message);
        }

        void dispatchError(Throwable e) {
            Message message = obtainMessage(MESSAGE_ERROR);
            message.obj = e;
            sendMessage(message);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MESSAGE_ADDED: {
                        mCallback.onAdded(RequestQueue.this);
                    } break;
                    case MESSAGE_PRE_EXECUTE: {
                        mCallback.onPreExecute(RequestQueue.this);
                    } break;
                    case MESSAGE_RESULT: {
                        mCallback.onSuccess(RequestQueue.this, (T) msg.obj);
                    } break;
                    case MESSAGE_ERROR: {
                        mCallback.onError(RequestQueue.this, (Throwable) msg.obj);
                    } break;
                    case MESSAGE_POST_EXECUTE: {
                        mCallback.onPostExecute(RequestQueue.this);
                    } break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles asynchronous operation of usual requests.
     */
    class RequestExecutor<T> implements Runnable {

        final CallbackHandler<T> mHandler;
        final Callback<? super T> mCallback;
        final RestRequest<T> mRequest;

        RequestExecutor(RestRequest<T> request, Callback<? super T> callback) {
            Looper looper = Looper.myLooper();
            if(looper == null) {
                Looper.prepare();
                looper = Looper.myLooper();
            }
            mCallback = callback;
            mRequest = request;
            mHandler = new CallbackHandler<T>(callback, looper) {
                @Override
                public void handleMessage(Message msg) {
                    if(!mRequest.isCanceled()) {
                        super.handleMessage(msg);
                    }
                }
            };
            mHandler.dispatchAdded();
        }

        @Override
        public void run() {
            Throwable error = null;
            mHandler.dispatchPreExecute();
            for(int i = 0; i < ATTEMPTS_COUNT; i++) {
                if(isNetworkAvailable()) {
                    if(!mRequest.isCanceled()) {
                        try {
                            mHandler.dispatchResult(mRequest.execute());
                            mHandler.dispatchPostExecute();
                            return;
                        } catch (IOException ignored) {
                            ignored.printStackTrace();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            error = e;
                        }
                    } else {
                        error = new CancellationException();
                    }
                }
            }
            if(error == null) {
                error = new IOException("Network error");
            }
            mHandler.dispatchError(error);
            mHandler.dispatchPostExecute();
        }

    }

    /**
     * Handles asynchronous operation of codeblocks.
     */
    class CodeblockExecutor<T> implements Runnable {

        final CallbackHandler<T> mHandler;
        final Callback<? super T> mCallback;
        final Codeblock<T> mCodeblock;

        CodeblockExecutor(Codeblock<T> codeblock, Callback<? super T> callback) {
            Looper looper = Looper.myLooper();
            if(looper == null) {
                Looper.prepare();
                looper = Looper.myLooper();
            }
            mCallback = callback;
            mCodeblock = codeblock;
            mHandler = new CallbackHandler<T>(callback, looper) {
                @Override
                public void handleMessage(Message msg) {
                    if(!mCodeblock.isCanceled()) {
                        super.handleMessage(msg);
                    }
                }
            };
            mHandler.dispatchAdded();
        }

        @Override
        public void run() {
            Throwable error = null;
            mHandler.dispatchPreExecute();
            for(int i = 0; i < ATTEMPTS_COUNT; i++) {
                if(isNetworkAvailable()) {
                    if(!mCodeblock.isCanceled()) {
                        try {
                            mHandler.dispatchResult(mCodeblock.run());
                            mHandler.dispatchPostExecute();
                            return;
                        } catch (IOException ignored) {
                            ignored.printStackTrace();
                        } catch (Throwable e) {
                            error = e;
                        }
                    } else {
                        error = new CancellationException();
                    }
                }
            }
            if(error == null) {
                error = new IOException("Network error");
            }
            mHandler.dispatchError(error);
            mHandler.dispatchPostExecute();
        }

    }

}