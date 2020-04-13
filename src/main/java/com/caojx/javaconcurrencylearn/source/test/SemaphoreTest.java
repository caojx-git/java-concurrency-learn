package com.caojx.javaconcurrencylearn.source.test;

import java.util.concurrent.Semaphore;

/**
 *
 * Semaphore，又名信号量，这个类的作用有点类似于“许可证”。有时，我们因为一些原因需要控制同时访问共享资源的最大线程数量，比如出于系统性能的考虑需要限流，或者共享资源是稀缺资源，我们需要有一种办法能够协调各个线程，以保证合理的使用公共资源。
 *
 * CountDownLatch内部实现了AQS的共享功能，Semaphore也一样是利用内部类实现了AQS的共享功能
 * <p>
 * Semaphore其实就是实现了AQS共享功能的同步器，对于Semaphore来说，资源就是许可证的数量：
 * <p>
 * 剩余许可证数（State值） - 尝试获取的许可数（acquire方法入参） ≥ 0：资源可用
 * 剩余许可证数（State值） - 尝试获取的许可数（acquire方法入参） < 0：资源不可用
 *
 * 参考文章：https://segmentfault.com/a/1190000015918459
 *
 * @author caojx created on 2020/4/10 5:46 下午
 */
public class SemaphoreTest {


    /**
     * Semaphore的公平策略分析
     * 假设现在一共3个线程：ThreadA、ThreadB、ThreadC。一个许可数为2的公平策略的Semaphore。线程的调用顺序如下：
     * <p>
     * Semaphore sm = new Semaphore (2, true);
     * <p>
     * // ThreadA: sm.acquire()
     * <p>
     * // ThreadB: sm.acquire(2)
     * <p>
     * // ThreadC: sm.acquire()
     * <p>
     * // ThreadA: sm.release()
     * <p>
     * // ThreadB: sm.release(2)
     *
     * @param args
     */
    public static void main(String[] args) {

        //创建公平策略的Semaphore对象
        Semaphore sm = new Semaphore(2, true);

        //线程A
        Thread threadA = new Thread(() -> {
            try {
                sm.acquire();
                System.out.println(Thread.currentThread().getName() + " acquire success 1");
                Thread.sleep(1000 * 5);
                System.out.println(Thread.currentThread().getName() + " release 1");
                sm.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadA.setName("threadA");

        //线程B
        Thread threadB = new Thread(() -> {
            try {
                sm.acquire(2);
                System.out.println(Thread.currentThread().getName() + " acquire success 2");
                Thread.sleep(1000 * 5);
                System.out.println(Thread.currentThread().getName() + " release 2");
                sm.release(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadB.setName("threadB");

        //线程C
        Thread threadC = new Thread(() -> {
            try {
                sm.acquire();
                System.out.println(Thread.currentThread().getName() + " acquire success 1");
                Thread.sleep(1000 * 5);
                System.out.println(Thread.currentThread().getName() + " release 1");
                sm.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadC.setName("threadC");


        threadA.start();
        threadB.start();
        threadC.start();
    }
}