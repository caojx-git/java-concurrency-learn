package com.caojx.javaconcurrencylearn.example.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试ReentrantLock中Condition的使用
 */
@Slf4j
public class LockExample6 {

    public static void main(String[] args) {
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();

        //线程1
        new Thread(() -> {
            try {
                reentrantLock.lock(); //线程1，获取锁，加入AQS等待队列
                log.info("wait signal"); // 1
                condition.await(); //线程1就从我们正常的AQS队列里移除了，其实就是锁的释放，接着他马上他又加入到了Condition的等待队列里边去（大家可以回忆一下，我们在讲AQS的时候，他有两个队列，这个时候他用到了第二个队列） //注意必须在调用condition.await()之前调用reentrantLock.lock()代码获取同步监视器
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("get signal"); // 4，线程2调用reentrantLock.unlock()方法释放锁，释放锁之后AQS中只剩线程1，于是AQS释放锁按照从头到尾唤醒线程，线程1就被唤醒了，于是线程1继续开始执行就得到第四个是得到信号（get signal）输出
            reentrantLock.unlock();
        }).start();

        //线程2
        new Thread(() -> {
            reentrantLock.lock(); //线程2因为线程1释放锁的关系线程2被唤醒，并判断线程2是否可以取到锁，于是线程2获取锁也加入到了AQS等待中，因此输出了（get lock）
            log.info("get lock"); // 2
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            condition.signalAll(); //发送信号,执行了发送信号（condition.signalAll()）这个方法，紧接着就输出了我们下面的发送信号（send signal ~ ），这个时候Condition的等待队列里面有我们线程1的一个节点，于是他就被取出来了，加入到了AQS的等待队列里边，需要注意的是，这个时候线程1没有被唤醒，只是放到我们下边这个图里面的上面的队列里面（Sync queue）
            log.info("send signal ~ "); // 3
            reentrantLock.unlock(); //发送信号完毕，调用reentrantLock.unlock() 方法，线程2调用reentrantLock.unlock()方法释放锁，释放锁之后AQS中只剩线程1，于是AQS释放锁按照从头到尾唤醒线程，线程1就被唤醒了
        }).start();
    }
}
