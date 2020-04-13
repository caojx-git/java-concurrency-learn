package com.caojx.javaconcurrencylearn.example.atomic;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * AtomicReferenceFieldUpdaterExample 使用示例，原子性的去更新某个实例中的某个属性
 * <p>
 * AtomicIntegerFieldUpdater 主要是让普通变量也享受原子操作
 * <p>
 * 在java.util.concurrent.atomic包中，有三个比较特殊的原子类：AtomicIntegerFieldUpdater、AtomicLongFieldUpdater、AtomicReferenceFieldUpdater。
 * 通过名称可以看到，这几类的功能大致相同，只是针对的类型有所不同，所谓AtomicXXXFieldUpdater，就是可以以一种线程安全的方式操作非线程安全对象的某些字段，
 * AtomicIntegerFieldUpdater只能处理int原始类型的字段，AtomicLongFieldUpdater只能处理long原始类型的字段，AtomicReferenceFieldUpdater可以处理所有引用类型的字段。
 * <p>
 * 这里演示AtomicIntegerFieldUpdater的使用
 *
 * @author caojx
 * @version $Id: AtomicReferenceFieldUpdaterExample.java,v 1.0 2019-07-23 23:23 caojx
 * @date 2019-07-23 23:23
 */
@Slf4j
@ThreadSafe
public class AtomicReferenceFieldUpdaterExample {

    /**
     * 原子性的去更新某个实例中的某个属性，根据源码AtomicIntegerFieldUpdaterImpl可以得出结论
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