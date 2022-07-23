package com.caojx.javaconcurrencylearn.example.sync;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * synchronized 示例，synchronized修饰方法和代码块
 * 修饰代码块：大括号括起来的代码，作用于调用的对象
 * 修饰方法：整个方法，作用于调用的对象
 *
 * @author caojx
 * @version $Id: SynchronizedExample2.java,v 1.0 2019-07-24 11:07 caojx
 * @date 2019-07-24 11:07
 */
@Slf4j
public class SynchronizedExample2 {

    /**
     * 修饰代码块，大括号括起来的代码，作用于调用的对象
     */
    public void test1(int j) {
        synchronized (this) { //被修饰的代码块称为同步代码块
            for (int i = 0; i < 10; i++) {
                log.info("test1 {} - {}", j, i);
            }
        }
    }

    /**
     * 修饰一个方法，被修饰的方法称为同步方法，整个方法，作用于调用的对象
     *
     * @param j
     */
    public synchronized void test2(int j) {
        for (int i = 0; i < 10; i++) {
            log.info("test2 {} - {}", j, i);
        }
    }

    /**
     * 不同的对象调用调用synchronized修饰的方法，乱序输出，调用同一个方法，不同的对象之间调用互相不影响
     *
     * @param args
     */
    public static void main(String[] args) {
        SynchronizedExample2 synchronizedExample1 = new SynchronizedExample2();
        SynchronizedExample2 synchronizedExample2 = new SynchronizedExample2();
        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(() -> {
            synchronizedExample1.test1(1);
        });
        executorService.execute(() -> {
            synchronizedExample2.test1(2);
        });
    }

}
