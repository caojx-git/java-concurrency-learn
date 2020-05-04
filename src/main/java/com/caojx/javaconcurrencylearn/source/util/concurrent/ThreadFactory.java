/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.caojx.javaconcurrencylearn.source.util.concurrent;

import java.util.concurrent.Executors;

/**
 * 既然返回的是一个线程池，那么就涉及线程的创建，一般我们需要通过 new Thread ()这种方法创建一个新线程，
 * 但是我们可能希望设置一些线程属性，比如名称、守护程序状态、ThreadGroup 等等，线程池中的线程非常多，如果每个线程都这样手动配置势必非常繁琐，
 * 而ThreadFactory 作为一个线程工厂可以让我们从这些繁琐的线程状态设置的工作中解放出来，还可以由外部指定ThreadFactory实例，以决定线程的具体创建方式
 * Executors提供了静态内部类，实现了ThreadFactory接口，最简单且常用的就是下面这个DefaultThreadFactory
 * <p>
 * An object that creates new threads on demand.  Using thread factories
 * removes hardwiring of calls to {@link Thread#Thread(Runnable) new Thread},
 * enabling applications to use special thread subclasses, priorities, etc.
 *
 * <p>
 * The simplest implementation of this interface is just:
 * <pre> {@code
 * class SimpleThreadFactory implements ThreadFactory {
 *   public Thread newThread(Runnable r) {
 *     return new Thread(r);
 *   }
 * }}</pre>
 * <p>
 * The {@link Executors#defaultThreadFactory} method provides a more
 * useful simple implementation, that sets the created thread context
 * to known values before returning it.
 *
 * @author Doug Lea
 * @since 1.5
 */
public interface ThreadFactory {

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    Thread newThread(Runnable r);
}
