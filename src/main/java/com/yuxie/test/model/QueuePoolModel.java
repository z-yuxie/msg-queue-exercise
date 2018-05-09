package com.yuxie.test.model;

import com.yuxie.test.bean.MyQueue;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 147356
 * @create 2018-05-03 15:11
 * @desc 一对一型队列消息池,此消息池中存储的均为一对一型消息队列,队列中的消息被任一消费者消费后,该消息将立即失效
 **/
public class QueuePoolModel {

    /**
     * 消息池默认允许的队列数量
     */
    private static final int DEFULT_QUEUE_COUNT = 10;

    /**
     * 消息池中每个队列默认允许的队列长度
     */
    private static final int DEFULT_QUEUE_LENGTH = 10;

    /**
     * 当前消息池允许的队列数量上限
     */
    private final int queueCount;

    /**
     * 消息池中每个队列允许的队列长度上限
     */
    private final int queueLength;

    /**
     * 当对应队列不存在时,是否允许自动创建队列
     */
    private final boolean autoCreateQueue;

    /**
     * 消息队列池
     */
    private Map<String , MyQueue> queuePool;

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    private static class QueuePoolModelHolder {
        private static final QueuePoolModel INSTANCE = new QueuePoolModel();
    }

    public static QueuePoolModel getInstance() {
        return QueuePoolModelHolder.INSTANCE;
    }

    /**
     * 创建默认配置的消息池
     */
    private QueuePoolModel() {
        this.queuePool = new HashMap<>(DEFULT_QUEUE_COUNT);
        this.queueCount = DEFULT_QUEUE_COUNT;
        this.autoCreateQueue = true;
        this.queueLength = DEFULT_QUEUE_LENGTH;
    }

    /**
     * 创建指定大小的消息池
     * @param queueCount 消息池中允许存在的队列数量上限
     * @param autoCreateQueue 当对应队列不存在时,是否允许自动创建队列
     */
    private QueuePoolModel(int queueCount , boolean autoCreateQueue , int queueLength) {
        this.queuePool = new HashMap<>(queueCount);
        this.queueCount = queueCount;
        this.autoCreateQueue = autoCreateQueue;
        this.queueLength = queueLength;
    }

    /**
     * 暂停队列池中所有队列的运行,然后清空队列池
     */
    public void emptyQueuePool() {
        System.out.println("------------开始清空队列池-----------");
        if (queuePool.isEmpty()) {
            return;
        }
        Map<String , MyQueue> poolClone = new HashMap<>(queuePool);
        for (Map.Entry<String , MyQueue> entry : poolClone.entrySet()) {
            if (entry == null || entry.getValue() == null) {
                continue;
            }
            if (entry.getValue().shutDownPushMsgThread()) {
                System.out.println("成功停止队列:" + entry.getKey() + " 的运行,可以进行删除");
                queuePool.remove(entry.getKey());
            } else {
                System.out.println("停止队列:" + entry.getKey() + " 的运行失败,暂时无法进行删除");
            }
        }
        if (queuePool.isEmpty()) {
            System.out.println("--------------队列池清空完成-------------");
        } else {
            System.out.println("--------------队列池未完全清空--------------");
        }
    }

    /**
     * 订阅者退订消息队列
     * @param subcriberName 订阅者名称
     * @param queueName 要订阅的消息队列的名称
     * @return 是否订阅成功
     */
    public boolean unsubscribeQueue(String subcriberName , String queueName) {
        try {
            if (!queuePool.containsKey(queueName)) {
                System.out.println("订阅者" + subcriberName + " 要退订的队列:" + queueName + " 不存在!");
                return false;
            }
            return queuePool.get(queueName).unsubscribe(subcriberName);
        } catch (Exception e) {
            System.out.println("订阅者" + subcriberName + " 退订队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 订阅者订阅消息队列
     * @param subcriberName 订阅者名称
     * @param queueName 要订阅的消息队列的名称
     * @return 是否订阅成功
     */
    public boolean subscribeQueue(String subcriberName , String queueName) {
        try {
            if (!queuePool.containsKey(queueName)) {
                if (!autoCreateQueue) {
                    System.out.println("订阅者" + subcriberName + " 要订阅的队列:" + queueName + " 不存在!");
                    return false;
                }
                MyQueue newQueue = new MyQueue(queueName);
                lock.lock();
                if (!queuePool.containsKey(queueName)) {
                    System.out.println("新创建了一个队列:" + queueName);
                    queuePool.put(queueName , newQueue);
                }
                lock.unlock();
            }
            return queuePool.get(queueName).subscribe(subcriberName);
        } catch (Exception e) {
            System.out.println("订阅者" + subcriberName + " 订阅队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 接收发布者发布的消息
     * @param publisherName 发布者名称
     * @param msg 消息内容
     * @param queueName 队列名称
     * @return 发布结果
     */
    public boolean push(String publisherName , String msg , String queueName) {
        try {
            if (!queuePool.containsKey(queueName)) {
                if (!autoCreateQueue) {
                    System.out.println("发布者:" + publisherName + " :要发布到的队列:" + queueName + " 不存在!");
                    return false;
                }
                MyQueue newQueue = new MyQueue(queueName);
                lock.lock();
                if (!queuePool.containsKey(queueName)) {
                    System.out.println("新创建了一个队列:" + queueName);
                    queuePool.put(queueName , newQueue);
                }
                lock.unlock();
            }
            return queuePool.get(queueName).addMsg(msg);
        } catch (Exception e) {
            System.out.println("发布者:" + publisherName + " 发布消息:" +msg + " 到队列:" + queueName
                    + "时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }
}