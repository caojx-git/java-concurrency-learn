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
 * 源码分析参考文章：https://segmentfault.com/a/1190000016767676#item-1
 * Future模式简介
 * Future模式是Java多线程设计模式中的一种常见模式，它的主要作用就是异步地执行任务，并在需要的时候获取结果。我们知道，一般调用一个函数，需要等待函数执行完成，调用线程才会继续往下执行，如果是一些计算密集型任务，需要等待的时间可能就会比较长。
 * 如果读者对经济学有些了解，或是了解金融衍生品的话，对Future这个单词应该不会陌生，Future在经济学中出现的频率相当之高，比如关于现金流的折算，其中的终值，英文就是Future value。常见的金融衍生品，期货、远期的英文分别是Futures、Financial future。
 * 我们之前说了，Future模式可以理解为一种凭证，拿着该凭证在将来的某个时间点可以取到我想要的东西，这其实就和期货、远期有点类似了，期货、远期也是双方制定协议或合同，然后在约定的某个时间点，拿着合同进行资金或实物的交割。可见，Future模式的命名是很有深意且很恰当的。
 * <p>
 * 凭证
 * Future模式可以让调用方获取任务的一个凭证，以便将来拿着凭证去获取任务结果，凭证需要具有以下特点：
 * 1.在将来某个时间点，可以通过凭证获取任务的结果；
 * 2.可以支持取消。
 * 从以上两点来看，我们首先想到的方式就是对Callable任务进行包装，包装成一个凭证，然后返回给调用方。
 * J.U.C提供了Future接口和它的实现类——FutureTask来满足我们的需求，{@link FutureTask} 就是真实的“凭证”，Future则是该凭证的接口
 * <p>
 * Future接口：
 * Future接口很简单，提供了isCancelled和isDone两个方法监控任务的执行状态，一个cancel方法用于取消任务的执行。两个get方法用于获取任务的执行结果，如果任务未执行完成，除非设置超时，否则调用线程将会阻塞。
 * 此外，为了能够被线程或线程池执行任务，凭证还需要实现Runnable接口，所以J.U.C还提供了一个RunnableFuture接口，其实就是组合了Runnable和Future接口：{@link RunnableFuture}
 * <p>
 * A {@code Future} represents the result of an asynchronous
 * computation.  Methods are provided to check if the computation is
 * complete, to wait for its completion, and to retrieve the result of
 * the computation.  The result can only be retrieved using method
 * {@code get} when the computation has completed, blocking if
 * necessary until it is ready.  Cancellation is performed by the
 * {@code cancel} method.  Additional methods are provided to
 * determine if the task completed normally or was cancelled. Once a
 * computation has completed, the computation cannot be cancelled.
 * If you would like to use a {@code Future} for the sake
 * of cancellability but not provide a usable result, you can
 * declare types of the form {@code Future<?>} and
 * return {@code null} as a result of the underlying task.
 *
 * <p>
 * <b>Sample Usage</b> (Note that the following classes are all
 * made-up.)
 * <pre> {@code
 * interface ArchiveSearcher { String search(String target); }
 * class App {
 *   ExecutorService executor = ...
 *   ArchiveSearcher searcher = ...
 *   void showSearch(final String target)
 *       throws InterruptedException {
 *     Future<String> future
 *       = executor.submit(new Callable<String>() {
 *         public String call() {
 *             return searcher.search(target);
 *         }});
 *     displayOtherThings(); // do other things while searching
 *     try {
 *       displayText(future.get()); // use future
 *     } catch (ExecutionException ex) { cleanup(); return; }
 *   }
 * }}</pre>
 * <p>
 * The {@link FutureTask} class is an implementation of {@code Future} that
 * implements {@code Runnable}, and so may be executed by an {@code Executor}.
 * For example, the above construction with {@code submit} could be replaced by:
 * <pre> {@code
 * FutureTask<String> future =
 *   new FutureTask<String>(new Callable<String>() {
 *     public String call() {
 *       return searcher.search(target);
 *   }});
 * executor.execute(future);}</pre>
 *
 * <p>Memory consistency effects: Actions taken by the asynchronous computation
 * <a href="package-summary.html#MemoryVisibility"> <i>happen-before</i></a>
 * actions following the corresponding {@code Future.get()} in another thread.
 *
 * @param <V> The result type returned by this Future's {@code get} method
 * @author Doug Lea
 * @see FutureTask
 * @see Executor
 * @since 1.5
 */
public interface Future<V> {

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when {@code cancel} is called,
     * this task should never run.  If the task has already started,
     * then the {@code mayInterruptIfRunning} parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     *
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}.  Subsequent calls to {@link #isCancelled}
     * will always return {@code true} if this method returned {@code true}.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Returns {@code true} if this task was cancelled before it completed
     * normally.
     *
     * @return {@code true} if this task was cancelled before it completed
     */
    boolean isCancelled();

    /**
     * Returns {@code true} if this task completed.
     * <p>
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * {@code true}.
     *
     * @return {@code true} if this task completed
     */
    boolean isDone();

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException    if the computation threw an
     *                               exception
     * @throws InterruptedException  if the current thread was interrupted
     *                               while waiting
     */
    V get() throws InterruptedException, ExecutionException;

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException    if the computation threw an
     *                               exception
     * @throws InterruptedException  if the current thread was interrupted
     *                               while waiting
     * @throws TimeoutException      if the wait timed out
     */
    V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException;
}
