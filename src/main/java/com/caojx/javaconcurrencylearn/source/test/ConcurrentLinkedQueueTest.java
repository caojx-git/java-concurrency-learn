package com.caojx.javaconcurrencylearn.source.test;


import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 参考：
 * https://www.jianshu.com/p/231caf90f30b
 * https://segmentfault.com/a/1190000016248143
 * <p>
 * 入队与出队操作与文章介绍的不太一样，以后再回来看吧
 *
 * @author caojx created on 2020/4/17 5:49 下午
 */
public class ConcurrentLinkedQueueTest {

    public static void main(String[] args) {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();


        queue.add("A");
        queue.add("B");
        queue.add("C");

        queue.poll();
        queue.poll();
        queue.poll();

    }
}