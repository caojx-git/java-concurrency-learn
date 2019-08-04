package com.caojx.javaconcurrencylearn.example.commonUnsafe;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * SimpleDateFormat 测试，将SimpleDateFormat声明为局部变量达到堆栈封闭，达到多线程环境下线程安全
 * 或者使用线程封闭技术，TreadLocal也可以解决
 *
 * @author caojx
 * @version $Id: SimpleDateFormatExample1.java,v 1.0 2019-07-25 16:23 caojx
 * @date 2019-07-25 16:23
 */
@Slf4j
@ThreadSafe
public class SimpleDateFormatExample2 {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotal = 200;

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
     * 将SimpleDateFormat声明为局部变量达到堆栈封闭，达到多线程环境下线程安全
     */
    private static void update() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            simpleDateFormat.parse("20180208");
        } catch (ParseException e) {
            log.error("parse exception", e);
        }
    }
}