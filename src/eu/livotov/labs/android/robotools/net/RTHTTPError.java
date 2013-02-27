package eu.livotov.labs.android.robotools.net;

import android.text.TextUtils;
import eu.livotov.labs.android.robotools.io.RTStreamUtil;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTHTTPError extends RuntimeException
{

    private int statusCode;
    private String statusText;
    private String responseBody;
    private String protocolVersion;

    public RTHTTPError(Throwable cause)
    {
        super(cause);
        statusCode = ErrorCodes.InternalApplicationError;
        statusText = null;
        protocolVersion = null;
    }

    public RTHTTPError(HttpResponse rsp, final String responseBody)
    {
        StatusLine statusLine = rsp.getStatusLine();
        statusCode = statusLine.getStatusCode();
        statusText = statusLine.getReasonPhrase();

        if (TextUtils.isEmpty(responseBody))
        {
            try
            {
                this.responseBody = RTStreamUtil.streamToString(rsp.getEntity().getContent(), TextUtils.isEmpty(rsp.getEntity().getContentEncoding().getValue()) ? "utf-8" : rsp.getEntity().getContentEncoding().getValue(), true);
            } catch (Throwable err)
            {
            }
        } else
        {
            this.responseBody = responseBody;
        }

        protocolVersion = statusLine.getProtocolVersion().toString();
    }

    public String getMessage()
    {
        if (statusCode != ErrorCodes.InternalApplicationError)
        {
            return (statusText == null || statusText.isEmpty()) ? ("HTTP Error: " + statusCode) : statusText;
        } else
        {
            return super.getMessage();
        }
    }

    public String getLocalizedMessage()
    {
        if (statusCode != ErrorCodes.InternalApplicationError)
        {
            return getMessage();
        } else
        {
            return super.getLocalizedMessage();
        }
    }

    public String getResponseBody()
    {
        return responseBody;
    }

    public static class ErrorCodes
    {

        public final static int InternalApplicationError = -65535;
    }
}
