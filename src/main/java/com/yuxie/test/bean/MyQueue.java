package com.yuxie.test.bean;

import com.yuxie.test.model.SubcriberModel;
import com.yuxie.test.threadpool.MyThreadPool;

import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 147356
 * @create 2018-05-06 23:20
 * @desc 消息队列类
 **/
public class MyQueue {
    /**
     * 队列名称
     */
    private String queueName;
    /**
     * 消息队列
     */
    private LinkedBlockingQueue<String> msgQueue;
    /**
     * 订阅者记录
     */
    private Set<String> subcriberRecord;
    /**
     * 推送消息队列中的消息的线程
     */
    private Thread pushMsgThread;

    private Lock lock = new ReentrantLock();

    private Condition noSubcriberRecord = lock.newCondition();

    public MyQueue(String queueName) {
        this.queueName = queueName;
        this.msgQueue = new LinkedBlockingQueue<>();
        this.subcriberRecord = new HashSet<>();
        this.pushMsgThread = new Thread(createPushMsgTask());
        this.pushMsgThread.start();
    }

    /**
     * 退订队列
     * @param subcriberName 订阅者名称
     * @return 是否退订成功
     */
    public boolean unsubscribe(String subcriberName) {
        lock.lock();
        try {
            if (!subcriberRecord.contains(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 未订阅队列:" + queueName);
                return false;
            }
            subcriberRecord.remove(subcriberName);
            return true;
        } catch (Exception e) {
            System.out.println("订阅者:" + subcriberName + " 退订队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 订阅队列
     * @param subcriberName 订阅者名称
     * @return 是否订阅成功
     */
    public boolean subscribe(String subcriberName) {
        lock.lock();
        try {
            if (subcriberRecord.contains(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 已经订阅了队列:" + queueName);
                return false;
            }
            if (subcriberRecord.add(subcriberName)) {
                noSubcriberRecord.signal();
                return true;
            }
            System.out.println("订阅者:" + subcriberName + " 订阅队列:" + queueName + " 失败");
        } catch (Exception e) {
            System.out.println("订阅者:" + subcriberName + " 新订阅队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * 向当前队列添加消息
     * @param msg 消息内容
     * @return 是否添加成功
     */
    public boolean addMsg(String msg) {
        try {
            if (msgQueue.offer(msg)) {
                System.out.println("成功向队列:" + queueName + " 中添加了一个新消息:" + msg);
                return true;
            }
            System.out.println("向队列:" + queueName + " 中添加新消息:" + msg + " 失败");
        } catch (Exception e) {
            System.out.println("向当前队列" + queueName + " 添加消息:" + msg + " 时发生异常,异常信息为:");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 中断消息推送工作线程
     * @return 是否已成功告知线程中断
     */
    public boolean shutDownPushMsgThread() {
        try {
            pushMsgThread.interrupt();
            return pushMsgThread.isInterrupted() || !pushMsgThread.isAlive();
        } catch (Exception e) {
            System.out.println("中断队列:" + queueName + " 的消息推送线程失败");
            return false;
        }
    }

    /**
     * 创建消息推送任务
     * @return 属于本队列的消息推送任务
     */
    private Runnable createPushMsgTask() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("消息队列:" + queueName + " 的消息推送任务开始运行");
                while (true) {
                    String msg = null;
                    try {
                        msg = msgQueue.take();
                        if (subcriberRecord.isEmpty()) {
                            lock.lock();
                            noSubcriberRecord.await();
                            lock.unlock();
                        }
                        System.out.println("消息队列:" + queueName + " 开始向其订阅者们推送消息:" + msg);
                        pushMsgToSubcriber(msg);
                        System.out.println("消息队列:" + queueName + " 向其订阅者们推送消息:" + msg + " 结束");
                        msgQueue.remove(msg);
                    } catch (InterruptedException e) {
                        System.out.println("消息队列:" + queueName + " 的消息推送线程被中断!");
                        return;
                    } catch (Exception e) {
                        System.out.println("消息队列" + queueName + " 向订阅者们推送消息时发生异常,异常信息为:");
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 推送消息给当前队列的订阅者们
     * @param msg 要推送的消息内容
     */
    private void pushMsgToSubcriber(String msg) {
        for (String subcriberName : subcriberRecord) {
            try {
                if (SubcriberModel.getInstance().receiveMsg(subcriberName , msg)) {
                    System.out.println("消息队列:" + queueName + " 向订阅者" + subcriberName + " 成功推送了一条消息:" + msg);
                    continue;
                }
                System.out.println("消息队列:" + queueName + " 向订阅者" + subcriberName + " 推送消息:" + msg + " 失败");
            } catch (Exception e) {
                System.out.println("消息队列:" + queueName + " 向订阅者" + subcriberName + " 推送消息:" + msg
                        + " 时发生异常,异常信息为:" + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("消息队列:" + queueName + " 对向订阅者" + subcriberName + " 推送失败的消息:" + msg
                    + " 进行处理......(保存后交由其他任务进行处理)");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MyQueue myQueue = (MyQueue) o;
        return Objects.equals(queueName, myQueue.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueName);
    }
}
