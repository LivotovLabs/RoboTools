package eu.livotov.labs.android.robotools.net;

import org.apache.http.client.CookieStore;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 29.01.13
 */
public class RTHTTPClientConfiguration {
    private int httpConnectionTimeout = 60000;

    private int httpDataResponseTimeout = 60000;

    private boolean allowSelfSignedCerts = false;

    private boolean enableGzipCompression = false;

    private int requestRetryCount = 0;

    private CookieStore cookieStore = new RTCookieStore();

    private boolean dirty = true;


    public int getHttpConnectionTimeout() {
        return httpConnectionTimeout;
    }

    public void setHttpConnectionTimeout(final int httpConnectionTimeout) {
        this.httpConnectionTimeout = httpConnectionTimeout;
        dirty = true;
    }

    public int getHttpDataResponseTimeout() {
        return httpDataResponseTimeout;
    }

    public void setHttpDataResponseTimeout(final int httpDataResponseTimeout) {
        this.httpDataResponseTimeout = httpDataResponseTimeout;
        dirty = true;
    }

    public boolean isAllowSelfSignedCerts() {
        return allowSelfSignedCerts;
    }

    public void setAllowSelfSignedCerts(final boolean allowSelfSignedCerts) {
        this.allowSelfSignedCerts = allowSelfSignedCerts;
        dirty = true;
    }

    public boolean isEnableGzipCompression() {
        return enableGzipCompression;
    }

    public void setEnableGzipCompression(final boolean enableGzipCompression) {
        this.enableGzipCompression = enableGzipCompression;
        dirty = true;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        dirty = true;
    }

    public int getRequestRetryCount() {
        return requestRetryCount;
    }

    public void setRequestRetryCount(final int requestRetryCount) {
        this.requestRetryCount = requestRetryCount;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirtyFlag() {
        dirty = false;
    }

    public void resetCookies() {
        if (cookieStore != null) {
            cookieStore.clear();
        }
    }
}
