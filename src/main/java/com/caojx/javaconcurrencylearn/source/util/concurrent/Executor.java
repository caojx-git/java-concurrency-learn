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
 * 源码分析参考文章：https://segmentfault.com/a/1190000016586578
 * <p>
 * Executor是JDK1.5时，随着J.U.C引入的一个接口，引入该接口的主要目的是解耦任务本身和任务的执行。我们之前通过线程执行一个任务时，往往需要先创建一个线程，然后调用线程的start方法来执行任务
 * new Thread(new(RunnableTask())).start();
 * 而Executor接口解耦了任务和任务的执行，该接口只有一个方法，入参为待执行的任务
 *
 * <p>
 * An object that executes submitted {@link Runnable} tasks. This
 * interface provides a way of decoupling task submission from the
 * mechanics of how each task will be run, including details of thread
 * use, scheduling, etc.  An {@code Executor} is normally used
 * instead of explicitly creating threads. For example, rather than
 * invoking {@code new Thread(new(RunnableTask())).start()} for each
 * of a set of tasks, you might use:
 * <p>
 * 我们可以像下面这样执行任务，而不必关心线程的创建：
 * <pre>
 * Executor executor = <em>anExecutor</em>;
 * executor.execute(new RunnableTask1());
 * executor.execute(new RunnableTask2());
 * ...
 * </pre>
 * <p>
 * 由于Executor仅仅是一个接口，所以根据其实现的不同，执行任务的具体方式也不尽相同，比如：
 * <p>
 * However, the {@code Executor} interface does not strictly
 * require that execution be asynchronous. In the simplest case, an
 * executor can run the submitted task immediately in the caller's
 * thread:
 * <p>
 * ①同步执行任务
 * DirectExecutor是一个同步任务执行器，对于传入的任务，只有执行完成后execute才会返回。
 * <pre> {@code
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 * }}</pre>
 * <p>
 * More typically, tasks are executed in some thread other
 * than the caller's thread.  The executor below spawns a new thread
 * for each task.
 * <p>
 * ②异步执行任务
 * ThreadPerTaskExecutor是一个异步任务执行器，对于每个任务，执行器都会创建一个新的线程去执行任务。
 *
 * <pre> {@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }}</pre>
 * <p>
 * Many {@code Executor} implementations impose some sort of
 * limitation on how and when tasks are scheduled.  The executor below
 * serializes the submission of tasks to a second executor,
 * illustrating a composite executor.
 * <p>
 * ③对任务进行排队执行
 * SerialExecutor 会对传入的任务进行排队（FIFO顺序），然后从队首取出一个任务执行。
 *
 * <pre> {@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
 *   final Executor executor;
 *   Runnable active;
 *
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *
 *   public synchronized void execute(final Runnable r) {
 *     tasks.offer(new Runnable() {
 *       public void run() {
 *         try {
 *           r.run();
 *         } finally {
 *           scheduleNext();
 *         }
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }}</pre>
 * <p>
 * The {@code Executor} implementations provided in this package
 * implement {@link ExecutorService}, which is a more extensive
 * interface.  The {@link ThreadPoolExecutor} class provides an
 * extensible thread pool implementation. The {@link Executors} class
 * provides convenient factory methods for these Executors.
 *
 * <p>Memory consistency effects: Actions in a thread prior to
 * submitting a {@code Runnable} object to an {@code Executor}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * its execution begins, perhaps in another thread.
 *
 * @author Doug Lea
 * @since 1.5
 */
public interface Executor {

    /**
     * 执行给定的Runnable任务.
     * 根据Executor的实现不同, 具体执行方式也不相同.
     * <p>
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     *                                    accepted for execution
     * @throws NullPointerException       if command is null
     */
    void execute(Runnable command);
}
