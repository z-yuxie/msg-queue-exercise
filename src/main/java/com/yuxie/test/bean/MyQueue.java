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
    private Queue<String> msgQueue;
    /**
     * 订阅者记录
     */
    private Set<String> subcriberRecord;
    /**
     * 是否已经拥有消息推送线程
     */
    private boolean hasPushThread;

    private Lock lock = new ReentrantLock();

    private Condition queueIsEmpty = lock.newCondition();

    public MyQueue(String queueName) {
        this.queueName = queueName;
        this.msgQueue = new LinkedBlockingQueue<>();
        this.subcriberRecord = new HashSet<>();
        this.hasPushThread = false;
    }

    /**
     * 退订队列
     * @param subcriberName 订阅者名称
     * @return 是否退订成功
     */
    public boolean unsubscribe(String subcriberName) {
        try {
            if (!subcriberRecord.contains(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 未订阅队列:" + queueName);
                return false;
            }
            lock.lock();
            if (!subcriberRecord.contains(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 未订阅队列:" + queueName);
                return false;
            }
            subcriberRecord.remove(subcriberName);
            lock.unlock();
            return true;
        } catch (Exception e) {
            System.out.println("订阅者:" + subcriberName + " 退订队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 订阅队列
     * @param subcriberName 订阅者名称
     * @return 是否订阅成功
     */
    public boolean subscribe(String subcriberName) {
        try {
            if (subcriberRecord.contains(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 已经订阅了队列:" + queueName);
                return false;
            }
            lock.lock();
            if (subcriberRecord.contains(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 已经订阅了队列:" + queueName);
                return false;
            }
            subcriberRecord.add(subcriberName);
            lock.unlock();
            if (!msgQueue.isEmpty()) {
                System.out.println("订阅者:" + subcriberName + "新订阅队列:" + queueName
                        + " 时发现该队列中有待推送的消息,触发消息推送");
                pushMsgs();
            }
            return true;
        } catch (Exception e) {
            System.out.println("订阅者:" + subcriberName + " 新订阅队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向当前队列添加消息
     * @param msg 消息内容
     * @return 是否添加成功
     */
    public boolean addMsg(String msg) {
        try {
            boolean flag = msgQueue.offer(msg);
            if (flag) {
                System.out.println("成功向队列:" + queueName + " 中添加了一个新消息:" + msg);
                //添加成功,开始进行相应的处理
                pushMsgs();
            }
            return flag;
        } catch (Exception e) {
            System.out.println("向当前队列" + queueName + " 添加消息:" + msg + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向该队列的订阅者们推送消息
     */
    public void pushMsgs() {
        try {
            if (hasPushThread || subcriberRecord.isEmpty()) {
                //如果该队列的推送线程正在执行,或者当前该队列的订阅者为空,则直接返回
                return;
            }
            lock.lock();
            if (hasPushThread || subcriberRecord.isEmpty()) {
                lock.unlock();
                return;
            } else {
                hasPushThread = true;
                lock.unlock();
            }
            //这个线程是否有必要让他被创建后一直存在，然后在使用时唤醒它？
            MyThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("消息队列:" + queueName + " 的消息推送任务开始运行");
                    while (true) {
                        String msg = null;
                        //为何网上大多数代码均是先取出,然后再删除,而不是取出的同时便删除头元素？
                        //是否需要每个消息都单开一个线程？
                        if ((msg = msgQueue.poll()) != null) {
                            System.out.println("消息队列:" + queueName + " 开始向其订阅者们推送消息:" + msg);
                            pushMsgToSubcriber(msg);
                            System.out.println("消息队列:" + queueName + " 向其订阅者们推送消息:" + msg + " 结束");
                        } else {
                            lock.lock();
                            hasPushThread = false;
                            lock.unlock();
                            System.out.println("消息队列:" + queueName + " 的消息推送任务运行结束");
                            return;
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("消息队列:" + queueName + "推送消息时发生异常,异常信息为：");
            e.printStackTrace();
        }
    }

    /**
     * 推送消息给当前队列的订阅者们
     * @param msg 要推送的消息内容
     */
    private void pushMsgToSubcriber(String msg) {
        for (String subcriberName : subcriberRecord) {
            SubcriberModel.getInstance().receiveMsg(subcriberName , msg);
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
