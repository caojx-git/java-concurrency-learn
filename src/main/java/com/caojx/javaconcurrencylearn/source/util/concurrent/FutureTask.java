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

import com.caojx.javaconcurrencylearn.source.util.concurrent.locks.LockSupport;

import java.util.concurrent.TimeUnit;

/**
 * 源码分析参考文章：
 * https://segmentfault.com/a/1190000016629668
 * https://segmentfault.com/a/1190000016767676#item-1
 * <p>
 * J.U.C中的Future接口是“Future模式”的多线程设计模式的实现，可以让调用方以异步方式获取任务的执行结果。而FutureTask便是这样一类支持异步返回结果的任务，
 * 既然是任务就需要实现Runnable接口，同时又要支持异步功能，所以又需要实现Future接口。J.U.C为了方便，新定义了一个接口——RunnableFuture，
 * 该接口同时继承Runnable和Future，代表支持异步处理的任务，而FutureTask便是它的默认实现。
 * <p>
 * 在J.U.C提供的Future模式中，最重要的就是FutureTask类，FutureTask是在JDK1.5时，随着J.U.C一起引入的，它代表着一个异步任务，这个任务一般提交给Executor执行，当然也可以由调用方直接调用run方法运行。
 * 既然是任务，就有状态，FutureTask一共给任务定义了7种状态：
 * 1.NEW：表示任务的初始化状态；
 * 2.COMPLETING：表示任务已执行完成（正常完成或异常完成），但任务结果或异常原因还未设置完成，属于中间状态；
 * 3.NORMAL：表示任务已经执行完成（正常完成），且任务结果已设置完成，属于最终状态；
 * 4.EXCEPTIONAL：表示任务已经执行完成（异常完成），且任务异常已设置完成，属于最终状态；
 * 5.CANCELLED：表示任务还没开始执行就被取消（非中断方式），属于最终状态；
 * 6.INTERRUPTING：表示任务还没开始执行就被取消（中断方式），正式被中断前的过渡状态，属于中间状态；
 * 7.INTERRUPTED：表示任务还没开始执行就被取消（中断方式），且已被中断，属于最终状态。
 * <p>
 * 注意：
 * 1.FutureTask虽然支持任务的取消（cancel方法），但是只有当任务是初始化（NEW状态）时才有效，否则cancel方法直接返回false；
 * 2.当执行任务时（run方法），无论成功或异常，都会先过渡到COMPLETING状态，直到任务结果设置完成后，才会进入响应的终态。
 * <p>
 * JDK1.7之前，FutureTask通过内部类实现了AQS框架来实现功能。 JDK1.7及以后，则改变为直接通过Unsafe类CAS操作state状态字段来进行同步。
 * <p>
 * 总结:
 * 本章我们从源头开始，讲解了Future模式的来龙去脉，并以J.U.C中的Future模式为例，分析了Future模式的组件以及核心实现类——FutureTask，最后回顾了ScheduledFutureTask中定义的内部异步任务类——ScheduledFutureTask。
 * 理解Future模式的关键就是理解它的两个核心组件，可以类比生活中的凭证来理解这一概念，不必拘泥于Java多线程设计模式中Future模式的类关系图。
 * 1.真实任务类
 * 2.凭证
 * A cancellable asynchronous computation.  This class provides a base
 * implementation of {@link Future}, with methods to start and cancel
 * a computation, query to see if the computation is complete, and
 * retrieve the result of the computation.  The result can only be
 * retrieved when the computation has completed; the {@code get}
 * methods will block if the computation has not yet completed.  Once
 * the computation has completed, the computation cannot be restarted
 * or cancelled (unless the computation is invoked using
 * {@link #runAndReset}).
 *
 * <p>A {@code FutureTask} can be used to wrap a {@link Callable} or
 * {@link Runnable} object.  Because {@code FutureTask} implements
 * {@code Runnable}, a {@code FutureTask} can be submitted to an
 * {@link Executor} for execution.
 *
 * <p>In addition to serving as a standalone class, this class provides
 * {@code protected} functionality that may be useful when creating
 * customized task classes.
 *
 * @param <V> The result type returned by this FutureTask's {@code get} methods
 * @author Doug Lea
 * @since 1.5
 */
