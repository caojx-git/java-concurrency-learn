package com.caojx.javaconcurrencylearn.example.sync;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * synchronized 示例 修饰类和静态方法
 * <p>
 * 修饰静态方法：整个静态方法，作用于所有对象
 * 修饰类：括号括起来的部分，作用于所有对象
 *
 * @author caojx
 * @version $Id: SynchronizedExample1.java,v 1.0 2019-07-24 11:07 caojx
 * @date 2019-07-24 11:07
 */
@Slf4j
public class SynchronizedExample3 {

    /**
     * 修饰一个类，括号括起来的部分，作用对象是这个类的所有对象
     */
    public static void test1(int j) {
        synchronized (SynchronizedExample3.class) {
            for (int i = 0; i < 10; i++) {
                log.info("test1 {} - {}", j, i);
            }
        }
    }

    /**
     * 修饰一个静态方法，整个静态方法，作用于所有对象
     */
    public static synchronized void test2(int j) {
        for (int i = 0; i < 10; i++) {
            log.info("test2 {} - {}", j, i);
        }
    }


    /**
     * 0~9先执行一次，然后再0~9一次
     *
     * @param args
     */
    public static void main(String[] args) {
        SynchronizedExample3 synchronizedExample1 = new SynchronizedExample3();
        SynchronizedExample3 synchronizedExample2 = new SynchronizedExample3();
        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(() -> {
            synchronizedExample1.test2(1);
        });
        executorService.execute(() -> {
            synchronizedExample2.test2(2);
        });
    }

}
