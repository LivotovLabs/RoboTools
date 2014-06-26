package eu.livotov.labs.android.robotools.content;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * This receiver will receive all requests events, errors and results if
 * host {@link android.app.Activity} can be
 * recreated during the process.
 *
 * You should create a child of this class in your {@link android.app.Activity} or {@link android.app.Fragment},
 * and register its instance on {@code onStart()} and {@code onStop()} methods.
 * Then you can run requests using {@link ReceiverCallback}.
 *
 * @param <T> <T> type of request result. Must support {@link android.os.Parcelable} or {@link java.io.Serializable} interfaces.
 */
public class RestReceiver<T> extends android.content.BroadcastReceiver {

    final static String EXTRA_CODE = "extra.code";
    final static String EXTRA_ERROR = "extra.error";
    final static String EXTRA_RESULT = "extra.result";
    final static String EXTRA_TARGET = "extra.target";
    final static String ACTION_ADD = "action.add";
    final static String ACTION_PRE_EXECUTE = "action.pre_execute";
    final static String ACTION_POST_EXECUTE = "action.post_execute";
    final static String ACTION_SUCCESS = "action.success";
    final static String ACTION_ERROR = "action.error";

    /**
     * Universal filter that you can use for every child of this class.
     */
    public final static IntentFilter FILTER = new IntentFilter();

    static {
        FILTER.addAction(ACTION_ADD);
        FILTER.addAction(ACTION_PRE_EXECUTE);
        FILTER.addAction(ACTION_SUCCESS);
        FILTER.addAction(ACTION_ERROR);
        FILTER.addAction(ACTION_POST_EXECUTE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String target = intent.getStringExtra(EXTRA_TARGET);
        int code = intent.getIntExtra(EXTRA_CODE, Integer.MIN_VALUE);
        RequestQueue queue = RequestQueue.with(context);
        if(getClass().getCanonicalName().equals(target)) {
            if(ACTION_ADD.equals(action)) {
                onAdded(queue, code);
            } else if(ACTION_PRE_EXECUTE.equals(action)) {
                onPreExecute(queue, code);
            } else if(ACTION_SUCCESS.equals(action)) {
                T result = (T) intent.getParcelableExtra(EXTRA_RESULT);
                if(result == null) {
                    result = (T) intent.getSerializableExtra(EXTRA_RESULT);
                }
                onSuccess(queue, code, result);
            } else if(ACTION_ERROR.equals(action)) {
                onError(queue, code, (Throwable) intent.getSerializableExtra(EXTRA_ERROR));
            } else if(ACTION_POST_EXECUTE.equals(action)) {
                onPostExecute(queue, code);
            }
        }
    }

    /**
     * Called when request is added fo requests queue.
     * @param requestCode unique code of request to identify.
     */
    public void onAdded(RequestQueue queue, int requestCode) {

    }

    /**
     * Called before the request has been executed.
     * @param requestCode unique code of request to identify.
     */
    public void onPreExecute(RequestQueue queue, int requestCode) {

    }

    /**
     * Called if request was successful and without errors.
     * @param requestCode unique code of request to identify.
     * @param data result of this response.
     */
    public void onSuccess(RequestQueue queue, int requestCode, T data) {

    }

    /**
     * Called if the server returned an error code.
     * @param requestCode unique code of request to identify.
     * @param t server's error exception.
     */
    public void onError(RequestQueue queue, int requestCode, Throwable t) {

    }

    /**
     * Called after the request has been executed.
     * @param requestCode unique code of request to identify.
     */
    public void onPostExecute(RequestQueue queue, int requestCode) {

    }
}

