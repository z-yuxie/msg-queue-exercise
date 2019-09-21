package com.yuxie.test.bean;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 147356
 * @create 2018-05-06 23:18
 * @desc 订阅者类，此类中暂未使用codition进行线程通信控制
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
    private LinkedBlockingQueue<String> msgQueue;
    /**
     * 处理待处理消息队列中的消息的线程
     */
    private Thread dealMsgThread;

    private Lock lock = new ReentrantLock();

    private Condition queueIsEmpty = lock.newCondition();

    public Subcriber(String subcriberName) {
        this.subcriberName = subcriberName;
        this.myQueueSet = new HashSet<>();
        this.msgQueue = new LinkedBlockingQueue<>();
        this.dealMsgThread = new Thread(createDealMsgTask());
        this.dealMsgThread.start();
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
            if (msgQueue.offer(msg)) {
                //添加成功,开始进行相应的处理
                System.out.println("订阅者:" + subcriberName + " 成功接收并保存了一个消息:" + msg);
                return true;
            }
            System.out.println("订阅者:" + subcriberName + " 保存接收到的消息:" + msg + " 失败");
        } catch (Exception e) {
            System.out.println("订阅者" + subcriberName + " 保存接收到的消息:" + msg + " 时发生异常,异常信息为:");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 中断消息处理工作线程
     * @return 是否已成功告知线程中断
     */
    public boolean shutDownDealMsgThread() {
        try {
            dealMsgThread.interrupt();
            return dealMsgThread.isInterrupted() || !dealMsgThread.isAlive();
        } catch (Exception e) {
            System.out.println("中断订阅者:" + subcriberName + " 的消息处理线程失败");
            return false;
        }
    }

    /**
     * 创建消息处理任务
     * @return 属于当前订阅者的消息处理任务
     */
    private Runnable createDealMsgTask() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("订阅者:" + subcriberName + " 的消息处理任务开始运行");
                while (true) {
                    String msg = null;
                    try {
                        msg = msgQueue.take();
                        System.out.println("订阅者:" + subcriberName + " 从待处理消息队列中取出一个消息:"
                                + msg + " 进行处理....");
                        Thread.sleep(1000);
                        System.out.println("订阅者:" + subcriberName + " 对消息:"
                                + msg + " 的处理结束");
                        msgQueue.remove(msg);
                    } catch (InterruptedException e) {
                        System.out.println("订阅者:" + subcriberName + " 的消息处理线程被中断!");
                        return;
                    } catch (Exception e) {
                        System.out.println("订阅者:" + subcriberName + " 处理消息:"
                                + msg + " 时发生异常,异常信息为:");
                        e.printStackTrace();
                        System.out.println("订阅者:" + subcriberName + " 对处理失败的消息:"
                                + msg + " 进行保存及其他操作");
                    }
                }
            }
        };
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
