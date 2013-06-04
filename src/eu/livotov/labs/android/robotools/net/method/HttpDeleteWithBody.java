package eu.livotov.labs.android.robotools.net.method;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 04/06/2013
 */
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase
{

    public static final String METHOD_NAME = "DELETE";

    public String getMethod()
    {
        return METHOD_NAME;
    }

    public HttpDeleteWithBody(final String uri)
    {
        super();
        setURI(URI.create(uri));
    }

    public HttpDeleteWithBody(final URI uri)
    {
        super();
        setURI(uri);
    }

    public HttpDeleteWithBody()
    {
        super();
    }
}