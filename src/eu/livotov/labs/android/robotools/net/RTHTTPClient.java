package eu.livotov.labs.android.robotools.net;

import eu.livotov.labs.android.robotools.io.RTStreamUtil;
import eu.livotov.labs.android.robotools.net.multipart.FilePart;
import eu.livotov.labs.android.robotools.net.multipart.MultipartEntity;
import eu.livotov.labs.android.robotools.net.multipart.Part;
import eu.livotov.labs.android.robotools.net.multipart.StringPart;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 12.09.12
 */
public class RTHTTPClient implements HttpRequestRetryHandler
{

    private DefaultHttpClient http;

    private RTHTTPClientConfiguration configuration = new RTHTTPClientConfiguration();


    public RTHTTPClient()
    {
        this(false);
    }

    public RTHTTPClient(boolean allowSelfSignedCerts)
    {
        configuration.setAllowSelfSignedCerts(allowSelfSignedCerts);
    }

    public RTHTTPClientConfiguration getConfiguration()
    {
        return configuration;
    }

    public HttpResponse executeGetRequest(final String url)
    {
        try
        {
            if (configuration.isDirty())
            {
                reconfigureHttpClient();
            }

            HttpGet get = new HttpGet(url);
            return http.execute(get);
        } catch (Throwable err)
        {
            throw new RTHTTPError(err);
        }
    }

    public String executeGetRequestToString(final String url)
    {
        return executeGetRequestToString(url, "utf-8");
    }

    public String executeGetRequestToString(final String url, final String encoding)
    {
        HttpResponse response = executeGetRequest(url);
        return loadHttpResponseToString(response, encoding);
    }

    public HttpResponse executePostRequest(final String url, final String contentType, final String content, RTPostParameter... headers)
    {
        return executePostRequest(url, contentType, "utf-8", content, headers);
    }

    public HttpResponse executePostRequest(final String url, final String contentType, final String encoding, final String content, RTPostParameter... headers)
    {
        try
        {
            if (configuration.isDirty())
            {
                reconfigureHttpClient();
            }

            HttpPost post = new HttpPost(url);

            StringEntity postEntity = new StringEntity(content, encoding);
            postEntity.setContentType(contentType + "; charset=" + encoding);

            if (headers != null)
            {
                for (RTPostParameter header : headers)
                {
                    post.addHeader(header.getName(), header.getValue());
                }
            }

            post.setEntity(postEntity);
            return http.execute(post);
        } catch (Throwable err)
        {
            throw new RTHTTPError(err);
        }
    }

    public String executePostRequestToString(final String url, final String contentType, final String content, RTPostParameter... headers)
    {
        return executePostRequestToString(url, contentType, "utf-8", content, headers);
    }

    public String executePostRequestToString(final String url, final String contentType, final String encoding, final String content, RTPostParameter... headers)
    {
        HttpResponse response = executePostRequest(url, contentType, encoding, content, headers);
        return loadHttpResponseToString(response, encoding);
    }

    public HttpResponse submitMultipartForm(final String url, Collection<RTPostParameter> headers, Collection<RTPostParameter> formFields, final String fileFeldName, File file)
    {
        try
        {
            if (configuration.isDirty())
            {
                reconfigureHttpClient();
            }

            HttpPost httppost = new HttpPost(url);

            List<Part> parts = new ArrayList<Part>();

            for (RTPostParameter field : formFields)
            {
                parts.add(new StringPart(field.getName(), field.getValue(), "utf-8"));
            }

            FilePart pData = new FilePart(fileFeldName, file);
            pData.setTransferEncoding("8bit");
            parts.add(pData);

            MultipartEntity mpEntity = new MultipartEntity(parts.toArray(new Part[parts.size()]));
            httppost.setEntity(mpEntity);

            for (RTPostParameter header : headers)
            {
                httppost.addHeader(header.getName(), header.getValue());
            }

            return http.execute(httppost);
        } catch (Throwable err)
        {
            throw new RTHTTPError(err);
        }
    }

