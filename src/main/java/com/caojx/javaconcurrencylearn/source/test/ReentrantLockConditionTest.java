package com.caojx.javaconcurrencylearn.source.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * ReentrantLock 条件队列 Condition分析
 * <p>
 * 参考文章：https://segmentfault.com/a/1190000015807209
 *
 * @author caojx created on 2020/4/2 6:54 下午
 */
@Slf4j
public class ReentrantLockConditionTest {

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void doAwait() {
        try {
            lock.lock();
            log.info("begin doAwait 时间为" + System.currentTimeMillis() + " ThreadName=" + Thread.currentThread().getName());
            condition.await();
            log.info("end doAwait 时间为" + System.currentTimeMillis() + " ThreadName=" + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            log.error("doAwait 异常：", e);
        } finally {
            lock.unlock();
        }
    }

    public void doSignal() {
        try {
            lock.lock();
            log.info("begin doSignal 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
            condition.signal();
            log.info("end doSignal 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
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
     * 以ReentrantLock的公平锁为例，分析AbstractQueuedSynchronizer的Condition功能。
     * 通过分析，可以看到，当线程在指定Condition对象上等待的时候，其实就是将线程包装成结点，加入了条件队列，释放锁，然后阻塞。当线程被通知唤醒时，则是将条件队列中的结点转换成等待队列中的结点，之后的处理就和独占功能完全一样。
     *
     * 除此之外，Condition还支持限时等待、非中断等待等功能，分析思路是一样的，读者可以自己去阅读AQS的源码，通过使用示例，加入调试断点一步步看内部的调用流程，主干理顺了之后，再看其它分支，其实是异曲同工的。
     *
     * @param args
     */
    public static void main(String[] args) {

        ReentrantLockConditionTest reentrantLockConditionTest = new ReentrantLockConditionTest();

        //线程A
        Thread threadA = new Thread(() -> {
            reentrantLockConditionTest.doAwait();
        });
        threadA.setName("threadA");
        threadA.start();

        //线程B
        Thread threadB = new Thread(() -> {
            reentrantLockConditionTest.doSignal();
        });
        threadB.setName("threadB");
        threadB.start();
    }
}