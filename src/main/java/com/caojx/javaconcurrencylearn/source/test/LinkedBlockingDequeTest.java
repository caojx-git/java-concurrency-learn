package com.caojx.javaconcurrencylearn.source.test;

import com.caojx.javaconcurrencylearn.source.util.concurrent.LinkedBlockingDeque;

/**
 * 参考文章：https://segmentfault.com/a/1190000016398508
 *
 * @author caojx created on 2020/4/22 4:37 下午
 */
public class LinkedBlockingDequeTest {

    public static void main(String[] args) throws InterruptedException {
        LinkedBlockingDeque linkedBlockingDeque = new LinkedBlockingDeque<>();

        // 队尾入队
        linkedBlockingDeque.put(1);

        System.out.println(linkedBlockingDeque);

        // 队首入队
        linkedBlockingDeque.putFirst(2);
        System.out.println(linkedBlockingDeque);

        // 队首出队
        System.out.println(linkedBlockingDeque.take());

        // 队尾出队
        System.out.println(linkedBlockingDeque.takeLast());

    }
}