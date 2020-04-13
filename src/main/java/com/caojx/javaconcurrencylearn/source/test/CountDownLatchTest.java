package com.caojx.javaconcurrencylearn.source.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;


/**
 * CountDownLatch为例，分析AQS的共享功能。CountDownLatch，是J.U.C中的一个同步器类，可作为倒数计数器使用
 *
 * 参考文章：https://segmentfault.com/a/1190000015807573
 *
 * AQS的共享功能，通过钩子方法tryAcquireShared暴露，与独占功能最主要的区别就是：
 *
 * 共享功能的结点，一旦被唤醒，会向队列后部传播（Propagate）状态，以实现共享结点的连续唤醒。这也是共享的含义，当锁被释放时，所有持有该锁的共享线程都会被唤醒，并从等待队列移除。
 *
 * @author caojx created on 2020/4/3 12:42 下午
 */
@Slf4j
public class CountDownLatchTest {

    CountDownLatch switcher = new CountDownLatch(1);

    /**
     * 假设现在有3个线程，ThreadA、ThreadB、mainThread，CountDownLatch初始计数为1：
     * CountDownLatch switcher = new CountDownLatch(1);
     *
     * 线程的调用时序如下
     *     ThreadA调用await()方法等待
     *
     *     ThreadB调用await()方法等待
     *
     *     主线程main调用countDown()放行
     */
    public static void main(String[] args) {

        CountDownLatchTest countDownLatchTest = new CountDownLatchTest();

        //线程A
        Thread threadA = new Thread(() -> {
            countDownLatchTest.doAwait();
        });
        threadA.setName("threadA");
        threadA.start();

        //线程B
        Thread threadB = new Thread(() -> {
            countDownLatchTest.doAwait();
        });
        threadB.setName("threadB");
        threadB.start();

        log.info("main countDown finish");
        countDownLatchTest.switcher.countDown();

    }

    private void doAwait() {
        try {
            log.info("begin doAwait 时间为" + System.currentTimeMillis() + " ThreadName=" + Thread.currentThread().getName());
            Thread.sleep(2000);
            switcher.await();
            log.info("end doAwait  时间为" + System.currentTimeMillis() + " ThreadName=" + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}