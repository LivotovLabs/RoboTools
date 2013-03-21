package eu.livotov.labs.android.robotools.services.download;

import java.io.File;
import java.io.Serializable;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 21.03.13
 */
public abstract class RTDownloadTask implements Serializable
{
    RTDownloadStatus status = RTDownloadStatus.Idle;

    File targetFile;
    public long bytesRead;
    public long contentLength;
    public String contentType;

    public abstract String getDownloadId();

    public abstract String getDownloadUrl();

    public abstract String getDownloadName();

    public abstract String getDownloadDescription();

    public abstract long getDownloadSize();

    public abstract boolean isCancellable();

    public RTDownloadStatus getStatus()
    {
        return status;
    }

    public File getTargetFile()
    {
        return targetFile;
    }

    public long getBytesRead()
    {
        return bytesRead;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public String getContentType()
    {
        return contentType;
    }
}
