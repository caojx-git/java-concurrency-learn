package com.caojx.javaconcurrencylearn.example.atomic;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * AtomicIntegerArray 使用示例
 * <p>
 * JDK提供了三种类型的原子数组：AtomicIntegerArray、AtomicLongArray、AtomicReferenceArray。这里演示AtomicIntegerArray的使用，他们的实现都大同小异，
 * 其实阅读源码也可以发现，这些数组原子类与对应的普通原子类相比，只是多了通过索引找到内存中元素地址的操作而已。
 * <p>
 * 注意：原子数组并不是说可以让线程以原子方式一次性地操作数组中所有元素的数组。
 * 而是指对于数组中的每个元素，可以以原子方式进行操作。
 * <p>
 * 代码第new AtomicIntegerArray(10)声明了一个内含10个元素的数组。定义了一个AddThread线程对数组内10个元素进行累加操作，每个元素各加1000次。
 * <p>
 * 开启new Thread[10]，10个这样的线程。因此，可以预测，如果线程安全，数组内10个元素的值必然都是10000。反之，如果线程不安全，则部分或者全部数值会小于10000。
 */
@Slf4j
@ThreadSafe
public class AtomicIntegerArrayExample {

    static AtomicIntegerArray arr = new AtomicIntegerArray(10);

    public static class AddThread implements Runnable {
        @Override
        public void run() {
            for (int k = 0; k < 10000; k++) {
                arr.getAndIncrement(k % arr.length());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] ts = new Thread[10];
        for (int k = 0; k < 10; k++) {
            ts[k] = new Thread(new AddThread());
        }
        for (int k = 0; k < 10; k++) {
            ts[k].start();
        }
        for (int k = 0; k < 10; k++) {
            ts[k].join();
        }
        log.info("结果：{}", arr);
    }
}
