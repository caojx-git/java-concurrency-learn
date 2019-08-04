package com.caojx.javaconcurrencylearn.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 测试Semaphore，一次性拿多个许可
 */
@Slf4j
public class SemaphoreExample2 {

    private final static int threadCount = 200;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        //设置允许的并发数3
        final Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executorService.execute(() -> {
                try {

                    /**
                     * Semaphore设置的允许的并发数与一次性或的许可数和一次性允许释放的许可相同，那么这种情况只能5s中执行一次test()方法（sleep(5000)）,
                     * 因为第二个请求过来时，没有多余的许可。
                     */
                    //一次性获取3个许可
                    semaphore.acquire(3);
                    test(threadNum);
                    //一次性释放3个许可，当然我如果一次性拿多个许可，我们们也可以一个一个释放
                    semaphore.release(3);
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
