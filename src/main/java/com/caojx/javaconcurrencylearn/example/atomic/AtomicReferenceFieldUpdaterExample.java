package com.caojx.javaconcurrencylearn.example.atomic;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * AtomicReferenceFieldUpdaterExample 使用示例，原子性的去更新某个实例中的某个属性
 * <p>
 * AtomicIntegerFieldUpdater主要是让普通变量也享受原子操作
 *
 * @author caojx
 * @version $Id: AtomicReferenceFieldUpdaterExample.java,v 1.0 2019-07-23 23:23 caojx
 * @date 2019-07-23 23:23
 */
@Slf4j
@ThreadSafe
public class AtomicReferenceFieldUpdaterExample {

    /**
     * 原子性的去更新某个实例中的某个属性
     * 构造函数：
     * 第一个参数为值所在的对象
     * 第二个参数为对象中的属性名，该属性必须要求为 volatile 修饰，且为非static
     */
    private static AtomicIntegerFieldUpdater<AtomicReferenceFieldUpdaterExample> updater = AtomicIntegerFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterExample.class, "count");

    private volatile int count = 100;

    public static void main(String[] args) {

        AtomicReferenceFieldUpdaterExample example5 = new AtomicReferenceFieldUpdaterExample();

        //如果 对象example5 的值 = 100， 则将值更新为 120
        if (updater.compareAndSet(example5, 100, 120)) {
            log.info("update success 1, {}", example5.getCount());
        }

        if (updater.compareAndSet(example5, 100, 120)) {
            log.info("update success 2, {}", example5.getCount());
        } else {
            log.info("update failed, {}", example5.getCount());
        }

    }

    public int getCount() {
        return count;
    }
}