package com.caojx.javaconcurrencylearn.example.abc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 三线程按顺序交替打印ABC, 打印10次
 * <p>
 * 参考：https://www.jianshu.com/p/f79fa5aafb44
 *
 * @author caojx created on 2021/3/29 11:20 上午
 */
public class ABC_Condition {

    private static Lock lock = new ReentrantLock();

    private static Condition A = lock.newCondition();
    private static Condition B = lock.newCondition();
    private static Condition C = lock.newCondition();

    static class ThreadA extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    System.out.print("A");
                    B.signal(); // A执行完唤醒B线程
                    A.await(); // A释放lock锁
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    static class ThreadB extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    System.out.print("B");
                    C.signal();// B执行完唤醒C线程
                    B.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    static class ThreadC extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    System.out.println("C");
                    A.signal();// C执行完唤醒A线程
                    C.await();// C释放lock锁，当前面B线程执行后会通过C.signal()唤醒该线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new ThreadA().start();
        new ThreadB().start();
        new ThreadC().start();
    }
}