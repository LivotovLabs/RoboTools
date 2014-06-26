package eu.livotov.labs.android.robotools.compat.v1.net;

import org.apache.http.client.CookieStore;
import org.apache.http.conn.scheme.SocketFactory;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 29.01.13
 */
public class RTHTTPClientConfiguration
{

    private int httpConnectionTimeout = 60000;

    private int httpDataResponseTimeout = 60000;

    private boolean allowSelfSignedCerts = false;

    private boolean enableGzipCompression = false;

    private boolean useExpectContinue = false;

    private int requestRetryCount = 0;

    private boolean allowRedirects = true;

    private CookieStore cookieStore;

    private boolean dirty = true;

    private SocketFactory sslSocketFactory;

    private int defaultSslPort = 443;

    private int defaultHttpPort = 80;

    private String userAgent;


    public int getHttpConnectionTimeout()
    {
        return httpConnectionTimeout;
    }

    public void setHttpConnectionTimeout(final int httpConnectionTimeout)
    {
        this.httpConnectionTimeout = httpConnectionTimeout;
        dirty = true;
    }

    public int getHttpDataResponseTimeout()
    {
        return httpDataResponseTimeout;
    }

    public void setHttpDataResponseTimeout(final int httpDataResponseTimeout)
    {
        this.httpDataResponseTimeout = httpDataResponseTimeout;
        dirty = true;
    }

    public boolean isAllowSelfSignedCerts()
    {
        return allowSelfSignedCerts;
    }

    public void setAllowSelfSignedCerts(final boolean allowSelfSignedCerts)
    {
        this.allowSelfSignedCerts = allowSelfSignedCerts;
        dirty = true;
    }

    public boolean isEnableGzipCompression()
    {
        return enableGzipCompression;
    }

    public void setEnableGzipCompression(final boolean enableGzipCompression)
    {
        this.enableGzipCompression = enableGzipCompression;
        dirty = true;
    }

    public CookieStore getCookieStore()
    {
        return cookieStore;
    }

    public void setCookieStore(final CookieStore cookieStore)
    {
        this.cookieStore = cookieStore;
        dirty = true;
    }

    public int getRequestRetryCount()
    {
        return requestRetryCount;
    }

    public void setRequestRetryCount(final int requestRetryCount)
    {
        this.requestRetryCount = requestRetryCount;
        dirty = true;
    }

    public boolean isAllowRedirects()
    {
        return allowRedirects;
    }

    public void setAllowRedirects(final boolean allowRedirects)
    {
        this.allowRedirects = allowRedirects;
        dirty = true;
    }

    public SocketFactory getSslSocketFactory()
    {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(final SocketFactory sslSocketFactory)
    {
        this.sslSocketFactory = sslSocketFactory;

        if (sslSocketFactory != null)
        {
            allowSelfSignedCerts = false;
        }

        dirty = true;
    }

    public int getDefaultSslPort()
    {
        return defaultSslPort;
    }

    public void setDefaultSslPort(final int defaultSslPort)
    {
        this.defaultSslPort = defaultSslPort;
        dirty = true;
    }

    public int getDefaultHttpPort()
    {
        return defaultHttpPort;
    }

    public void setDefaultHttpPort(final int defaultHttpPort)
    {
        this.defaultHttpPort = defaultHttpPort;
        dirty = true;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(final String userAgent)
    {
        this.userAgent = userAgent;
        dirty = true;
    }

    public boolean isUseExpectContinue()
    {
        return useExpectContinue;
    }

    public void setUseExpectContinue(final boolean useExpectContinue)
    {
        this.useExpectContinue = useExpectContinue;
        dirty = true;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public void clearDirtyFlag()
    {
        dirty = false;
    }

    public void resetCookies()
    {
        if (cookieStore != null)
        {
            cookieStore.clear();
        }
    }
}
