package eu.livotov.labs.android.robotools.content;

/**
 * Callback for requests.
 * Each method invoked in UI thread.
 * @param <T> type of request result.
 */
public interface Callback<T> {

    /**
     * Called when request is added fo requests queue.
     */
    void onAdded(RequestQueue queue);

    /**
     * Called before the request has been executed.
     */
    void onPreExecute(RequestQueue queue);

    /**
     * Called if request was successful and without errors.
     * @param data result of this response.
     */
    void onSuccess(RequestQueue queue, T data);

    /**
     * Called if the server returned an error code.
     * @param t error exception.
     */
    void onError(RequestQueue queue, Throwable t);

    /**
     * Called after the request has been executed.
     */
    void onPostExecute(RequestQueue queue);
}