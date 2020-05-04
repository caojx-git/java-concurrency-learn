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
 * 源码分析参考文章：https://segmentfault.com/a/1190000016586578
 * 周期任务的调度——ScheduledExecutorService
 * 在工业环境中，我们可能希望提交给执行器的某些任务能够定时执行或周期性地执行，这时我们可以自己实现Executor接口来创建符合我们需要的类，
 * Doug Lea已经考虑到了这类需求，所以在ExecutorService的基础上，又提供了一个接口——ScheduledExecutorService，该接口也是在JDK1.5时，随着J.U.C引入的
 * ScheduledExecutorService提供了一系列schedule方法，可以在给定的延迟后执行提交的任务，或者每个指定的周期执行一次提交的任务
 * <p>
 * <p>
 * An {@link ExecutorService} that can schedule commands to run after a given
 * delay, or to execute periodically.
 *
 * <p>The {@code schedule} methods create tasks with various delays
 * and return a task object that can be used to cancel or check
 * execution. The {@code scheduleAtFixedRate} and
 * {@code scheduleWithFixedDelay} methods create and execute tasks
 * that run periodically until cancelled.
 *
 * <p>Commands submitted using the {@link Executor#execute(Runnable)}
 * and {@link ExecutorService} {@code submit} methods are scheduled
 * with a requested delay of zero. Zero and negative delays (but not
 * periods) are also allowed in {@code schedule} methods, and are
 * treated as requests for immediate execution.
 *
 * <p>All {@code schedule} methods accept <em>relative</em> delays and
 * periods as arguments, not absolute times or dates. It is a simple
 * matter to transform an absolute time represented as a {@link
 * java.util.Date} to the required form. For example, to schedule at
 * a certain future {@code date}, you can use: {@code schedule(task,
 * date.getTime() - System.currentTimeMillis(),
 * TimeUnit.MILLISECONDS)}. Beware however that expiration of a
 * relative delay need not coincide with the current {@code Date} at
 * which the task is enabled due to network time synchronization
 * protocols, clock drift, or other factors.
 *
 * <p>The {@link Executors} class provides convenient factory methods for
 * the ScheduledExecutorService implementations provided in this package.
 *
 * <h3>Usage Example</h3>
 * <p>
 * Here is a class with a method that sets up a ScheduledExecutorService
 * to beep every ten seconds for an hour:
 *
 * <pre> {@code
 * import static java.util.concurrent.TimeUnit.*;
 * class BeeperControl {
 *   private final ScheduledExecutorService scheduler =
 *     Executors.newScheduledThreadPool(1);
 *
 *   public void beepForAnHour() {
 *     final Runnable beeper = new Runnable() {
 *       public void run() { System.out.println("beep"); }
 *     };
 *     final ScheduledFuture<?> beeperHandle =
 *       scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
 *     scheduler.schedule(new Runnable() {
 *       public void run() { beeperHandle.cancel(true); }
 *     }, 60 * 60, SECONDS);
 *   }
 * }}</pre>
 *
 * @author Doug Lea
 * @since 1.5
 */
public interface ScheduledExecutorService extends ExecutorService {

    /**
     * 提交一个待执行的任务，并在给定的延迟后执行该任务
     * <p>
     * Creates and executes a one-shot action that becomes enabled
     * after the given delay.
     *
     * @param command the task to execute                               待执行的任务
     * @param delay   the time from now to delay execution              延迟时间
     * @param unit    the time unit of the delay parameter              延迟时间单位
     * @return a ScheduledFuture representing pending completion of
     * the task and whose {@code get()} method will return
     * {@code null} upon completion
     * @throws RejectedExecutionException if the task cannot be
     *                                    scheduled for execution
     * @throws NullPointerException       if command is null
     */
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay, TimeUnit unit);

    /**
     * 提交一个待执行的任务（具有返回值），并在给定的延迟后执行该任务.
     * <p>
     * Creates and executes a ScheduledFuture that becomes enabled after the
     * given delay.
     *
     * @param callable the function to execute                                  待执行的任务
     * @param delay    the time from now to delay execution                     延迟时间
     * @param unit     the time unit of the delay parameter                     延迟时间的单位
     * @param <V>      the type of the callable's result                        返回值类型
     * @return a ScheduledFuture that can be used to extract result or cancel
     * @throws RejectedExecutionException if the task cannot be
     *                                    scheduled for execution
     * @throws NullPointerException       if callable is null
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay, TimeUnit unit);

    /**
     * 提交一个待执行的任务
     * 该任务在 initialDelay 后开始执行，然后在 initialDelay+period 后执行，接着在initialDelay + 2 * period 后执行, 依此类推.
     * <p>
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the given
     * period; that is executions will commence after
     * {@code initialDelay} then {@code initialDelay+period}, then
     * {@code initialDelay + 2 * period}, and so on.
     * If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.  If any execution of this task
     * takes longer than its period, then subsequent executions
     * may start late, but will not concurrently execute.
     *
     * @param command      the task to execute                                      待执行的任务
     * @param initialDelay the time to delay first execution                        首次执行的延迟时间
     * @param period       the period between successive executions                 连续执行之间的周期
     * @param unit         the time unit of the initialDelay and period parameters  延迟时间的单位
     * @return a ScheduledFuture representing pending completion of
     * the task, and whose {@code get()} method will throw an
     * exception upon cancellation
     * @throws RejectedExecutionException if the task cannot be
     *                                    scheduled for execution
     * @throws NullPointerException       if command is null
     * @throws IllegalArgumentException   if period less than or equal to zero
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);

    /**
     * 提交一个待执行的任务。
     * 该任务在 initialDelay 后开始执行，随后在每一次执行终止和下一次执行开始之间都存在给定的延迟
     * 如果任务的任一执行遇到异常，就会取消后续执行，否则，只能通过执行程序的取消或终止方法来终止该任务。
     *
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the
     * given delay between the termination of one execution and the
     * commencement of the next.  If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.
     *
     * @param command      the task to execute                                      待执行的任务
     * @param initialDelay the time to delay first execution                        首次执行的延迟时间
     * @param delay        the delay between the termination of one                 一次执行终止和下一次执行开始之间的延迟
     *                     execution and the commencement of the next
     * @param unit         the time unit of the initialDelay and delay parameters   延迟时间单位
     * @return a ScheduledFuture representing pending completion of
     * the task, and whose {@code get()} method will throw an
     * exception upon cancellation
     * @throws RejectedExecutionException if the task cannot be
     *                                    scheduled for execution
     * @throws NullPointerException       if command is null
     * @throws IllegalArgumentException   if delay less than or equal to zero
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit);

}
