package eu.livotov.labs.android.robotools.services.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 21.03.13
 */
public abstract class RTDownloadService<P extends RTDownloadTask> extends Service implements Runnable
{

    private Queue<P> queue = new ConcurrentLinkedQueue<P>();
    private P currentTask;
    private final static int ONGOING_NOTIFICATION_ID = 1;
    private Thread downloadThread;
    private Handler uiHandler;
    private NotificationManager notificationManager;

    public IBinder onBind(final Intent intent)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        uiHandler = new Handler();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (Commands.Download.equalsIgnoreCase(intent.getAction()))
        {
            if (intent.hasExtra(Extras.DownloadId))
            {
                P payload = createNewTask(intent.getStringExtra(Extras.DownloadId));
                queue.add(payload);
            }

            if (downloadThread == null)
            {
                downloadThread = new Thread(this);
                downloadThread.start();
            }

            return START_STICKY;
        }

        if (Commands.Cancel.equalsIgnoreCase(intent.getAction()))
        {
            if (intent != null && intent.hasExtra(Extras.DownloadId))
            {
                P task = findDownloadTask(intent.getStringExtra(Extras.DownloadId));
                cancelTask(task);
                return START_STICKY;
            }
        }

        if (Commands.CancelAll.equalsIgnoreCase(intent.getAction()))
        {
            cancelAll();
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    private P findDownloadTask(final String id)
    {
        Iterator<P> tasks = queue.iterator();

        if (currentTask != null && id.equalsIgnoreCase(currentTask.getDownloadId()))
        {
            return currentTask;
        }

        try
        {
            while (tasks.hasNext())
            {
                P task = tasks.next();
                if (id.equalsIgnoreCase(task.getDownloadId()))
                {
                    return task;
                }
            }
        } catch (ConcurrentModificationException cme)
        {
            return findDownloadTask(id);
        }

        return null;
    }

    private void cancelTask(final P task)
    {
        if (task != null && task.status == RTDownloadStatus.Downloading)
        {
            task.status = RTDownloadStatus.Cancelling;
            updateNotifications();
        }
    }

    private void cancelAll()
    {
        Iterator<P> tasks = queue.iterator();
        while (tasks.hasNext())
        {
            tasks.next().status = RTDownloadStatus.Cancelling;
        }
        updateNotifications();
    }

    private void updateNotifications()
    {
        if (currentTask!=null)
        {
            startForeground(ONGOING_NOTIFICATION_ID, buildNotification(currentTask));
        } else
        {
            stopForeground(true);
        }
    }

    public void run()
    {
        while (queue.peek() != null)
        {
            P downloadTask = queue.poll();

            if (downloadTask != null)
            {
                try
                {
                    processSingleDownload(downloadTask);

                    switch (downloadTask.status)
                    {
                        case Finished:
                            finishDownload(downloadTask);
                            break;

                        case Cancelled:
                        case Cancelling:
                            downloadTask.status = RTDownloadStatus.Cancelled;
                            notifyDownloadCancelled(downloadTask);
                            break;

                        case Failed:
                            failDownload(downloadTask, new Exception("Internal Silent Fail. Look at your download code."));
                            break;
                    }

                } catch (Throwable err)
                {
                    Log.e(RTDownloadService.class.getSimpleName(), "Download error: " + downloadTask.getDownloadUrl() + " : " + err.getMessage(), err);
                    failDownload(downloadTask, err);
                } finally
                {
                    updateNotifications();
                    currentTask = null;
                }
            }
        }

        stopSelf();
        downloadThread = null;
    }

    private void notifyDownloadCancelled(final P task)
    {
        uiHandler.post(new Runnable()
        {
            public void run()
            {
                onDownloadCompleted(task);
            }
        });
    }

    private void failDownload(final P task, final Throwable err)
    {
        uiHandler.post(new Runnable()
        {
            public void run()
            {
                onDownloadFailed(task, err);
            }
        });
    }

    private void finishDownload(final P task)
    {
        uiHandler.post(new Runnable()
        {
            public void run()
            {
                onDownloadCompleted(task);
            }
        });
    }

    private void processSingleDownload(final P job) throws Throwable
    {
        currentTask = job;
        currentTask.targetFile = getLocationOnDevice(currentTask);
        currentTask.status = RTDownloadStatus.Downloading;

        updateNotifications();

        uiHandler.post(new Runnable()
        {
            public void run()
            {
                onDownloadStarted(currentTask);
            }
        });

        URL url = new URL(currentTask.getDownloadUrl());
        URLConnection urlConn = url.openConnection();
        urlConn.setUseCaches(false);
        urlConn.setConnectTimeout(4000);

        currentTask.contentType = urlConn.getContentType();
        final int contentLength = urlConn.getContentLength();
        currentTask.contentLength = contentLength > 0 ? contentLength : currentTask.getDownloadSize();

        verifyStream(currentTask, urlConn);

        FileOutputStream writer = new FileOutputStream(currentTask.targetFile);
        InputStream inStream = urlConn.getInputStream();
        byte[] buffer = new byte[1024];
        int count = 0;
        long lastNotificationUpdateTime = 0;
        currentTask.bytesRead = 0;

        while (inStream != null && -1 != (count = inStream.read(buffer)))
        {
            if (currentTask.status != RTDownloadStatus.Downloading)
            {
                break;
            }

            currentTask.bytesRead += count;

            writer.write(buffer, 0, count);

            if (System.currentTimeMillis() - lastNotificationUpdateTime > 3000)
            {
                updateNotifications();
                lastNotificationUpdateTime = System.currentTimeMillis();
            }
        }

        writer.close();
        inStream.close();

        if (currentTask.status != RTDownloadStatus.Downloading)
        {
            currentTask.targetFile.delete();
        } else
        {
            currentTask.status = RTDownloadStatus.Postprocessing;
            updateNotifications();
            performDownloadPostprocess(currentTask);
            currentTask.status = RTDownloadStatus.Finished;
        }

        currentTask = null;
    }

    private Notification buildNotification(P task)
    {
        final String userDefinedTitle = getDownloadNotificationTitle(task);
        final String userDefinedFooter = getDownloadNotificationFooter(task);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                                                     .setSmallIcon(getNotificationSmallIconResource(task))
                                                     .setContentTitle(TextUtils.isEmpty(userDefinedTitle) ? task.getDownloadName() : userDefinedTitle)
                                                     .setContentText(TextUtils.isEmpty(userDefinedFooter) ? task.getDownloadDescription() : userDefinedFooter)
                                                     .setTicker(task.getDownloadName());

        final Bitmap notificationPreviewBitmap = getNotificationPreviewBitmap(task);
        if (notificationPreviewBitmap != null)
        {
            builder.setLargeIcon(notificationPreviewBitmap);
        }

        switch (task.status)
        {
            case Downloading:
                setupDownloadPhaseNotificationOptions(task, builder);
                break;

            case Cancelling:
                setupDownloadBeingCancelledNotificationOptions(task, builder);
                break;

            case Postprocessing:
                setupDownloadPostprocessingNotificationOptions(task, builder);
                break;
        }

        return builder.build();
    }

    private NotificationCompat.Builder setupDownloadPostprocessingNotificationOptions(final P task, final NotificationCompat.Builder builder)
    {
        final String userDefinedPostProcessingMessage = getDownloadPostprocessingMessage(task);

        builder.setProgress(100,0,true);
        builder.setContentText(TextUtils.isEmpty(userDefinedPostProcessingMessage) ? "Processing..." : userDefinedPostProcessingMessage);
        return builder;
    }

    private NotificationCompat.Builder setupDownloadBeingCancelledNotificationOptions(final P task, final NotificationCompat.Builder builder)
    {
        final String userDefinedCancellingMessage = getDownloadBeingCancelledMessage(task);

        builder.setProgress(100,0,true);
        builder.setContentText(TextUtils.isEmpty(userDefinedCancellingMessage) ? "Cancelling..." : userDefinedCancellingMessage);
        return builder;
    }

    private NotificationCompat.Builder setupDownloadPhaseNotificationOptions(final P task, final NotificationCompat.Builder builder)
    {
        int progress = computeDownloadProgress(task);
        builder.setProgress(100, progress >= 0 ? progress : 0, progress <= 0);

        if (task.isCancellable())
        {
            final String userDefinedCancelButtonText = getNotificationCancelActionText(task);
            Intent cancelDownloadIntent = new Intent(this, this.getClass()).setAction(Commands.Cancel).putExtra(Extras.DownloadId, task.getDownloadId());
            PendingIntent cancelDownloadPendingIntent = PendingIntent.getService(this, 0, cancelDownloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(getNotificationCancelIconResource(task), TextUtils.isEmpty(userDefinedCancelButtonText) ? "Cancel" : userDefinedCancelButtonText, cancelDownloadPendingIntent);
        }

        return builder;
    }

    private int computeDownloadProgress(final P task)
    {
        if (task.contentLength > 0 && task.targetFile != null)
        {
            return (int) Math.round(((float) task.bytesRead * 100) / task.contentLength);
        } else
        {
            return -1;
        }
    }

    private int getNotificationSmallIconResource(final P task)
    {
        if (Build.VERSION.SDK_INT < 11)
        {
            return getNotificationIconPreIcsResource(task);
        } else
        {
            return getNotificationIconIcsResource(task);
        }
    }

    private Bitmap getNotificationPreviewBitmap(final P task)
    {
        if (Build.VERSION.SDK_INT < 11)
        {
            return null;
        } else
        {
            return getLargeIconIcsBitmap(task);
        }
    }

    protected abstract P createNewTask(final String id);

    protected abstract Bitmap getLargeIconIcsBitmap(final P task);

    protected abstract int getNotificationIconPreIcsResource(final P task);

    protected abstract int getNotificationIconIcsResource(final P task);

    protected abstract String getNotificationCancelActionText(final P task);

    protected abstract String getDownloadPostprocessingMessage(final P task);

    protected abstract String getDownloadBeingCancelledMessage(final P task);

    protected abstract int getNotificationCancelIconResource(final P task);

    protected abstract void performDownloadPostprocess(P task);

    protected abstract void onDownloadStarted(P task);

    protected abstract void onDownloadCompleted(P task);

    protected abstract void onDownloadFailed(P task, Throwable err);

    protected abstract void onDownloadCancelled(P task);

    protected abstract File getLocationOnDevice(P task);

    protected abstract String getDownloadNotificationTitle(P task);

    protected abstract String getDownloadNotificationFooter(P task);

    protected abstract void verifyStream(P task, URLConnection connection) throws Exception;

    public final static class Commands
    {

        public final static String Download = "download";
        public final static String Cancel = "cancel";
        public final static String CancelAll = "cancelAll";
    }

    public final static class Extras
    {

        public final static String DownloadId = "downloadId";
    }


}
