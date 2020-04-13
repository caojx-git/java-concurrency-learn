package com.caojx.javaconcurrencylearn.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CyclicBarrier 并没有自己去实现AQS框架的API，而是利用了ReentrantLock和Condition
 * CyclicBarrier可以认为是一个栅栏，栅栏的作用是什么？就是阻挡前行。
 * 顾名思义，CyclicBarrier是一个可以循环使用的栅栏，它做的事情就是：
 * 让线程到达栅栏时被阻塞(调用await方法)，直到到达栅栏的线程数满足指定数量要求时，栅栏才会打开放行。这其实有点像军训报数，报数总人数满足教官认为的总数时，教官才会安排后面的训练。
 */
@Slf4j
public class CyclicBarrierExample1 {

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
    }

    private static void race(int threadNum) throws Exception {
        Thread.sleep(1000);
        log.info("{} is ready", threadNum);
        cyclicBarrier.await(); //await()线程数满足线程屏障数5个之后，就会继续执行后边的操作
        log.info("{} continue", threadNum);
    }
}
