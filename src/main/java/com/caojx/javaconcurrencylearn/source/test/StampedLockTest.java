package com.caojx.javaconcurrencylearn.source.test;

import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock类，在JDK1.8时引入，是对读写锁ReentrantReadWriteLock的增强，该类提供了一些功能，优化了读锁、写锁的访问，同时使读写锁之间可以互相转换，更细粒度控制并发
 * <p>
 * StampedLock的主要特点概括一下，有以下几点：
 * <p>
 * 1.所有获取锁的方法，都返回一个邮戳（Stamp），Stamp为0表示获取失败，其余都表示成功；
 * 2.所有释放锁的方法，都需要一个邮戳（Stamp），这个Stamp必须是和成功获取锁时得到的Stamp一致；
 * 3.StampedLock是不可重入的；（如果一个线程已经持有了写锁，再去获取写锁的话就会造成死锁）
 * 4.StampedLock有三种访问模式：
 * ①Reading（读模式）：功能和ReentrantReadWriteLock的读锁类似
 * ②Writing（写模式）：功能和ReentrantReadWriteLock的写锁类似
 * ③Optimistic reading（乐观读模式）：这是一种优化的读模式。
 * 5.StampedLock支持读锁和写锁的相互转换
 * 我们知道RRW中，当线程获取到写锁后，可以降级为读锁，但是读锁是不能直接升级为写锁的。
 * StampedLock提供了读锁和写锁相互转换的功能，使得该类支持更多的应用场景。
 * 6.无论写锁还是读锁，都不支持Conditon等待
 * <p>
 * 参考文章：https://segmentfault.com/a/1190000015808032
 * <p>
 * <p>
 * <p>
 * StampedLock的等待队列与RRW的CLH队列相比，有以下特点：
 * <p>
 * 当入队一个线程时，如果队尾是读结点，不会直接链接到队尾，而是链接到该读结点的cowait链中，cowait链本质是一个栈；
 * 当入队一个线程时，如果队尾是写结点，则直接链接到队尾；
 * 唤醒线程的规则和AQS类似，都是首先唤醒队首结点。区别是StampedLock中，当唤醒的结点是读结点时，会唤醒该读结点的cowait链中的所有读结点（顺序和入栈顺序相反，也就是后进先出）。
 * <p>
 * 另外，StampedLock使用时要特别小心，避免锁重入的操作，在使用乐观读锁时也需要遵循相应的调用模板，防止出现数据不一致的问题。
 *
 *
 * 下边两句话来自：https://blog.csdn.net/barlay/article/details/83715524
 * StampedLock是Java8引入的一种新的所机制，简单的理解，可以认为它是读写锁的一个改进版本，读写锁虽然分离了读和写的功能，使得读与读之间可以完全并发，但是读和写之间依然是冲突的，读锁会完全阻塞写锁，它使用的依然是悲观的锁策略。如果有大量的读线程，他也有可能引起写线程的饥饿
 * 而StampedLock则提供了一种乐观的读策略，这种乐观策略的锁非常类似于无锁的操作，使得乐观锁完全不会阻塞写线程
 *
 * @author caojx created on 2020/4/6 5:14 下午
 */
public class StampedLockTest {

    private static final StampedLock sl = new StampedLock();

    /**
     * 示例分析：
     * 假设现在有三个线程：ThreadA、ThreadB、ThreadC、ThreadD。操作如下：
     * <p>
     * ThreadA调用writeLock, 获取写锁
     * ThreadB调用readLock, 获取读锁
     * ThreadC调用readLock, 获取读锁
     * ThreadD调用writeLock, 获取写锁
     * ThreadE调用readLock, 获取读锁
     *
     * @param args
     */
    public static void main(String[] args) {
        StampedLockTest stampedLockTest = new StampedLockTest();
        Thread threadA = new Thread(() -> stampedLockTest.writeA());
        threadA.setName("threadA");

        Thread threadB = new Thread(() -> stampedLockTest.readB());
        threadB.setName("threadB");

        Thread threadC = new Thread(() -> stampedLockTest.readC());
        threadC.setName("threadC");

        Thread threadD = new Thread(() -> stampedLockTest.writeD());
        threadD.setName("threadD");

        Thread threadE = new Thread(() -> stampedLockTest.readE());
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

    }


    public void writeA() {
        long stamp = sl.writeLock();
        try {
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000 * 5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    public void readB() {
        long stamp = sl.readLock();
        try {
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000 * 5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            sl.unlockRead(stamp);
        }
    }

    public void readC() {
        long stamp = sl.readLock();
        try {
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000 * 5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            sl.unlockRead(stamp);
        }
    }

    public void writeD() {
        long stamp = sl.writeLock();
        try {
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000 * 5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    public void readE() {
        long stamp = sl.readLock();
        try {
            System.out.println(Thread.currentThread().getName() + " start");
            Thread.sleep(1000 * 5);
            System.out.println(Thread.currentThread().getName() + " end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            sl.unlockRead(stamp);
        }
    }

}