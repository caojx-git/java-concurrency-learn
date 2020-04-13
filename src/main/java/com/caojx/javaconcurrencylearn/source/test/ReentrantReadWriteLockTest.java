package com.caojx.javaconcurrencylearn.source.test;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 本章将会从ReentrantReadWriteLock出发，给出其内部利用AQS框架的实现原理。
 * <p>
 * ReentrantReadWriteLock（以下简称RRW），也就是读写锁，是一个比较特殊的同步器，
 * 特殊之处在于其对同步状态State的定义与ReentrantLock、CountDownLatch都很不同。通过RRW的分析，
 * 我们可以更深刻的了解AQS框架的设计思想，以及对“什么是资源？如何定义资源是否可以被访问？”这一命题有更深刻的理解。
 *
 *
 * 关于ReentrantReadWriteLock，最后有两点规律需要注意：
 *
 *     当RRW的等待队列队首结点是共享结点，说明当前写锁被占用，当写锁释放时，会以传播的方式唤醒头结点之后紧邻的各个共享结点。
 *     当RRW的等待队列队首结点是独占结点，说明当前读锁被使用，当读锁释放归零后，会唤醒队首的独占结点。
 *
 * ReentrantReadWriteLock的特殊之处其实就是用一个int值表示两种不同的状态（低16位表示写锁的重入次数，高16位表示读锁的使用次数），并通过两个内部类同时实现了AQS的两套API，核心部分与共享/独占锁并无什么区别。
 *
 * 参考文章：https://segmentfault.com/a/1190000015807600
 *
 * @author caojx created on 2020/4/3 10:15 下午
 */
@Slf4j
public class ReentrantReadWriteLockTest {

    /**
     * 假设现在有4个线程，ThreadA、ThreadB、ThreadC、ThreadD。
     * ThreadA、ThreadB、ThreadD为读线程，ThreadC为写线程
     * <p>
     * //ThreadA调用读锁的lock()方法
     * <p>
     * //ThreadB调用读锁的lock()方法
     * <p>
     * //ThreadC调用写锁的lock()方法
     * <p>
     * //ThreadD调用读锁的lock()方法
     *
     * @param args
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock.ReadLock rl = lock.readLock();
    private ReentrantReadWriteLock.WriteLock wl = lock.writeLock();

    public void readA() {
        try {
            rl.lock();
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000*5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rl.unlock();
        }
    }
    public void readB() {
        try {
            rl.lock();
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000*5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rl.unlock();
        }
    }

    public void readD() {
        try {
            rl.lock();
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000*5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rl.unlock();
        }
    }

    public void writeC() {
        try {
            wl.lock();
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000*5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            wl.unlock();
        }
    }

    public static void main(String[] args) {
        final ReentrantReadWriteLockTest myTask = new ReentrantReadWriteLockTest();

        Thread threadA = new Thread(() -> myTask.readA());
        threadA.setName("threadA");

        Thread threadB = new Thread(() -> myTask.readB());
        threadB.setName("threadB");

        Thread threadC = new Thread(() -> myTask.writeC());
        threadC.setName("threadC");

        Thread threadD = new Thread(() -> myTask.readD());
        threadD.setName("threadD");


        threadA.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadB.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadC.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadD.start();
    }
}