    public HttpResponse submitForm(final String url, Collection<RTPostParameter> headers, Collection<RTPostParameter> formFields)
    {
        try
        {
            if (configuration.isDirty())
                        {
                            reconfigureHttpClient();
                        }

            HttpPost httppost = new HttpPost(url);

            List<Part> parts = new ArrayList<Part>();

            for (RTPostParameter field : formFields)
            {
                parts.add(new StringPart(field.getName(), field.getValue(), "utf-8"));
            }

            MultipartEntity mpEntity = new MultipartEntity(parts.toArray(new Part[parts.size()]));
            httppost.setEntity(mpEntity);

            for (RTPostParameter header : headers)
            {
                httppost.addHeader(header.getName(), header.getValue());
            }

            return http.execute(httppost);
        } catch (Throwable err)
        {
            throw new RTHTTPError(err);
        }
    }

    public String loadHttpResponseToString(HttpResponse response, final String encoding)
    {
        if (response.getStatusLine().getStatusCode() == 200)
        {
            try
            {
                return RTStreamUtil.streamToString(response.getEntity().getContent(), encoding, true);
            } catch (Throwable err)
            {
                throw new RTHTTPError(err);
            }
        }

        throw new RTHTTPError(response.getStatusLine());
    }

    protected void reconfigureHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException
    {

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpClientParams.setRedirecting(params, false);
        HttpClientParams.setCookiePolicy(params, CookiePolicy.BEST_MATCH);
        params.setParameter("http.protocol.expect-continue", false);
        HttpConnectionParams.setConnectionTimeout(params, configuration.getHttpConnectionTimeout());
        HttpConnectionParams.setSoTimeout(params, configuration.getHttpDataResponseTimeout());

        if (configuration.isAllowSelfSignedCerts())
        {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
            {
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager()
            {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                }

                public X509Certificate[] getAcceptedIssuers()
                {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            org.apache.http.conn.ssl.SSLSocketFactory sf = new DummySslSocketFactory(trustStore);
            sf.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            http = new DefaultHttpClient(ccm, params);
        } else
        {
            http = new DefaultHttpClient(params);
        }

        http.setHttpRequestRetryHandler(this);

        if (configuration.isEnableGzipCompression())
        {
            http.addRequestInterceptor(new HttpRequestInterceptor()
            {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException,
                                                                                                 IOException
                {
                    if (!request.containsHeader("Accept-Encoding"))
                    {
                        request.addHeader("Accept-Encoding", "gzip");
                    }
                }

            });

            http.addResponseInterceptor(new HttpResponseInterceptor()
            {
                public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException
                {
                    HttpEntity entity = response.getEntity();
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null)
                    {
                        HeaderElement[] codecs = ceheader.getElements();
                        for (int i = 0; i < codecs.length; i++)
                        {
                            if (codecs[i].getName().equalsIgnoreCase("gzip"))
                            {
                                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                return;
                            }
                        }
                    }
                }

            });
        }

        if (configuration.getCookieStore() != null)
        {
            http.setCookieStore(configuration.getCookieStore());
        }

        configuration.clearDirtyFlag();
    }

    @Override
    public boolean retryRequest(IOException e, int i, HttpContext httpContext)
    {
        if (configuration.getRequestRetryCount()==0 || i > configuration.getRequestRetryCount())
        {
            return false;
        } else
        {
            return true;
        }
    }

    class DummySslSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory
    {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public DummySslSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                                                                 KeyStoreException, UnrecoverableKeyException
        {
            super(truststore);

            TrustManager tm = new X509TrustManager()
            {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                }

                public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException
        {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException
        {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    class GzipDecompressingEntity extends HttpEntityWrapper
    {

        public GzipDecompressingEntity(final HttpEntity entity)
        {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException
        {

            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength()
        {
            return -1;
        }

    }
}
