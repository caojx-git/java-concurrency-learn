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

import java.util.concurrent.TimeUnit;

/**
 * 一种混合风格的接口，用来标记那些应该在给定延迟时间之后执行的对象。
 * 此接口的实现必须定义一个 compareTo 方法，该方法提供与此接口的 getDelay 方法一致的排序。
 * <p>
 * A mix-in style interface for marking objects that should be
 * acted upon after a given delay.
 *
 * <p>An implementation of this interface must define a
 * {@code compareTo} method that provides an ordering consistent with
 * its {@code getDelay} method.
 *
 * @author Doug Lea
 * @since 1.5
 */
public interface Delayed extends Comparable<Delayed> {

    /**
     * 返回与此对象相关的剩余有效时间，以给定的时间单位表示.
     * <p>
     * Returns the remaining delay associated with this object, in the
     * given time unit.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate
     * that the delay has already elapsed
     */
    long getDelay(TimeUnit unit);
}
