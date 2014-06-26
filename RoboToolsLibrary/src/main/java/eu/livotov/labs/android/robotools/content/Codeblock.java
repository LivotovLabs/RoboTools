package eu.livotov.labs.android.robotools.content;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.CancellationException;

/**
 * Codeblock allows you to execute unlimited requests in one block asynchronously and successively. <br />
 * <B>Note</B>: Codeblock are not processes captcha errors automatically.
 * @param <T> type of request result.
 */
public abstract class Codeblock<T> {

    boolean mCanceled;

    /**
     * Executes the codeblock in accordance with the life cycle in the current thread.
     * @return parsed and checked the server's response.
     * @throws IOException if network error occurs
     * @throws JSONException if server response unable to parse
     * @throws CancellationException if codeblock was canceled during its execution
     */
    public abstract T run() throws ServerException, IOException, JSONException, CancellationException;

    /**
     * If this method returns true, and same preference is enabled is
     * VK, the application can be interrupted by some VK server errors to
     * user can resolve them.
     *
     * For example, validation process, re-authorization.
     *
     * If this method return false or same preference is disabled is
     * VK, the exception will be sent to callback like any other server exception.
     *
     * @return true if this method allows UI interruption, false otherwise.
     */
    public boolean interruptUIIfNecessary() {
        return true;
    }

    /**
     * Cancels this codeblock.
     * If codeblock is canceled, it will never be executed to dispatched to callback.
     * Does not work, if the codeblock is already executed ​​or dispatched.
     * This action cannot be undone.
     */
    public void cancel() {
        mCanceled = true;
    }

    public boolean isCanceled() {
        return mCanceled;
    }
}
