package eu.livotov.labs.android.robotools.content;

import android.util.Pair;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Allow to join a few request to execute them successively.
 */
public final class RequestsBatch {

    private Queue<Pair<RestRequest, Callback>> mQueue = new LinkedList<Pair<RestRequest, Callback>>();

    /**
     * Appends request to the batch.
     *
     * @param request  request to append
     * @param callback callback to take results of request.
     * @param <T>      type of result
     * @return this batch
     */
    public <T> RequestsBatch add(RestRequest<T> request, Callback<T> callback) {
        mQueue.add(new Pair<RestRequest, Callback>(request, new CallbackWrapper<T>(callback)));
        return this;
    }

    void exec(RequestQueue queue) {
        if (mQueue.size() > 0) {
            Pair<RestRequest, Callback> pair = mQueue.remove();
            if (pair != null) {
                queue.exec(pair.first, pair.second);
            }
        }
    }

    private final class CallbackWrapper<T> implements Callback<T> {

        private final Callback<T> mWrapped;

        CallbackWrapper(Callback<T> callback) {
            mWrapped = callback;
        }

        @Override
        public void onAdded(RequestQueue queue) {
            mWrapped.onAdded(queue);
        }

        @Override
        public void onPreExecute(RequestQueue queue) {
            mWrapped.onPreExecute(queue);
        }

        @Override
        public void onSuccess(RequestQueue queue, T data) {
            mWrapped.onSuccess(queue, data);
        }

        @Override
        public void onError(RequestQueue queue, Throwable t) {
            mWrapped.onError(queue, t);
        }

        @Override
        public void onPostExecute(RequestQueue queue) {
            mWrapped.onPostExecute(queue);
            RequestsBatch.this.exec(queue);
        }
    }

}
