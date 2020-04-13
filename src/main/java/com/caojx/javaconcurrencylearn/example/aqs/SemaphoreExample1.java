package com.caojx.javaconcurrencylearn.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 *
 * Semaphore通过内部类实现了AQS框架提供的接口，而且基本结构几乎和ReentrantLock完全一样，通过内部类分别实现了公平/非公平策略
 *
 * 测试Semaphore
 */
@Slf4j
public class SemaphoreExample1 {

    private final static int threadCount = 200;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        //设置允许的并发数3
        final Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executorService.execute(() -> {
                try {
                    //获取一个许可，如果没有许可就等待
                    semaphore.acquire();
                    test(threadNum);
                    //释放一个许可
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
            });
        }
        executorService.shutdown();
    }

    //可以看到日志是一块一块输出的，每5s执行一次
    private static void test(int threadNum) throws Exception {
        log.info("{}", threadNum);
        Thread.sleep(5000);
    }
}
