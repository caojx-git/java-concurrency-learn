package com.caojx.javaconcurrencylearn.example.atomic;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * AtomicReference 使用示例
 *
 * AtomicReference与AtomicInteger类似, 只是里面封装了一个对象, 而不是int, 对引用进行修改
 *
 * @author caojx
 * @version $Id: AtomicReferenceExample.java,v 1.0 2019-07-23 23:23 caojx
 * @date 2019-07-23 23:23
 */
@Slf4j
@ThreadSafe
public class AtomicReferenceExample {

    private static AtomicReference<Integer> count = new AtomicReference<>(0);

    public static void main(String[] args) {

        count.compareAndSet(0, 2); // count = 2
        count.compareAndSet(0, 1); // no
        count.compareAndSet(1, 3); // no
        count.compareAndSet(2, 4); // count = 4
        count.compareAndSet(3, 5); // no
        log.info("count:{}", count.get());
    }
}