package com.caojx.javaconcurrencylearn.example.lock;

import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock 乐观读、悲观读案例
 *
 * 下边两句话来自：https://blog.csdn.net/barlay/article/details/83715524
 * StampedLock是Java8引入的一种新的所机制，简单的理解，可以认为它是读写锁的一个改进版本，读写锁虽然分离了读和写的功能，使得读与读之间可以完全并发，但是读和写之间依然是冲突的，读锁会完全阻塞写锁，它使用的依然是悲观的锁策略。如果有大量的读线程，他也有可能引起写线程的饥饿
 * 而StampedLock则提供了一种乐观的读策略，这种乐观策略的锁非常类似于无锁的操作，使得乐观锁完全不会阻塞写线程
 */
public class LockExample4 {

    class Point {
        private double x, y;
        private final StampedLock sl = new StampedLock();

        void move(double deltaX, double deltaY) { // an exclusively locked method
            long stamp = sl.writeLock(); // 涉及对共享资源的修改，使用写锁-独占操作
            try {
                x += deltaX;
                y += deltaY;
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        /**
         * 下面看看乐观读锁访问共享资源案例（写锁降级为读锁）
         * 使用乐观读锁访问共享资源
         * 注意：乐观读锁在保证数据一致性上需要拷贝一份要操作的变量到方法栈，并且在操作数据时候可能其他写线程已经修改了数据，
         * 而我们操作的是方法栈里面的数据，也就是一个快照，所以最多返回的不是最新的数据，但是一致性还是得到保障的。
         *
         * 整体逻辑：
         * 乐观读的时候，先获取乐观锁，然后业务层面假设是没有写线程在同时竞争数据资源的，先获取读之前的写状态并记录，等读完后再比较读之前的状态和现在的状态是否相等，如果相等就表示没有写操作执行过，否则就进入到悲观读逻辑（有点儿像偏向锁和轻量级锁的升级）。
         *
         * @return
         */
        double distanceFromOrigin() { // A read-only method
            long stamp = sl.tryOptimisticRead(); // 非阻塞获取版本信息
            double currentX = x, currentY = y;  // 拷贝共享资源到本地方法栈中
            if (!sl.validate(stamp)) { // 如果有写锁被占用，可能造成数据不一致，所以要切换到普通读锁模式
                stamp = sl.readLock();  // 切换回普通读锁模式
                try {
                    currentX = x; // 拷贝共享资源到本地方法栈中
                    currentY = y;
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return Math.sqrt(currentX * currentX + currentY * currentY);
        }

        // 下面是悲观读锁案例 （读锁升级为写锁）
         void  moveIfAtOrigin(double newX, double newY) { // upgrade
            // Could instead start with optimistic, not read mode
            long stamp = sl.readLock();
            try {
                while (x == 0.0 && y == 0.0) { // 循环，检查当前状态是否符合
                    long ws = sl.tryConvertToWriteLock(stamp); // 将读锁转为写锁
                    if (ws != 0L) { // 这是确认转为写锁是否成功
                        stamp = ws; // 如果成功 替换票据
                        x = newX; // 进行状态改变
                        y = newY;  // 进行状态改变
                        break;
                    } else { // 如果不能成功转换为写锁
                        sl.unlockRead(stamp);  // 显式释放读锁
                        stamp = sl.writeLock();  // 显式直接进行写锁 然后再通过循环再试
                    }
                }
            } finally {
                sl.unlock(stamp); // 释放读锁或写锁
            }
        }
    }
}
