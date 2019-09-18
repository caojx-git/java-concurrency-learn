package com.caojx.javaconcurrencylearn.example;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 非线程安全，count最终结果不确定，不一定为5000
 *
 * @author caojx
 * @version $Id: CountExample.java,v 1.0 2019-05-26 17:36 caojx
 * @date 2019-05-26 17:36
 */
@Slf4j
@NotThreadSafe
public class CountExample {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotal = 200;

    public static int count = 0;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal ; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        log.info("count:{}", count);
    }

    /**
     * 做+1 操作
     *
     * 计数值不是5000，我们来推演一下，我们假设主内存中当前变量的值为1，线程A和线程B同时开始执行
     *
     * 1.先说线程A执行，线程A从主内存里面拿到的变量的值是1，存到自己的本地内存A里面，然后执行+1操作，线程A计算完之后得到的结果为2，然后线程A将结果写回主内存变量变成2。
     *
     * 2.同时执行的线程B他是如何执行的呢，线程A从主内存拿到值为1的同时，线程B从主内存拿到的值也是1，存在线程B的本地内存B里面，然后执行+1操作，最后变成结果为2，
     *
     * 在线程A将结果2写回主内存的同时线程B也开始让自己计算后的结果2写回到主内存，而不是先读取到线程A的结果2之后再重新计算，这两个计算过程中，两个线程间的数据他们是互相不可见的，
     * 因此计数就出现了错误，这个时候我们就必须增加一些同步的手段，来保证并发时程序处理的准确性。
     */
    private static void add() {
        count++;
    }
}