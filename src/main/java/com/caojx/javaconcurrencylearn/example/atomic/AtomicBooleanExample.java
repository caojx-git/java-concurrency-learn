package com.caojx.javaconcurrencylearn.example.atomic;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AtomicBoolean 使用示例
 *
 * @author caojx
 * @version $Id: AtomicBooleanExample.java,v 1.0 2019-07-23 23:23 caojx
 * @date 2019-07-23 23:23
 */
@Slf4j
@ThreadSafe
public class AtomicBooleanExample {

    private static AtomicBoolean isHappend = new AtomicBoolean(false);

    // 请求总数
    private static int clientTotal = 5000;

    // 同时并发执行的线程数
    private static int threadTotal = 200;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);

        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    // 是否允许执行，是否达到设置的并发数（threadTotal），如果达到则不允许执行
                    semaphore.acquire();
                    test();
                    // 释放信号量
                    semaphore.release();
                } catch (InterruptedException e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        //countDownLatch 计数器为0后才会往后执行
        countDownLatch.await();
        //关闭线程池
        executorService.shutdown();
        log.info("isHappend:{}", isHappend.get());

    }

    //只会执行一次
    public static void test() {
        if (isHappend.compareAndSet(false, true)) {
            log.info("execute");
        }
    }
}