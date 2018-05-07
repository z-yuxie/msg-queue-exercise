package com.yuxie.test.model;

import com.yuxie.test.bean.Subcriber;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 147356
 * @create 2018-05-06 23:17
 * @desc 订阅者模块
 * 若业务系统对各个订阅者有做持久化处理,则此模块应当有定期处理非活跃状态的订阅者的定时任务
 **/
public class SubcriberModel {
    /**
     * 订阅者集合,代替数据库或其他持久化方式存储的各个订阅者
     * 若业务系统对各个订阅者有做持久化处理,则此集合仅代表在系统中当前时间段处在活跃状态的订阅者集合
     */
    private Map<String , Subcriber> subcriberSet;

    private Lock lock = new ReentrantLock();

    private static class SubcriberModelHolder {
        private static final SubcriberModel INSTANCE = new SubcriberModel();
    }

    public static SubcriberModel getInstance() {
        return SubcriberModelHolder.INSTANCE;
    }

    private SubcriberModel() {
        subcriberSet = new HashMap<>();
    }

    /**
     * 订阅队列消息
     * @param subcriberName 订阅者名称
     * @param queueName 订阅者要订阅的消息队列
     * @return 是否订阅成功
     */
    public boolean unsubscribeQueue(String subcriberName , String queueName) {
        try {
            if (!subcriberSet.containsKey(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 不存在,无需进行退订操作!");
                return false;
            }
            if (QueuePoolModel.getInstance().unsubscribeQueue(subcriberName, queueName)) {
                subcriberSet.get(subcriberName).removeQueueSubcriberRecord(queueName);
                System.out.println("订阅者:" + subcriberName + " 成功退订了一个队列:" + queueName);
                return true;
            }
            System.out.println("订阅者:" + subcriberName + " 退订队列:" + queueName + " 失败");
            return false;
        } catch (Exception e) {
            System.out.println("订阅者:" + subcriberName + " 退订队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 订阅队列消息
     * @param subcriberName 订阅者名称
     * @param queueName 订阅者要订阅的消息队列
     * @return 是否订阅成功
     */
    public boolean subscribeQueue(String subcriberName , String queueName) {
        try {
            if (!subcriberSet.containsKey(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 还没有进行注册,无法进行订阅!");
                return false;
            }
            if (QueuePoolModel.getInstance().subscribeQueue(subcriberName, queueName)) {
                subcriberSet.get(subcriberName).addQueueSubcriberRecord(queueName);
                System.out.println("订阅者:" + subcriberName + " 成功订阅了一个队列:" + queueName);
                return true;
            }
            System.out.println("订阅者:" + subcriberName + " 订阅队列:" + queueName + " 失败");
            return false;
        } catch (Exception e) {
            System.out.println("订阅者:" + subcriberName + " 订阅队列:" + queueName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注册订阅者
     * @param subcriberName 要注册的订阅者名称
     * @return 是否注册成功
     */
    public boolean registSubcriber(String subcriberName) {
        try {
            if (subcriberSet.containsKey(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 已经注册过了,请不要重复注册!");
                return false;
            }
            Subcriber newSubcriber = new Subcriber(subcriberName);
            lock.lock();
            if (subcriberSet.containsKey(subcriberName)) {
                System.out.println("订阅者:" + subcriberName + " 已经注册过了,请不要重复注册!");
                return false;
            }
            subcriberSet.put(subcriberName , newSubcriber);
            lock.unlock();
            System.out.println("成功注册了一个新的订阅者:" + subcriberName);
            return true;
        } catch (Exception e) {
            System.out.println("新注册订阅者:" + subcriberName + " 时发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 接收来自队列推送的消息
     * @param subcriberName 订阅者名称
     * @param msg 消息内容
     * @return 订阅者接收结果
     */
    public boolean receiveMsg(String subcriberName , String msg) {
        try {
            if (!subcriberSet.containsKey(subcriberName)) {
                //若有持久化,此处实际应该是去数据库等获取订阅者,如果仍没有获取到,再返回false
                //在没有获取到对应订阅者的情况下,应当通知系统中的各个队列取消该订阅者的订阅？
                //或者抛出异常,让推送方的队列对此问题进行处理？
                System.out.println("接收推送给:" + subcriberName + " 的消息:" + msg + "时,发现该订阅者已不存在");
                return false;
            }
            return subcriberSet.get(subcriberName).receiveMsg(msg);
        } catch (Exception e) {
            System.out.println("接收推送给:" + subcriberName + " 的消息:" + msg + "时,发生异常,异常信息为:");
            e.printStackTrace();
            return false;
        }
    }
}
