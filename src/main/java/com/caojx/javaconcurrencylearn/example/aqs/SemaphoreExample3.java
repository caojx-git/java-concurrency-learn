package com.caojx.javaconcurrencylearn.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 测试Semaphore，一次性拿多个许可
 */
@Slf4j
public class SemaphoreExample3 {

    private final static int threadCount = 20;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        //设置允许的并发数3
        final Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executorService.execute(() -> {
                try {
                    if (semaphore.tryAcquire()) {//尝试获取一个许可，如果获取到就允许执行，否则就不让执行
                        test(threadNum);
                        //性释放一个许可
                        semaphore.release();
                    }
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
