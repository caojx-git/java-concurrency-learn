package com.caojx.javaconcurrencylearn.example.commonUnsafe;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * SimpleDateFormat 测试，多线程环境下出现线程安全问题
 *
 * @author caojx
 * @version $Id: SimpleDateFormatExample1.java,v 1.0 2019-07-25 16:23 caojx
 * @date 2019-07-25 16:23
 */
@Slf4j
@NotThreadSafe
public class SimpleDateFormatExample1 {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotal = 200;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
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
    }

    /**
     * 多线程情况下会出现异常
     */
    private static void update() {
        try {
            simpleDateFormat.parse("20180208");
        } catch (ParseException e) {
            log.error("parse exception", e);
        }
    }
}