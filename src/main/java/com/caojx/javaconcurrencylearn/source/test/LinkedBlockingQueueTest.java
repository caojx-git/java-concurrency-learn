package com.caojx.javaconcurrencylearn.source.test;

import com.caojx.javaconcurrencylearn.source.util.concurrent.LinkedBlockingQueue;

/**
 * 参考文章：https://segmentfault.com/a/1190000016315487
 *
 * @author caojx created on 2020/4/22 4:31 下午
 */
public class LinkedBlockingQueueTest {

    public static void main(String[] args) throws InterruptedException {
        LinkedBlockingQueue queue = new LinkedBlockingQueue();
        queue.put(9);
        queue.put(2);
        queue.put(10);
        queue.put(25);

        System.out.println(queue.toString());

        System.out.println(queue.take());

    }
}