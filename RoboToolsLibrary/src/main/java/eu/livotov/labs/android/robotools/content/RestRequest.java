package eu.livotov.labs.android.robotools.content;

import android.net.Uri;
import android.text.TextUtils;
import eu.livotov.labs.android.robotools.net.HttpRequest;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

import static java.lang.String.valueOf;

/**
 * Represents request to REST service.
 * All params will be transferred as POST.
 *
 * You can extend it to write custom raw request.
 *
 * This class does not support handling VK API errors,
 * but allows you to send some files.
 * @param <T> type of request result
 */
@SuppressWarnings("unused")
public abstract class RestRequest<T> {

    protected enum Method { GET, POST, DELETE, PUT, HEAD }

    /**
     * Delimiter for list params.
     */
    private final static String DELIMITER = ",";

    /**
     * Url to connection.
     */
    CharSequence mUrl;

    Method mMethod;

    /**
     * Arguments of this request.
     */
    protected final Map<CharSequence, CharSequence> mParams = new LinkedHashMap<CharSequence, CharSequence>();

    /**
     * Files to transfer via HTTP for this request.
     */
    protected final Map<CharSequence, Uri> mFiles = new LinkedHashMap<CharSequence, Uri>();

    /**
     * Headers to send.
     */
    final Map<CharSequence, CharSequence> mHeaders = new HashMap<CharSequence, CharSequence>();

    /**
     * Flag for cancel this request.
     */
    private boolean mCanceled;

    /**
     * Creates request in accordance with the specified URL.
     */
    public RestRequest(Method method, CharSequence url) {
        if(url == null) {
            throw new NullPointerException("URL cannot be null");
        }
        if(method == null) {
            throw new NullPointerException("Method cannot be null");
        }
        mUrl = url;
        mMethod = method;
    }

    /**
     * Constructor special for SDK subclasses.
     */
    RestRequest() {

    }

    /***********************************************************************
     *                        LIFECYCLE METHODS
     ***********************************************************************/

    /**
     * Override this method to define parsing data procedure.
     * @param source response from server without pre-processing.
     * @return valid data.
     * @throws org.json.JSONException if data is not correct. Exception handled
     * automatically by requests manager end sends to callback as API error.
     */
    protected abstract T parse(String source) throws JSONException;

    /**
     * This method is called each time before sending the request.
     * You can override it to add custom action with data.
     */
    protected void onPreExecute() {

    }

    /**
     * This method is called each time after sending the request.
     * You can override it to add custom action with data or result.
     * @throws java.io.IOException if network error occurs
     * @throws org.json.JSONException if server response unable to parse
     * @throws java.util.concurrent.CancellationException if request was canceled during its execution
     */
    protected void onPostExecute(T result) throws IOException, JSONException, CancellationException {

    }

    /**
     * Executes the query in accordance with the life cycle in the current thread.
     * @return parsed and checked the server's response.
     * @throws java.io.IOException if network error occurs
     * @throws org.json.JSONException if server response unable to parse
     * @throws java.util.concurrent.CancellationException if request was canceled during its execution
     * @throws eu.livotov.labs.android.robotools.content.ServerException if server returned an error.
     */
    public final T execute() throws IOException, JSONException, CancellationException, ServerException {
        if(!isCanceled()) {
            onPreExecute();
            HttpRequest httpRequest = (mMethod == Method.GET ? HttpRequest.get(mUrl, mParams) : new HttpRequest(mUrl, mMethod.toString()).post(mParams)).
                    headers(mHeaders).files(mFiles);
            String body = httpRequest.body();
            body = validateResponse(body);
            httpRequest.close();
            T result = parse(body);
            onPostExecute(result);
            return result;
        } else {
            throw new CancellationException("This request was canceled: " + mUrl);
        }
    }

    protected String validateResponse(String response) throws ServerException {
        return response;
    }

    /***********************************************************************
     *                        GETTERS AND SETTERS
     ***********************************************************************/

    /**
     * Cancels this request.
     * If request is canceled, it will never be executed to dispatched to callback.
     * Does not work, if the request is already executed ​​or dispatched.
     * This action cannot be undone.
     */
    public void cancel() {
        mCanceled = true;
    }

    /**
     * @return true is request wan canceler, false otherwise.
     * @see #cancel()
     */
    public boolean isCanceled() {
        return mCanceled;
    }

