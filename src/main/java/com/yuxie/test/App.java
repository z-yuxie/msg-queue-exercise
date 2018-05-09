package com.yuxie.test;

import com.sun.org.apache.bcel.internal.ExceptionConstants;

import java.util.concurrent.*;

/**
 * Hello world!
 * @author 147356
 */
public class App {

    private volatile boolean flag = true;

    public Thread thread1 = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                System.out.println("线程1进入睡眠");
                TimeUnit.SECONDS.sleep(2L);
                System.out.println("线程1睡眠结束,开始变更标识状态");
                flag = false;
            } catch (InterruptedException e) {
                System.out.println("........线程1出现异常");
                e.printStackTrace();
            }
        }
    });

    public Thread thread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    if (!flag && thread2.isInterrupted()) {
                        System.out.println("线程2准备进入睡眠");
                        TimeUnit.SECONDS.sleep(6L);
                        System.out.println("线程2睡眠结束,线程2准备结束");
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("........线程2出现异常" + e.getMessage());
                e.printStackTrace();
            }
        }
    });

    public static void main(String[] args) throws InterruptedException {
        App app = new App();
        app.thread1.start();
        app.thread2.start();
        TimeUnit.SECONDS.sleep(1L);
        app.thread2.interrupt();
        TimeUnit.SECONDS.sleep(10L);
    }
}