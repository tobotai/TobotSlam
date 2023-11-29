package com.tobot.map.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理
 *
 * @author houdeming
 * @date 2020/12/29
 */
public class ThreadPoolManager {
    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 10;
    private static final long TIME = 5000L;
    private final ThreadPoolExecutor mThreadPool;

    private ThreadPoolManager() {
        mThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, TIME, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10));
    }

    private static class ThreadPoolManagerHolder {
        private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

    public static ThreadPoolManager getInstance() {
        return ThreadPoolManagerHolder.INSTANCE;
    }

    public void execute(Runnable runnable) {
        if (runnable != null && mThreadPool != null) {
            // 在某些情况下，线程池可能无法接受新的任务，并抛出RejectedExecutionException异常
            try {
                mThreadPool.execute(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancel(Runnable runnable) {
        if (runnable != null && mThreadPool != null && !mThreadPool.isShutdown() && !mThreadPool.isTerminated()) {
            try {
                mThreadPool.remove(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelAll() {
        if (mThreadPool != null && !mThreadPool.isShutdown() && !mThreadPool.isTerminated()) {
            try {
                mThreadPool.getQueue().clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
