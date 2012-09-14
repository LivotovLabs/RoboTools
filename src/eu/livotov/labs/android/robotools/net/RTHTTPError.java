package eu.livotov.labs.android.robotools.net;

import org.apache.http.StatusLine;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTHTTPError extends RuntimeException {
    public RTHTTPError(Throwable cause) {
    }

    public RTHTTPError(StatusLine statusLine) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
