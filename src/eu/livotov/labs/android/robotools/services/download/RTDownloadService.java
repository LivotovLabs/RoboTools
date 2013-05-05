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
import eu.livotov.labs.android.robotools.net.RTHTTPClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 21.03.13
 */
public abstract class RTDownloadService<P extends RTDownloadTask> extends Service implements Runnable
{

    private final static int ONGOING_NOTIFICATION_ID = 1;
    private Queue<P> queue = new ConcurrentLinkedQueue<P>();
    private P currentTask;
    private Thread downloadThread;
    private Handler uiHandler;
    private NotificationManager notificationManager;
    private RTHTTPClient http = new RTHTTPClient();

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
        if (intent != null && intent.getAction() != null)
        {
            if (Commands.Download.equalsIgnoreCase(intent.getAction()))
            {
                if (intent.hasExtra(Extras.DownloadId))
                {
                    P payload = createNewTask(intent.getStringExtra(Extras.DownloadId));
                    addDownloadTask(payload);
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
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    public void addDownloadTask(P payload)
    {
        if (findDownloadTask(payload.getDownloadId()) == null)
        {
            queue.add(payload);
        } else
        {
            updateNotifications();
        }

        if (downloadThread == null)
        {
            downloadThread = new Thread(this);
            downloadThread.start();
        }
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

    public void cancelTask(final P task)
    {
        if (task != null && task.status == RTDownloadStatus.Downloading)
        {
            task.status = RTDownloadStatus.Cancelling;
            updateNotifications();
        }
    }

    public void cancelAll()
    {
        queue.clear();
        cancelTask(getCurrentTask());
        updateNotifications();
    }

    public Collection<P> getDownloadQueue()
    {
        List<P> tasks = new ArrayList<P>();
        tasks.addAll(queue);
        return tasks;
    }

    public P getCurrentTask()
    {
        return currentTask;
    }

    private void updateNotifications()
    {
        if (currentTask != null)
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
                    boolean retryDownload = true;

                    while (retryDownload)
                    {
                        final String url = downloadTask.getDownloadUrl();

                        if (TextUtils.isEmpty(url))
                        {
                            Log.e(RTDownloadService.class.getSimpleName(), String.format("No more mirrors left for task %s, failing download.", downloadTask.getDownloadId()));
                            downloadTask.status = RTDownloadStatus.Failed;
                            retryDownload = false;
                        } else
                        {
                            try
                            {
                                processSingleDownload(downloadTask, url);
                                retryDownload = false;
                            } catch (Throwable err)
                            {
                                retryDownload = downloadTask.supportsMirrors();
                                if (!retryDownload)
                                {
                                    downloadTask.status = RTDownloadStatus.Failed;
                                } else
                                {
                                    Log.e(RTDownloadService.class.getSimpleName(), String.format("Failed download for task %s , url %s , will  now try another mirror.", downloadTask.getDownloadId(), url));
                                }
                            }
                        }
                    }

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
                onDownloadCancelled(task);
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

    private void processSingleDownload(final P job, final String urlString) throws Throwable
    {
        currentTask = job;
        currentTask.targetFile = getLocationOnDevice(currentTask);
        currentTask.status = RTDownloadStatus.Downloading;

        http.getConfiguration().setHttpConnectionTimeout(job.getHttpConnectionTimeoutMs());
        http.getConfiguration().setHttpDataResponseTimeout(job.getHttpDataResponseTimeoutMs());
        http.getConfiguration().setEnableGzipCompression(true);

        updateNotifications();

        uiHandler.post(new Runnable()
        {
            public void run()
            {
                onDownloadStarted(currentTask);
            }
        });


        HttpResponse response = http.executeGetRequest(urlString);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null && status != 200)
        {
            throw new Exception("server returned error, status: " + status);
        }

        currentTask.contentType = resEntity.getContentType().getValue();
        final long contentLength = resEntity.getContentLength();
        currentTask.contentLength = contentLength > 0 ? contentLength : currentTask.getDownloadSize();

        verifyStream(currentTask, response);

        FileOutputStream writer = new FileOutputStream(currentTask.targetFile);
        InputStream inStream = resEntity.getContent();
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
                onDownloadProgressUpdate(currentTask);
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
        final int queueSize = queue.size();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                                                     .setSmallIcon(getNotificationSmallIconResource(task))
                                                     .setContentTitle(TextUtils.isEmpty(userDefinedTitle) ? task.getDownloadName() : userDefinedTitle)
                                                     .setContentText(TextUtils.isEmpty(userDefinedFooter) ? task.getDownloadDescription() : userDefinedFooter)
                                                     .setWhen(0)
                                                     .setContentInfo(queueSize > 0 ? ("" + queue.size()) : "")
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

        if (Build.VERSION.SDK_INT < 14)
        {
            // Androids <4.0 does not support notifications with empty content intent
            PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent("empty"), 0);
            builder.setContentIntent(contentIntent);
        }

        return builder.build();
    }

    private NotificationCompat.Builder setupDownloadPostprocessingNotificationOptions(final P task, final NotificationCompat.Builder builder)
    {
        final String userDefinedPostProcessingMessage = getDownloadPostprocessingMessage(task);

        builder.setProgress(100, 0, true);
        builder.setContentText(TextUtils.isEmpty(userDefinedPostProcessingMessage) ? "Processing..." : userDefinedPostProcessingMessage);
        return builder;
    }

    private NotificationCompat.Builder setupDownloadBeingCancelledNotificationOptions(final P task, final NotificationCompat.Builder builder)
    {
        final String userDefinedCancellingMessage = getDownloadBeingCancelledMessage(task);

        builder.setProgress(100, 0, true);
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
            final String userDefinedCancelAllButtonText = getNotificationCancelAllActionText(task);

            Intent cancelDownloadIntent = new Intent(this, this.getClass()).setAction(Commands.Cancel).putExtra(Extras.DownloadId, task.getDownloadId());
            Intent cancelAllDownloadIntent = new Intent(this, this.getClass()).setAction(Commands.CancelAll);
            Intent cancelDownloadIntentViaSwipeOut = new Intent(this, this.getClass()).setAction(Commands.Cancel).putExtra(Extras.DownloadId, task.getDownloadId());

            PendingIntent cancelDownloadPendingIntent = PendingIntent.getService(this, 0, cancelDownloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cancelAllDownloadPendingIntent = PendingIntent.getService(this, 0, cancelAllDownloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cancelDownloadPendingIntentVisSwipeOut = PendingIntent.getService(this, 0, cancelDownloadIntentViaSwipeOut, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addAction(getNotificationCancelIconResource(task), TextUtils.isEmpty(userDefinedCancelButtonText) ? "Cancel" : userDefinedCancelButtonText, cancelDownloadPendingIntent);
            if (getDownloadQueue().size() > 0)
            {
                builder.addAction(getNotificationCancelAllIconResource(task), TextUtils.isEmpty(userDefinedCancelAllButtonText) ? "Cancel All" : userDefinedCancelAllButtonText, cancelAllDownloadPendingIntent);
            }

            builder.setDeleteIntent(cancelDownloadPendingIntentVisSwipeOut);
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

    protected abstract String getNotificationCancelAllActionText(final P task);

    protected abstract String getDownloadPostprocessingMessage(final P task);

    protected abstract String getDownloadBeingCancelledMessage(final P task);

    protected abstract int getNotificationCancelIconResource(final P task);

    protected abstract int getNotificationCancelAllIconResource(final P task);

    protected abstract void performDownloadPostprocess(P task);

    protected abstract void onDownloadStarted(P task);

    protected abstract void onDownloadCompleted(P task);

    protected abstract void onDownloadProgressUpdate(P task);

    protected abstract void onDownloadFailed(P task, Throwable err);

    protected abstract void onDownloadCancelled(P task);

    protected abstract File getLocationOnDevice(P task);

    protected abstract String getDownloadNotificationTitle(P task);

    protected abstract String getDownloadNotificationFooter(P task);

    protected abstract void verifyStream(P task, HttpResponse connection) throws Exception;

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
