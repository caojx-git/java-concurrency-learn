package com.caojx.javaconcurrencylearn.example;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 非线程安全，map中的size不一定为5000
 *
 * @author caojx
 * @version $Id: MapExample.java,v 1.0 2019-05-26 17:45 caojx
 * @date 2019-05-26 17:45
 */
@Slf4j
@NotThreadSafe
public class MapExample {

    private static Map<Integer, Integer> map = Maps.newHashMap();

    // 请求总数
    private static int clientNum = 5000;

    // 同时并发执行的线程数
    private static int threadNum = 200;


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadNum);
        for (int index = 0; index < clientNum; index++) {
            final int threadNum = index;
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    func(threadNum);
                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
            });
        }
        executorService.shutdown();
        log.info("size:{}", map.size());
    }

    private static void func(int threadNum) {
        map.put(threadNum, threadNum);
    }
}