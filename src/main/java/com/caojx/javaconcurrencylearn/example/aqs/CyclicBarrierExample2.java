package com.caojx.javaconcurrencylearn.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * CyclicBarrier中的await方法支持传入等待时间，
 */
@Slf4j
public class CyclicBarrierExample2 {

    //设置线程屏障数为5个
    private static CyclicBarrier cyclicBarrier = new CyclicBarrier(5);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            //延迟1s，便于观察日志
            Thread.sleep(1000);
            executorService.execute(() -> {
                try {
                    race(threadNum);
                } catch (Exception e) {
                    log.error("exception", e);
                }
            });
        }
        executorService.shutdown();
    }

    private static void race(int threadNum) throws Exception {
        Thread.sleep(1000);
        log.info("{} is ready", threadNum);
        try {
            // 超过等待时间之后，会抛出BrokenBarrierException，为了不影响后边的执行，这里try一下
            cyclicBarrier.await(2000, TimeUnit.MILLISECONDS);
        } catch (BrokenBarrierException | TimeoutException | InterruptedException e) {
            log.error("BrokenBarrierException | TimeoutException |  InterruptedException", e);
        }
        log.info("{} continue", threadNum);
    }
}