public class FutureTask<V> implements RunnableFuture<V> {
    /*
     * Revision notes: This differs from previous versions of this
     * class that relied on AbstractQueuedSynchronizer, mainly to
     * avoid surprising users about retaining interrupt status during
     * cancellation races. Sync control in the current design relies
     * on a "state" field updated via CAS to track completion, along
     * with a simple Treiber stack to hold waiting threads.
     *
     * Style note: As usual, we bypass overhead of using
     * AtomicXFieldUpdaters and instead directly use Unsafe intrinsics.
     */

    /**
     * FutureTask的字段定义非常简单，State标识任务的当前状态，状态之间的转换通过Unsafe来操作，所有操作都基于自旋+CAS完成
     * <p>
     * The run state of this task, initially NEW.  The run state
     * transitions to a terminal state only in methods set,
     * setException, and cancel.  During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique
     * and cannot be further modified.
     * <p>
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state;
    private static final int NEW = 0;
    private static final int COMPLETING = 1;
    private static final int NORMAL = 2;
    private static final int EXCEPTIONAL = 3;
    private static final int CANCELLED = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED = 6;

    /**
     * 真正的任务
     * <p>
     * The underlying callable; nulled out after running
     */
    private Callable<V> callable;
    /**
     * 记录结果或异常
     * <p>
     * The result to return or exception to throw from get()
     */
    private Object outcome; // non-volatile, protected by state reads/writes
    /**
     * 保存正在执行任务的线程
     * <p>
     * The thread running the callable; CASed during run()
     */
    private volatile Thread runner;
    /**
     * 无锁栈（Treiber stack）
     * 保存等待线程
     * <p>
     * waiters指向一个“无锁栈”，该栈保存着所有等待线程，我们知道当调用FutureTask的get方法时，
     * 如果任务没有完成，则调用线程会被阻塞，其实就是将线程包装成WaitNode结点保存到waiters指向的栈中
     * <p>
     * Treiber stack of waiting threads
     */
    private volatile WaitNode waiters;

    /**
     * 将同步状态映射为执行结果.
     * <p>
     * report会根据任务的状态进行映射，如果任务是Normal状态，说明正常执行完成，则返回任务结果；
     * 如果任务被取消（CANCELLED或INTERRUPTED），则抛出CancellationException；
     * 其它情况则抛出ExecutionException。
     * <p>
     * Returns result or throws exception for completed task.
     *
     * @param s completed state value
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)    // 如果任务是Normal状态，说明正常执行完成，则返回任务结果
            return (V) x;
        if (s >= CANCELLED) // 如果任务被取消（CANCELLED或INTERRUPTED），则抛出CancellationException
            throw new CancellationException();

        throw new ExecutionException((Throwable) x); // 其它情况则抛出ExecutionException。
    }

    /**
     * 构造：FutureTask在构造时可以接受Runnable或Callable任务，如果是Runnable，则最终包装成Callable
     * <p>
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Callable}.
     *
     * @param callable the callable task
     * @throws NullPointerException if the callable is null
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }

    /**
     * 构造：FutureTask在构造时可以接受Runnable或Callable任务，如果是Runnable，则最终包装成Callable
     * <p>
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Runnable}, and arrange that {@code get} will return the
     * given result on successful completion.
     *
     * @param runnable the runnable task
     * @param result   the result to return on successful completion. If
     *                 you don't need a particular result, consider using
     *                 constructions of the form:
     *                 {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     */
    public FutureTask(Runnable runnable, V result) {
        // 内部使用RunnableAdapter对Runnable对象做了适配，返回Callable适配对象
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;       // ensure visibility of callable
    }

    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    public boolean isDone() {
        return state != NEW;
    }

