package com.caojx.javaconcurrencylearn.example.lock;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用 ReentrantLock 加锁
 *
 * @author caojx
 * @version $Id: CountExample.java,v 1.0 2019-05-26 17:36 caojx
 * @date 2019-05-26 17:36
 */
@Slf4j
@ThreadSafe
public class LockExample2 {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotal = 200;

    public static int count = 0;

    private static final Lock lock = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("count:{}", count);
    }

    /**
     * 做+1 操作, 使用 lock
     */
    private static void add() {
        //tryLock()函数，他本质上的作用是仅在调用时锁定未被另一个线程保持的情况下，才获取锁定。
        // 与他相对应方法还有tryLock(long timeout, TimeUnit unit) 后面跟上两个参数代表超时时间和时间单位，它的作用是如果锁定在给定的等待时间内没有被另一个线程保持，且当前线程未被中断，则获取该锁定。
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
}