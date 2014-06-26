package eu.livotov.labs.android.robotools.os;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Представляет собой поток, работающий с очередью сообщений.
 *
 * Отправляемые в поток запросы складываются в единую очередь.
 * Затем в отдельном потоке очередь считывается, и код выполняется.
 *
 * Плюс этого класса в том, что он бережно относится к ресурсам процессора,
 * позволяет выполнять потоки и сообщения с задержкой.
 */
@SuppressWarnings("unused")
public class Runloop extends Thread {

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
     * Добавляет сообщение в очередь для исполнения.
     * Исполнение начнется сразу же после того, как выполнятся
     * предыдущие сообщения и потоки.
     */
    public void sendMessage(Message message) {
        sendMessage(message, 0);
    }

    /**
     * Добавляет поток в очередь для исполнения.
     * Исполнение начнется сразу же после того, как выполнятся
     * предыдущие сообщения и потоки.
     */
    public void post(Runnable runnable) {
        post(runnable, 0);
    }

    /**
     * Добавляет сообщение в очередь для исполнения.
     * Исполнение начнется сразу же после истечения указанного периода задержки.
     * @param delayMillis количество миллисекунд задержки выполнения.
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
     * Добавляет поток в очередь для исполнения.
     * Исполнение начнется сразу же после истечения указанного периода задержки.
     * @param delayMillis количество миллисекунд задержки выполнения.
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
