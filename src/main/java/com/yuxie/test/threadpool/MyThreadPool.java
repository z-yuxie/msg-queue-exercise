package com.yuxie.test.threadpool;

import java.util.concurrent.*;

/**
 * @author 147356
 * @create 2018-05-06 20:02
 * @desc 线程池
 **/
public class MyThreadPool extends ThreadPoolExecutor {

    private static class MyThreadPoolHolder {
        private static final MyThreadPool INSTANCE = new MyThreadPool();
    }

    public static MyThreadPool getInstance() {
        return MyThreadPoolHolder.INSTANCE;
    }

    private MyThreadPool() {
        super(5, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5));
    }
}
