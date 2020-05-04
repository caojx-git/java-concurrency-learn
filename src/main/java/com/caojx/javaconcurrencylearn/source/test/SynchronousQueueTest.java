package com.caojx.javaconcurrencylearn.source.test;

import java.util.concurrent.SynchronousQueue;

/**
 * 参考文章：https://segmentfault.com/a/1190000016359551
 * <p>
 * 很多文章说：SynchronousQueue是一个不存储元素的队列。每一个put操作必须等待一个take操作，否则不能继续添加元素
 * 我对"SynchronousQueue是一个不存储元素的队列"，这句话保持质疑的态度，SynchronousQueue中是会保存节点的
 * 多线程情况下：多个线程如果都是入队操作，结点还是会连接到队列中
 * <p>
 * SynchronousQueue与LinkedTransferQueue对比：
 * SynchronousQueue与LinkedTransferQueue很相似，只不过LinkedTransferQueue中定义了四种执行类型， NOW, ASYNC, SYNC, TIMED 四种常量定义(执行类型，立即返回的NOW，异步的ASYNC，阻塞的SYNC， 带超时的 TIMED)，put操作是异步的，并不会阻塞
 * 对于SynchronousQueue全部都是阻塞的操作（put、take来说）更加灵活。
 *
 * @author caojx created on 2020/4/22 12:48 下午
 */
public class SynchronousQueueTest {

    public static void main(String[] args) throws InterruptedException {
        SynchronousQueueTest test = new SynchronousQueueTest();
//        test.singleTheadTest();

        test.multiTheadTest();
    }


    /**
     * 单线程情况下
     *
     * @throws InterruptedException
     */
    public void singleTheadTest() throws InterruptedException {
        SynchronousQueue queue = new SynchronousQueue();
        System.out.println("put--1");
        queue.put(1);

        // 由于queue.put(1); 会阻塞，所以不会执行到这里
        System.out.println("take--" + queue.take());
    }

    /**
     * 多线程环境下，如果不是配对节点（出队入队），则会加入到队列/栈
     */
    public void multiTheadTest() {
        SynchronousQueue queue = new SynchronousQueue();
        Thread threadA = new Thread(() -> {
            System.out.println("put--1");
            try {
                queue.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadA.setName("threadA");

        Thread threadB = new Thread(() -> {
            System.out.println("put--2");
            try {
                queue.put(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadB.setName("threadB");

        Thread threadC = new Thread(() -> {
            System.out.println("put--3");
            try {
                queue.put(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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