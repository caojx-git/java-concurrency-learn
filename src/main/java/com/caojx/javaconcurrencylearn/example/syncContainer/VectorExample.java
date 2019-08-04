package com.caojx.javaconcurrencylearn.example.syncContainer;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 测试Vector的线程安全性，线程安全
 *
 * @author caojx
 * @version $Id: VectorExample.java,v 1.0 2019-07-25 20:25 caojx
 * @date 2019-07-25 20:25
 */
@Slf4j
@ThreadSafe
public class VectorExample {

    // 请求总数
    public static int clientTotal = 5000;
    // 同时并发执行的线程数
    public static int threadTotal = 200;

    public static Vector<Integer> vector = new Vector<>();

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
            final int count = i;
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add(count);
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("size:{}", vector.size());
    }

    private static void add(int i) {
        vector.add(i);
    }
}