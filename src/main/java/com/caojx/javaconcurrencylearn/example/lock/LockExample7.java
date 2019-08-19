package com.caojx.javaconcurrencylearn.example.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition 如果想单独唤醒部分线程该怎么处理？这时候就必须使用多个Condition对象了，也就是Condition对象可以唤醒部分指定的线程，有助于
 * 提升程序运行时的效率。可以先对线程进行分组，然后再唤醒指定组的中线程。
 */
@Slf4j
public class LockExample7 {

    private Lock lock = new ReentrantLock();
    private Condition conditionA = lock.newCondition();
    private Condition conditionB = lock.newCondition();

    public void awaitA() {
        try {
            lock.lock();
            log.info("begin awaitA 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
            conditionA.await();
            log.info("end awaitA 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            log.error("awaitA异常：", e);
        } finally {
            lock.unlock();
        }
    }

    public void awaitB() {
        try {
            lock.lock();
            log.info("begin awaitB 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
            conditionB.await();
            log.info("end awaitB 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            log.error("awaitB异常：", e);
        } finally {
            lock.unlock();
        }
    }

    public void signalAll_A() {
        try{
            lock.lock();
            log.info("signalAll_A 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
            conditionA.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public void signalAll_B() {
        try{
            lock.lock();
            log.info("signalAll_B 时间为" + System.currentTimeMillis() + "ThreadName=" + Thread.currentThread().getName());
            conditionB.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockExample7 lockExample7 = new LockExample7();

        //线程1
        Thread threadA = new Thread(() -> {
            lockExample7.awaitA();
        });
        threadA.setName("A");
        threadA.start();

        //线程2
        Thread threadB = new Thread(()->{
            lockExample7.awaitB();
        });
        threadB.setName("B");
        threadB.start();

        Thread.sleep(3000);
        lockExample7.signalAll_A();

        //运行程序后，只有结线程A被唤醒了
    }
}
