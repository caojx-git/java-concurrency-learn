package com.caojx.javaconcurrencylearn.example.commonUnsafe;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * StringBuilder，线程不安全
 *
 * @author caojx
 * @version $Id: StringExample1.java,v 1.0 2019-07-25 16:00 caojx
 * @date 2019-07-25 16:00
 */
@Slf4j
@NotThreadSafe
public class StringExample1 {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotal = 200;

    public static StringBuilder stringBuilder = new StringBuilder();

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal ; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    update();
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("length:{}", stringBuilder.length());
    }

    /**
     * 做+1 操作
     */
    private static void update() {
       stringBuilder.append("1");
    }
}