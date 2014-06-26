package eu.livotov.labs.android.robotools.content;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * This callback will be useful if host {@link android.app.Activity} can be recreated during the process.
 * All events will be sent to given Receiver class.
 * @param <T> type of request result. Must support {@link android.os.Parcelable} or {@link java.io.Serializable} interfaces.
 */
public class ReceiverCallback<T> implements Callback<T> {

    private final String mClazz;
    private final int mCode;
    private final Context mContext;

    /**
     * Creates new Receiver callback
     * @param receiver receiver class to handle events.
     * @param requestCode unique code to identify requests.
     */
    public ReceiverCallback(Context context, Class<? extends RestReceiver<T>> receiver, int requestCode) {
        mClazz = receiver.getCanonicalName();
        mCode = requestCode;
        mContext = context;
    }

    @Override
    public final void onAdded(RequestQueue queue) {
        Intent intent = new Intent();
        intent.setAction(RestReceiver.ACTION_ADD);
        intent.putExtra(RestReceiver.EXTRA_TARGET, mClazz);
        intent.putExtra(RestReceiver.EXTRA_CODE, mCode);
        mContext.sendBroadcast(intent);
    }

    @Override
    public final void onPreExecute(RequestQueue queue) {
        Intent intent = new Intent();
        intent.setAction(RestReceiver.ACTION_PRE_EXECUTE);
        intent.putExtra(RestReceiver.EXTRA_TARGET, mClazz);
        intent.putExtra(RestReceiver.EXTRA_CODE, mCode);
        mContext.sendBroadcast(intent);
    }

    @Override
    public final void onSuccess(RequestQueue queue, T data) {
        Intent intent = new Intent();
        intent.setAction(RestReceiver.ACTION_SUCCESS);
        if(data instanceof Parcelable) {
            intent.putExtra(RestReceiver.EXTRA_RESULT, (Parcelable) data);
        } else if(data instanceof Serializable) {
            intent.putExtra(RestReceiver.EXTRA_RESULT, (Serializable) data);
        } else {
            throw new IllegalArgumentException("Parameter <T> in the ReceiverCallback must implement Parcelable or Serializable");
        }
        intent.putExtra(RestReceiver.EXTRA_TARGET, mClazz);
        intent.putExtra(RestReceiver.EXTRA_CODE, mCode);
        mContext.sendBroadcast(intent);
    }

    @Override
    public final void onError(RequestQueue queue, Throwable t) {
        Intent intent = new Intent();
        intent.setAction(RestReceiver.ACTION_ERROR);
        intent.putExtra(RestReceiver.EXTRA_ERROR, t);
        intent.putExtra(RestReceiver.EXTRA_TARGET, mClazz);
        intent.putExtra(RestReceiver.EXTRA_CODE, mCode);
        mContext.sendBroadcast(intent);
    }

    @Override
    public final void onPostExecute(RequestQueue queue) {
        Intent intent = new Intent();
        intent.setAction(RestReceiver.ACTION_POST_EXECUTE);
        intent.putExtra(RestReceiver.EXTRA_CODE, mCode);
        intent.putExtra(RestReceiver.EXTRA_TARGET, mClazz);
        mContext.sendBroadcast(intent);
    }
}