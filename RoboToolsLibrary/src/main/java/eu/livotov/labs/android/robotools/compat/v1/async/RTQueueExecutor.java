package eu.livotov.labs.android.robotools.compat.v1.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 30/05/2013
 */
public class RTQueueExecutor extends ThreadPoolExecutor
{

    public static RTQueueExecutor create(int poolSize)
    {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        return new RTQueueExecutor(poolSize, poolSize + 1, 30, TimeUnit.SECONDS, queue);
    }

    public static RTQueueExecutor create(int minPoolSize, int maxPoolSize, long threadKeepAlive, TimeUnit threadKeepAliveTimeUnit)
    {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        return new RTQueueExecutor(minPoolSize, maxPoolSize > minPoolSize ? maxPoolSize : (minPoolSize + 1), threadKeepAlive, TimeUnit.SECONDS, queue);
    }

    protected RTQueueExecutor(int poolSizeMin, int poolSizeMax, long keepALive, TimeUnit unit, BlockingQueue<Runnable> queue)
    {
        super(poolSizeMin, poolSizeMax, keepALive, unit, queue);
    }
}