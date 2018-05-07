package com.yuxie.test.bean;

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
 * @create 2018-05-06 23:18
 * @desc 订阅者类
 **/
public class Subcriber {
    /**
     * 订阅者名称
     */
    private String subcriberName;
    /**
     * 已订阅的队列名称记录
     * 主动拉取消息时使用
     */
    private Set<String> myQueueSet;
    /**
     * 待处理消息队列
     */
    private Queue<String> msgQueue;
    /**
     * 是否已有正在执行中的消息处理线程
     * 也可作为当前订阅者是否处在活跃状态的判断标识
     */
    private boolean hasPushThread;

    private Lock lock = new ReentrantLock();

    private Condition queueIsEmpty = lock.newCondition();

    public Subcriber(String subcriberName) {
        this.subcriberName = subcriberName;
        this.myQueueSet = new HashSet<>();
        this.msgQueue = new LinkedBlockingQueue<>();
        this.hasPushThread = false;
    }

    /**
     * 从该订阅者订阅的队列记录中移除某个订阅队列的记录
     * @param queueName 队列名称
     * @return 是否移除成功
     */
    public boolean removeQueueSubcriberRecord(String queueName) {
        myQueueSet.remove(queueName);
        return true;
    }

    /**
     * 增加该订阅者订阅的队列记录
     * @param queueName 队列名称
     * @return 是否添加成功
     */
    public boolean addQueueSubcriberRecord(String queueName) {
        myQueueSet.add(queueName);
        return true;
    }

    /**
     * 向当前订阅者的待处理消息队列添加消息
     * @param msg 消息内容
     * @return 是否添加成功
     */
    public boolean receiveMsg(String msg) {
        try {
            boolean flag = msgQueue.offer(msg);
            if (flag) {
                //添加成功,开始进行相应的处理
                dealMsgs();
            }
            return flag;
        } catch (Exception e) {
            System.out.println("向当前订阅者" + subcriberName + " 添加待处理消息:" + msg + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向该队列的订阅者们推送消息
     */
    private void dealMsgs() {
        try {
            if (hasPushThread) {
                //如果该队列的推送线程正在执行则直接返回
                return;
            }
            lock.lock();
            if (!hasPushThread) {
                hasPushThread = true;
                lock.unlock();
            } else {
                lock.unlock();
                return;
            }
            //这个线程是否有必要让他被创建后一直存在，然后在使用时唤醒它？
            MyThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String msg = null;
                        //为何网上大多数代码均是先取出,然后再删除,而不是取出的同时便删除头元素？
                        if ((msg = msgQueue.poll()) != null) {
                            try {
                                System.out.println("订阅者:" + subcriberName + " 从待处理消息队列中取出一个消息:"
                                        + msg + " 进行处理....");
                                Thread.sleep(1000);
                                System.out.println("订阅者:" + subcriberName + " 对消息:"
                                        + msg + " 的处理结束");
                            } catch (Exception e) {
                                System.out.println("订阅者:" + subcriberName + " 处理消息:"
                                        + msg + " 时发生异常,异常信息为:");
                                e.printStackTrace();
                                System.out.println("订阅者:" + subcriberName + " 对处理失败的消息:"
                                        + msg + " 进行保存及其他操作");
                            }
                        } else {
                            lock.lock();
                            hasPushThread = false;
                            lock.unlock();
                            System.out.println("订阅者:" + subcriberName + " 已暂时处理完到当前为止收到的所有消息,当前时间为:"
                                    + System.currentTimeMillis());
                            return;
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("推送消息时发生异常,异常信息为：");
            e.printStackTrace();
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
        Subcriber subcriber = (Subcriber) o;
        return Objects.equals(subcriberName, subcriber.subcriberName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subcriberName);
    }
}
