package eu.livotov.labs.android.robotools.api;

import eu.livotov.labs.android.robotools.net.RTHTTPClient;
import eu.livotov.labs.android.robotools.net.RTHTTPError;
import eu.livotov.labs.android.robotools.net.RTPostParameter;
import org.apache.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 29.01.13
 */
public abstract class RTApiClient extends RTHTTPClient
{

    private String transportEncoding = "utf-8";
    private boolean debugMode = false;


    protected RTApiClient()
    {
        super(true);
    }

    public boolean isDebugMode()
    {
        return debugMode;
    }

    public void setDebugMode(final boolean debugMode)
    {
        this.debugMode = debugMode;
    }

    public abstract String getEndpointUrlFor(RTApiCommand cmd);

    public RTApiCommandResult execute(RTApiCommand cmd)
    {
        try
        {
            final String url = String.format("%s%s", getEndpointUrlFor(cmd), secureSlash(cmd.buildRequestUri()));
            final List<RTPostParameter> parameters = new ArrayList<RTPostParameter>();
            cmd.buildRequestParameters(parameters);

            List<RTPostParameter> headers = new ArrayList<RTPostParameter>();
            onCommandPreExecute(cmd, url, parameters, headers);
            RTApiRequestType rtType = cmd.getRequestType();

            if (debugMode)
            {
                System.err.println("Calling API method via " + rtType.name() + " : " + url);
                System.err.println("With headers: ");
                for (RTPostParameter p : headers)
                {
                    System.err.println(String.format("  %s = %s", p.getName(), p.getValue()));
                }
            }

            HttpResponse response = null;

            switch (rtType)
            {
                case POST:
                    response = processPost(cmd,url,parameters,headers);
                    break;

                case GET:
                    response = executeGetRequest(url,headers,parameters);
                    break;

                case PUT:
                    response = executePutRequest(url,headers,parameters);
                    break;

                case DELETE:
                    response = executeDeleteRequest(url,headers,parameters);
                    break;

                default:
                    response = submitForm(url, headers, parameters);
                    break;
            }

            onCommandHttpRequestDone(cmd, url, parameters, response);

            if (debugMode)
            {
                System.err.println(">> Server returned status code: " + response.getStatusLine().getStatusCode());
                System.err.println(">> " + response.getStatusLine().getReasonPhrase());
            }

            final String data = loadHttpResponseToString(response, transportEncoding);

            onCommandResponseDataLoaded(cmd, url, parameters, response, data);

            if (debugMode)
            {
                System.err.println("\n\n<< " + url);
                System.err.println(data);
                System.err.println("\n\n\n\n");
            }

            RTApiCommandResult result = cmd.parseServerResponseData(data);
            onCommandPostExecure(cmd, url, parameters, response, result);
            return result;
        } catch (RTHTTPError httpError)
        {
            throw new RTApiError(httpError);
        } catch (RTApiError baw)
        {
            throw baw;
        } catch (Throwable otherError)
        {
            throw new RTApiError(otherError);
        }
    }

    private HttpResponse processPost(final RTApiCommand cmd, final String url, final List<RTPostParameter> parameters, final List<RTPostParameter> headers)
    {
        StringBuffer body = new StringBuffer();
        cmd.buildRequestBody(body);

        if (debugMode)
        {
            if (body.length()<=0)
            {
            for (RTPostParameter p : parameters)
            {
                System.err.println("Form submitted:\n");
                System.err.println(String.format("  %s = %s", p.getName(), p.getValue()));
            }
            } else
            {
                System.err.println("Request body:\n" + body.toString());
            }
        }

        if (body.length()>0)
        {
            return executePostRequest(url, "Content-Type: application/json", "utf-8", body.toString(), headers.toArray(new RTPostParameter[headers.size()]));
        } else
        {
            return submitForm(url, headers, parameters);
        }
    }

    protected abstract void onCommandHttpRequestDone(RTApiCommand cmd, String url, List<RTPostParameter> parameters, HttpResponse response);

    protected abstract void onCommandResponseDataLoaded(RTApiCommand cmd, String url, List<RTPostParameter> parameters, HttpResponse response, String data);

    protected abstract void onCommandPostExecure(RTApiCommand cmd, String url, List<RTPostParameter> parameters, HttpResponse response, RTApiCommandResult result);

    protected abstract void onCommandPreExecute(RTApiCommand cmd, String finalUrl, List<RTPostParameter> parameters, List<RTPostParameter> headers);

    public void resetCookies()
    {
        getConfiguration().resetCookies();
    }

    protected String secureSlash(String path)
    {
        if (path != null && path.length() > 1 && !path.startsWith("/"))
        {
            return "/" + path;
        }

        return path;
    }
}
