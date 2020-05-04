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


/**
 * 源码分析参考文章：https://segmentfault.com/a/1190000016629668
 * <p>
 * J.U.C中的Future接口是“Future模式”的多线程设计模式的实现，可以让调用方以异步方式获取任务的执行结果。而FutureTask便是这样一类支持异步返回结果的任务，
 * 既然是任务就需要实现Runnable接口，同时又要支持异步功能，所以又需要实现Future接口。J.U.C为了方便，新定义了一个接口——RunnableFuture，
 * 该接口同时继承Runnable和Future，代表支持异步处理的任务，而FutureTask便是它的默认实现。
 * <p>
 * A {@link Future} that is {@link Runnable}. Successful execution of
 * the {@code run} method causes completion of the {@code Future}
 * and allows access to its results.
 *
 * @param <V> The result type returned by this Future's {@code get} method
 * @author Doug Lea
 * @see FutureTask
 * @see Executor
 * @since 1.6
 */
public interface RunnableFuture<V> extends Runnable, Future<V> {
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */
    void run();
}
