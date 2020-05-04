package com.caojx.javaconcurrencylearn.source.test;

import java.util.concurrent.LinkedTransferQueue;

/**
 * 参考：
 * https://segmentfault.com/a/1190000016460411
 * https://www.jianshu.com/p/ae6977886cec
 * <p>
 * LinkedTransferQueue当 put 时，如果有等待的线程，就直接将元素 “交给” 等待者， 否则直接进入队列，
 * put和 transfer 方法的区别是，put 是立即返回的， transfer 是阻塞等待消费者拿到数据才返回。transfer方法和 SynchronousQueue的 put 方法类似
 * <p>
 * 很多文章说：SynchronousQueue是一个不存储元素的队列。每一个put操作必须等待一个take操作，否则不能继续添加元素
 * 我对"SynchronousQueue是一个不存储元素的队列"，这句话保持质疑的态度，SynchronousQueue中是会保存节点的
 * 多线程情况下：多个线程如果都是入队操作，结点还是会连接到队列中
 * <p>
 * SynchronousQueue与LinkedTransferQueue对比：
 * SynchronousQueue与LinkedTransferQueue很相似，只不过LinkedTransferQueue中定义了四种执行类型， NOW, ASYNC, SYNC, TIMED 四种常量定义(执行类型，立即返回的NOW，异步的ASYNC，阻塞的SYNC， 带超时的 TIMED)，put操作是异步的，并不会阻塞
 * 对于SynchronousQueue全部都是阻塞的操作（put、take来说）更加灵活。
 *
 * @author caojx created on 2020/4/22 12:59 下午
 */
public class LinkedTransferQueueTest {

    public static void main(String[] args) throws InterruptedException {
        LinkedTransferQueueTest test = new LinkedTransferQueueTest();
//        test.singleTheadTest();
        test.multiTheadTest();
    }

    /**
     * 单线程情况下，LinkedTransferQueue 的put操作是异步的执行类型，并不会阻塞
     *
     * @throws InterruptedException
     */
    public void singleTheadTest() throws InterruptedException {
        LinkedTransferQueue queue = new LinkedTransferQueue<>();
        System.out.println("put--1");
        queue.put(1);

        System.out.println("put--2");
        queue.put(2);

        System.out.println("take--" + queue.take());
        System.out.println("take--" + queue.take());
    }

    public void multiTheadTest() {
        LinkedTransferQueue queue = new LinkedTransferQueue<>();
        Thread threadA = new Thread(() -> {
            System.out.println("put--1");
            queue.put(1);
        });
        threadA.setName("threadA");

        Thread threadB = new Thread(() -> {
            System.out.println("put--2");
            queue.put(2);
        });
        threadB.setName("threadB");

        Thread threadC = new Thread(() -> {
            System.out.println("put--3");
            queue.put(3);
        });
        threadC.setName("threadC");

        Thread threadD = new Thread(() -> {
            try {
                System.out.println("take--" + queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadD.setName("threadD");

        Thread threadE = new Thread(() -> {
            try {
                System.out.println("take--" + queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadE.setName("threadE");


        threadA.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadB.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadC.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadD.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadE.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}