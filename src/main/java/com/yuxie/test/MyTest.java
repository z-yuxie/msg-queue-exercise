package com.yuxie.test;

import com.yuxie.test.model.QueuePoolModel;
import com.yuxie.test.model.SubcriberModel;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

/**
 * @author 147356
 * @create 2018-05-06 23:47
 * @desc 测试方法
 **/
public class MyTest {
    public static void main(String[] args) {
        System.out.println("消息队列系统测试开始-------------当前时间是:" + System.currentTimeMillis());
        try {
            while (true) {
                System.out.println("请输入要执行的指令:");
                Scanner scan = new Scanner(System.in);
                String oneInstructions = scan.nextLine();
                if (StringUtils.isBlank(oneInstructions)) {
                    System.out.println("请输入有效的指令!");
                    continue;
                }
                String[] oneInstructionsKeys = oneInstructions.trim().split(" ");
                if (oneInstructionsKeys.length == 0) {
                    System.out.println("请输入有效的指令!");
                    continue;
                }
                if ("$close".equals(oneInstructionsKeys[0])) {
                    QueuePoolModel.getInstance().emptyQueuePool();
                    SubcriberModel.getInstance().emptySubcriberMap();
                    break;
                } else if (oneInstructionsKeys.length == 2 && "$registSubcriber".equals(oneInstructionsKeys[0])
                        && StringUtils.isNotBlank(oneInstructionsKeys[1])) {
                    SubcriberModel.getInstance().registSubcriber(oneInstructionsKeys[1]);
                } else if (oneInstructionsKeys.length == 3 && "$subscribeQueue".equals(oneInstructionsKeys[0])
                        && StringUtils.isNoneBlank(oneInstructionsKeys[1] , oneInstructionsKeys[2])) {
                    SubcriberModel.getInstance().subscribeQueue(oneInstructionsKeys[1] , oneInstructionsKeys[2]);
                } else if (oneInstructionsKeys.length == 3 && "$unsubscribeQueue".equals(oneInstructionsKeys[0])
                        && StringUtils.isNoneBlank(oneInstructionsKeys[1] , oneInstructionsKeys[2])) {
                    SubcriberModel.getInstance().unsubscribeQueue(oneInstructionsKeys[1] , oneInstructionsKeys[2]);
                } else if (oneInstructionsKeys.length == 4 && "$pushMsg".equals(oneInstructionsKeys[0])
                        && StringUtils.isNoneBlank(
                        oneInstructionsKeys[1] , oneInstructionsKeys[2] , oneInstructionsKeys[3])) {
                    QueuePoolModel.getInstance().push(
                            oneInstructionsKeys[1] , oneInstructionsKeys[2] , oneInstructionsKeys[3]);
                } else {
                    System.out.println("请输入有效的指令!");
                }
            }
        } catch (Exception e) {
            System.out.println("消息队列系统测试中发生异常,异常信息为:");
            e.printStackTrace();
        }
        System.out.println("消息队列系统测试结束-------------当前时间是:" + System.currentTimeMillis());
    }
}