    /**
     * 任务的取消
     * <p>
     * cancel方法用于取消任务，参数mayInterruptIfRunning如果为true，表示中断正在执行任务的线程，否则仅仅是将任务状态置为CANCELLED
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     * @return
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        // 仅NEW状态下可以取消任务
        if (!(state == NEW &&
                UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                        mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false;
        try {    // in case call to interrupt throws exception
            if (mayInterruptIfRunning) {    // 中断任务
                try {
                    Thread t = runner;
                    if (t != null)
                        t.interrupt();
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            // 任务取消后，最终调用finishCompletion方法，释放所有在栈上等待的线程
            finishCompletion();
        }
        return true;
    }

    /**
     * 结果获取
     *
     * @throws CancellationException {@inheritDoc}
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING) // 如果当前任务的状态是NEW或COMPLETING，会调用awaitDone阻塞线程。否则会认为任务已经完成，直接通过report方法映射结果
            s = awaitDone(false, 0L);
        return report(s);   // 映射任务执行结果
    }

    /**
     * 结果获取
     * <p>
     * FutureTask可以通过get方法获取任务结果，如果需要限时等待，可以调用get(long timeout, TimeUnit unit)。
     *
     * @throws CancellationException {@inheritDoc}
     */
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
                (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);   // 映射任务执行结果
    }

    /**
     * Protected method invoked when this task transitions to state
     * {@code isDone} (whether normally or via cancellation). The
     * default implementation does nothing.  Subclasses may override
     * this method to invoke completion callbacks or perform
     * bookkeeping. Note that you can query status inside the
     * implementation of this method to determine whether this task
     * has been cancelled.
     */
    protected void done() {
    }

    /**
     * 正常执行完成后，会调用set方法设置任务执行结果
     * <p>
     * Sets the result of this future to the given value unless
     * this future has already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon successful completion of the computation.
     *
     * @param v the value
     */
    protected void set(V v) {
        // NEW->COMPLETING->NORMAL
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
            finishCompletion();
        }
    }

    /**
     * 如果任务执行过程中抛出异常，则调用setException设置异常信息
     * <p>
     * Causes this future to report an {@link ExecutionException}
     * with the given throwable as its cause, unless this future has
     * already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon failure of the computation.
     *
     * @param t the cause of failure
     */
    protected void setException(Throwable t) {
        // NEW->COMPLETING->EXCEPTIONAL
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = t;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
            finishCompletion();
        }
    }

    /**
     * 任务的运行
     * 1.首先判断当前任务的state是否等于NEW,如果不为NEW则说明任务或者已经执行过，或者已经被取消，直接返回
     * 2.正常执行完成后，会调用set方法设置任务执行结果
     * 3.如果任务执行过程中抛出异常，则调用setException设置异常信息
     */
    public void run() {
        // 仅当任务为NEW状态时, 才能执行任务
        if (state != NEW ||
                !UNSAFE.compareAndSwapObject(this, runnerOffset,
                        null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex); // 如果任务执行过程中抛出异常，则调用setException设置异常信息
                }
                if (ran)
                    // 正常执行完成后，会调用set方法设置任务执行结果
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     * FutureTask的runAndReset方法与run方法的区别就是当任务正常执行完成后，不会设置任务的最终状态（即保持NEW状态），以便任务重复执行
     * <p>
     * Executes the computation without setting its result, and then
     * resets this future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @return {@code true} if successfully run and reset
     */
    protected boolean runAndReset() {
        // // 仅NEW状态的任务可以执行
        if (state != NEW ||
                !UNSAFE.compareAndSwapObject(this, runnerOffset,
                        null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**
     * Ensures that any interrupt from a possible cancel(true) is only
     * delivered to a task while in run or runAndReset.
     */
    private void handlePossibleCancellationInterrupt(int s) {
        // It is possible for our interrupter to stall before getting a
        // chance to interrupt us.  Let's spin-wait patiently.
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // wait out pending interrupt

        // assert state == INTERRUPTED;

        // We want to clear any interrupt we may have received from
        // cancel(true).  However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //
        // Thread.interrupted();
    }

    /**
     * Simple linked list nodes to record waiting threads in a Treiber
     * stack.  See other classes such as Phaser and SynchronousQueue
     * for more detailed explanation.
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode() {
            thread = Thread.currentThread();
        }
    }

    /**
     * 唤醒栈上的所有等待线程.
     * <p>
     * Removes and signals all waiting threads, invokes done(), and
     * nulls out callable.
     */
    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null; ) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                for (; ; ) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }

        done(); // 钩子方法

        callable = null;        // to reduce footprint
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion
     */
    private int awaitDone(boolean timed, long nanos)
            throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        for (; ; ) {
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                return s;
            } else if (s == COMPLETING) // cannot time out yet
                Thread.yield();
            else if (q == null)
                q = new WaitNode();
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                        q.next = waiters, q);
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            } else
                LockSupport.park(this);
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid
     * accumulating garbage.  Internal nodes are simply unspliced
     * without CAS since it is harmless if they are traversed anyway
     * by releasers.  To avoid effects of unsplicing from already
     * removed nodes, the list is retraversed in case of an apparent
     * race.  This is slow when there are a lot of nodes, but we don't
     * expect lists to be long enough to outweigh higher-overhead
     * schemes.
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (; ; ) {          // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    } else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                            q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
