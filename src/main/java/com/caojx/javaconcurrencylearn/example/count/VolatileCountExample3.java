package com.caojx.javaconcurrencylearn.example.count;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * volatile修饰，做+1操作，非线程安全，volatile这个关键字他不具有原子性
 *
 * @author caojx
 * @version $Id: CountExample2.java,v 1.0 2019-07-23 16:22 caojx
 * @date 2019-07-23 16:22
 */
@Slf4j
@NotThreadSafe
public class VolatileCountExample3 {

    // 请求总数
    private static int clientTotal = 5000;

    // 同时并发执行的线程数
    private static int threadTotal = 200;

    /**
     * 使用volatile 修饰变量，修改其值依然无法保证线程安全，当我们执行count++的时候，他其实是分了3步：
     * 1. 取count的值
     * 2. +1
     * 3. count的值重新写回主存
     *
     */
    private static volatile int count = 0;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);

        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    // 是否允许执行，是否达到设置的并发数（threadTotal），如果达到则不允许执行
                    semaphore.acquire();
                    add();
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
        log.info("count:{}", count);

    }

    private static void add() {
        count++;
        /**
         * 实际使用volatile修饰变量结果证明，即使使用volatile修饰改变其值依然无法保证线程安全，当我们执行count++的时候，他其实是分了3步：
         * 1. 取count的值
         * 2. +1
         * 3. count的值重新写回主存
         *
         * 假设我们同事有2个线程在执行count++操作，两个线程都执行了第1步，比如count=5，他们都读到了，然后2个线程都执行了+1操作，并写回主存，尽管他们在读取的时候他们
         * 都拿到了最新值，但是他们同时把自己+1后的值写回了主存，这样的话就丢掉了一次+1的操作，这样一来呢，我们就会发现，有的时候我计算了5000次+1操作，结果却有可能比5000小，通过这个演示呢，我们确认了一个事情，volatile直接做这种操作，不是线程安全的
         */
    }

}