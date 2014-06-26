package eu.livotov.labs.android.robotools.content;
/**
 * This callback allow you to send the same request with given period. <br />
 * <b>Note</b>: if you cancels this callback, you'll should not use it again.
 * @param <T>  type of request result.
 */
public final class ScheduledCallback<T> implements Callback<T> {

    private final Callback<T> mCallback;
    private boolean mCanceled;
    private long mPeriod;
    private RestRequest<T> mRequest;

    public ScheduledCallback(RestRequest<T> request, Callback<T> wrapped, long period) {
        mRequest = request;
        mCallback = wrapped;
        mPeriod = period;
    }

    @Override
    public void onAdded(RequestQueue queue) {
        if(mCanceled) {
            throw new IllegalStateException("ScheduledCallback was canceled. Please use a new instance instead of this");
        }
        mCallback.onAdded(queue);
    }

    @Override
    public void onPostExecute(RequestQueue queue) {
        mCallback.onPostExecute(queue);
        if(!mCanceled) {
            queue.exec(mRequest, this, mPeriod);
        }
    }

    @Override
    public void onPreExecute(RequestQueue queue) {
        mCallback.onPreExecute(queue);
    }

    public void onSuccess(RequestQueue queue, T data) {
        mCallback.onSuccess(queue, data);
    }

    @Override
    public void onError(RequestQueue queue, Throwable t) {
        mCallback.onError(queue, t);
    }

    /**
     * Cancels current request and callback. <br />
     * <b>Note</b>: if you cancels this callback, you'll should not use it again.
     */
    public void cancel() {
        mCanceled = true;
        mRequest.cancel();
    }
}

