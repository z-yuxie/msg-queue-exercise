package com.yuxie.test;

import static org.junit.Assert.assertTrue;

import com.yuxie.test.model.QueuePoolModel;
import com.yuxie.test.model.SubcriberModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    boolean flag = true;

    @Test
    public void threadTest() throws InterruptedException {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("线程1进入睡眠");
                    TimeUnit.SECONDS.sleep(5L);
                    System.out.println("线程1睡眠结束,开始变更标识状态");
                    flag = false;
                } catch (InterruptedException e) {
                    System.out.println("........线程1出现异常");
                    e.printStackTrace();
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (!flag) {
                            System.out.println("线程2准备进入睡眠");
                            TimeUnit.SECONDS.sleep(5L);
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
        thread1.start();
        thread2.start();
        TimeUnit.SECONDS.sleep(1L);
        thread2.interrupt();
        TimeUnit.SECONDS.sleep(10L);
    }
}