    /**
     * Adds a request params according with given key and value.
     */
    public RestRequest param(CharSequence key, CharSequence value) {
        if(key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        mParams.put(key, value);
        return this;
    }

    /**
     * Adds a request params according with given key and value.
     * If value is {@code true}, param will be equal {@code 1}, {@code 0} otherwise.
     */
    public RestRequest param(CharSequence key, boolean value) {
        return param(key, String.valueOf(value));
    }

    /**
     * Adds a request params according with given key and value.
     */
    public RestRequest param(CharSequence key, byte value) {
        return param(key, String.valueOf(value));
    }

    /**
     * Adds a request params according with given key and value.
     */
    public RestRequest param(CharSequence key, short value) {
        return param(key, String.valueOf(value));
    }

    /**
     * Adds a request params according with given key and value.
     */
    public RestRequest param(CharSequence key, int value) {
        return param(key, String.valueOf(value));
    }

    /**
     * Adds a request params according with given key and value.
     */
    public RestRequest param(CharSequence key, long value) {
        return param(key, String.valueOf(value));
    }

    /**
     * Adds a request params according with given key and value.
     */
    public RestRequest param(CharSequence key, float value) {
        return param(key, String.valueOf(value));
    }

    /**
     * Adds a request params according with given key and value.
     */
    public RestRequest param(CharSequence key, double value) {
        return param(key, String.valueOf(value));
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this array {@code ["val1", "val2", "val3"} it's would be
     * {@code "val1,val2,val3"}.
     */
    public RestRequest params(CharSequence key, CharSequence[] value) {
        if(value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        mParams.put(key, TextUtils.join(DELIMITER, value));
        return this;
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this list {@code {"val1", "val2", "val3"}} it's would be
     * {@code "val1,val2,val3"}.
     */
    public RestRequest params(CharSequence key, Iterable value) {
        if(value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        mParams.put(key, TextUtils.join(DELIMITER, value));
        return this;
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this array {@code {1, 2, 3}} it's would be
     * {@code "1,2,3"}.
     */
    public RestRequest params(CharSequence key, byte[] values) {
        if(values == null) {
            throw new NullPointerException("Values cannot be null");
        }
        String[] value = new String[values.length];
        for(int i = 0; i < values.length; i++) {
            value[i] = valueOf(values[i]);
        }
        return params(key, value);
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this array {@code {1, 2, 3}} it's would be
     * {@code "1,2,3"}.
     */
    public RestRequest params(CharSequence key, short[] values) {
        if(values == null) {
            throw new NullPointerException("Values cannot be null");
        }
        String[] value = new String[values.length];
        for(int i = 0; i < values.length; i++) {
            value[i] = valueOf(values[i]);
        }
        return params(key, value);
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this array {@code {1, 2, 3}} it's would be
     * {@code "1,2,3"}.
     */
    public RestRequest params(CharSequence key, int[] values) {
        if(values == null) {
            throw new NullPointerException("Values cannot be null");
        }
        String[] value = new String[values.length];
        for(int i = 0; i < values.length; i++) {
            value[i] = valueOf(values[i]);
        }
        return params(key, value);
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this array {@code {1, 2, 3}} it's would be
     * {@code "1,2,3"}.
     */
    public RestRequest params(CharSequence key, long[] values) {
        if(values == null) {
            throw new NullPointerException("Values cannot be null");
        }
        String[] value = new String[values.length];
        for(int i = 0; i < values.length; i++) {
            value[i] = valueOf(values[i]);
        }
        return params(key, value);
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this array {@code {1, 2, 3}} it's would be
     * {@code "1,2,3"}.
     */
    public RestRequest params(CharSequence key, float[] values) {
        if(values == null) {
            throw new NullPointerException("Values cannot be null");
        }
        String[] value = new String[values.length];
        for(int i = 0; i < values.length; i++) {
            value[i] = valueOf(values[i]);
        }
        return params(key, value);
    }

    /**
     * Adds a request params according with given key and value.
     * The value will be sent as a list of values ​​separated by commas.
     *
     * For example, for this array {@code {1, 2, 3}} it's would be
     * {@code "1,2,3"}.
     */
    public RestRequest params(CharSequence key, double[] values) {
        if(values == null) {
            throw new NullPointerException("Values cannot be null");
        }
        String[] value = new String[values.length];
        for(int i = 0; i < values.length; i++) {
            value[i] = valueOf(values[i]);
        }
        return params(key, value);
    }

    /**
     * Attach file to this request.
     * @param key name of param for file.
     * @param file resolved absolute path to file.
     */
    public RestRequest files(CharSequence key, Uri file) {
        mFiles.put(key, file);
        return this;
    }

    public RestRequest header(CharSequence key, CharSequence value) {
        mHeaders.put(key, value);
        return this;
    }
}

