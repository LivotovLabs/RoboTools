package eu.livotov.labs.android.robotools.api;

import eu.livotov.labs.android.robotools.net.RTCookieStore;
import eu.livotov.labs.android.robotools.net.RTHTTPClient;
import eu.livotov.labs.android.robotools.net.RTHTTPError;
import eu.livotov.labs.android.robotools.net.RTPostParameter;

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
        private RTCookieStore cookies = new RTCookieStore();


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
                final String url = String.format("%s/%s", getEndpointUrlFor(cmd), cmd.buildRequestUri());
                final List<RTPostParameter> parameters = new ArrayList<RTPostParameter>();
                cmd.buildRequestParameters(parameters);

                if (debugMode)
                {
                    System.err.println("\n========================================\n\n");
                    System.err.println("Calling API method: " + url);
                    for (RTPostParameter p : parameters)
                    {
                        System.err.println(String.format("  %s = %s", p.getName(), p.getValue()));
                    }
                    System.err.println("\n========================================\n\n");
                }

                final String data = loadHttpResponseToString(submitForm(url, new ArrayList<RTPostParameter>(), parameters), transportEncoding);

                if (debugMode)
                {
                    System.err.println("\n========================================\n\n");
                    System.err.println("<< " + url);
                    System.err.println(data);
                    System.err.println("\n========================================\n\n");
                }

                return cmd.parseServerResponseData(data);
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

        public void resetCookies()
        {
            getConfiguration().resetCookies();
        }
}
