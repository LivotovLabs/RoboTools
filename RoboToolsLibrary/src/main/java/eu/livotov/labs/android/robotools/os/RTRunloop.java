package eu.livotov.labs.android.robotools.os;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Messagq queue enchanced thread. Mainly used for RTAsyncTask but
 * can be adopted to other things as well.
 */
@SuppressWarnings("unused")
public class RTRunloop extends Thread {

    private Handler mHandler;
    private final Object mLock = new Object();
    private boolean mReady = false;

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new Handler();
        synchronized (mLock) {
            this.mLock.notifyAll();
            mReady = true;
        }
        Looper.loop();
    }

    /**
     * Adds message into the serial queue for processing
     */
    public void sendMessage(Message message) {
        sendMessage(message, 0);
    }

    /**
     * Adds runnable into the queue for serial execution
     */
    public void post(Runnable runnable) {
        post(runnable, 0);
    }

    /**
     * Adds message into the queue for processing with the specified delay (in ms)
     */
    public void sendMessage(Message message, long delayMillis) {
        synchronized (mLock) {
            while(!mReady) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mHandler != null) {
                if (delayMillis <= 0) {
                    this.mHandler.sendMessage(message);
                } else {
                    mHandler.sendMessageDelayed(message, delayMillis);
                }
            }
        }
    }

    /**
     * Adds runnable into the queue for serial execution with the specified delay (in ms)
     */
    public void post(Runnable runnable, long delayMillis) {
        synchronized (mLock) {

            while(!mReady) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (mHandler != null) {
                if (delayMillis <= 0) {
                    this.mHandler.post(runnable);
                } else {
                    mHandler.postDelayed(runnable, delayMillis);
                }
            }
        }
    }

    public boolean isStarted() {
        return mReady;
    }
}
