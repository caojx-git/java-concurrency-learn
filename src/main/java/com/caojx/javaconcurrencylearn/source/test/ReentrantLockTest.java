package com.caojx.javaconcurrencylearn.source.test;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * ReentrantLock 锁分析
 *
 * 参考文章：https://segmentfault.com/a/1190000015804888
 *
 * @author caojx created on 2020/4/2 9:39 下午
 */
@Slf4j
public class ReentrantLockTest {

    private Lock lock = new ReentrantLock(true);

//    ReentrantLock lock = new ReentrantLock();

    private void doWork() {
        try {
            lock.lock();
            log.info("begin doWork 时间为" + System.currentTimeMillis() + " ThreadName=" + Thread.currentThread().getName());
            Thread.sleep(2000);
            log.info("end doWork 时间为" + System.currentTimeMillis() + " ThreadName=" + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            log.error("doAwait 异常：", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * ThreadA先调用lock方法获取到锁，然后调用con.await()
     * <p>
     * ThreadB获取锁，调用con.signal()唤醒ThreadA
     * <p>
     * ThreadB释放锁
     *
     * @param args
     */

    /**
     * ThreadA    lock
     * <p>
     * ThreadB    lock
     * <p>
     * ThreadC    lock
     * <p>
     * ThreadA    release
     * <p>
     * ThreadB    release
     * <p>
     * ThreadC    release
     *
     * @param args
     */
    public static void main(String[] args) {

        ReentrantLockTest reentrantLockTest = new ReentrantLockTest();

        //线程A
        Thread threadA = new Thread(() -> {
            reentrantLockTest.doWork();
        });
        threadA.setName("threadA");
        threadA.start();

        //线程B
        Thread threadB = new Thread(() -> {
            reentrantLockTest.doWork();
        });
        threadB.setName("threadB");
        threadB.start();

        //线程C
        Thread threadC = new Thread(() -> {
            reentrantLockTest.doWork();
        });
        threadC.setName("threadC");
        threadC.start();
    }
}