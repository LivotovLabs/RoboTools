package eu.livotov.labs.android.robotools.net;

import android.graphics.Bitmap;

import java.io.File;
import java.io.Serializable;

public abstract class DownloadTask implements Serializable
{
    public final static int DEFAULT_CONNECTION_TIMEOUT_MS = 20000;
    public final static int DEFAULT_DATA_TIMEOUT_MS = 20000;

    DownloadService.Status status = DownloadService.Status.Idle;

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

    public abstract boolean supportsMirrors();

    protected abstract boolean requiresVisibleNotification();

    protected abstract Bitmap getLargeIconIcsBitmap();

    protected abstract int getNotificationIconPreIcsResource();

    protected abstract int getNotificationIconIcsResource();

    protected abstract String getNotificationCancelActionText();

    protected abstract String getNotificationCancelAllActionText();

    protected abstract String getDownloadPostprocessingMessage();

    protected abstract String getDownloadBeingCancelledMessage();

    protected abstract int getNotificationCancelIconResource();

    protected abstract int getNotificationCancelAllIconResource();

    protected abstract void performDownloadPreprocess();

    protected abstract void performDownloadPostprocess();

    protected abstract File createDownloadReceiverFile();

    protected abstract String getDownloadNotificationTitle();

    protected abstract String getDownloadNotificationFooter();

    public int getHttpConnectionTimeoutMs()
    {
        return DEFAULT_CONNECTION_TIMEOUT_MS;
    }

    public int getHttpDataResponseTimeoutMs()
    {
        return DEFAULT_DATA_TIMEOUT_MS;
    }

    public DownloadService.Status getStatus()
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