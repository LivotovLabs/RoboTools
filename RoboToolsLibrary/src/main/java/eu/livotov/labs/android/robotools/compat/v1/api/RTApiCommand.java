package eu.livotov.labs.android.robotools.compat.v1.api;

import android.os.AsyncTask;
import eu.livotov.labs.android.robotools.compat.v1.net.RTHTTPError;
import eu.livotov.labs.android.robotools.compat.v1.net.RTPostParameter;

import java.util.List;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 29.01.13
 */
public abstract class RTApiCommand
{

    public final static class ContentTypes
    {

        public final static String Json = "application/json";
        public final static String Xml = "text/xml; charset=utf-8";
    }

    protected int retryCount = 3;

    protected Class resultClass;

    public RTApiCommand(Class resultClass) //todo: to be cleaned up once Oracle fix #5098163 since 2004 ! %=/
    {
        this.resultClass = resultClass;
    }

    public abstract String buildRequestUri();

    public abstract void buildRequestParameters(List<RTPostParameter> params);

    public abstract void buildRequestBody(StringBuffer body);

    public abstract RTApiRequestType getRequestType();

    public abstract RTApiClient getClient();

    public String getContentType()
    {
        return ContentTypes.Json;
    }

    public RTApiCommandResult parseServerResponseData(final String data) throws RTApiError
    {
        try
        {
            RTApiCommandResult response = (RTApiCommandResult) resultClass.newInstance();
            response.loadResponseData(this, data);
            return response;
        } catch (RTApiRetryRequiredException retry)
        {
            retryCount--;
            if (retryCount>0)
            {
                return execute();
            } else
            {
                throw new RTApiError(RTApiError.ErrorCodes.TooMuchRetries);
            }
        } catch (RTApiError bawError)
        {
            throw bawError;
        } catch (Throwable err)
        {
            throw new RTApiError(err);
        }
    }

    public RTApiCommandResult parseServerErrorResponseData(RTHTTPError error) throws RTApiError
    {
        try
        {
            RTApiCommandResult response = (RTApiCommandResult) resultClass.newInstance();
            response.loadErrorResponseData(this, error);
            return response;
        } catch (RTApiRetryRequiredException retry)
        {
            retryCount--;
            if (retryCount>0)
            {
                return execute();
            } else
            {
                throw new RTApiError(RTApiError.ErrorCodes.TooMuchRetries);
            }
        } catch (RTApiError bawError)
        {
            throw bawError;
        } catch (Throwable err)
        {
            throw new RTApiError(err);
        }
    }

    public RTApiCommandResult execute()
    {
        return getClient().execute(this);
    }

    public void execute(final RTApiCommandAsyncCallback callback)
    {
        new AsyncTask<RTApiCommandAsyncCallback, String, Object>()
        {
            protected void onPreExecute()
            {
                onPreAsyncExecute();
                callback.onBeforeCommandStart(RTApiCommand.this);
            }

            protected void onPostExecute(final Object result)
            {
                if (result instanceof RTApiCommandResult)
                {
                    onPostAsyncExecute((RTApiCommandResult) result);
                    callback.onCommandCompleted((RTApiCommandResult) result);
                } else
                {
                    if (result instanceof RTApiError)
                    {
                        onAsyncExecutionError((RTApiError) result);
                        callback.onCommandFailed((RTApiError) result);
                    } else
                    {
                        final RTApiError error = new RTApiError((Throwable) result);
                        onAsyncExecutionError(error);
                        callback.onCommandFailed(error);
                    }
                }
            }

            protected Object doInBackground(final RTApiCommandAsyncCallback... callbacks)
            {
                try
                {
                    return RTApiCommand.this.execute();
                } catch (Throwable err)
                {
                    return err;
                }
            }
        }.execute(callback);
    }

    public void onPreAsyncExecute()
    {

    }

    public void onPostAsyncExecute(RTApiCommandResult result)
    {

    }

    public void onAsyncExecutionError(RTApiError error)
    {

    }

    public int getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(final int retryCount)
    {
        this.retryCount = retryCount;
    }
}
