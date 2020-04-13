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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * A reusable synchronization barrier, similar in functionality to
 * {@link java.util.concurrent.CyclicBarrier CyclicBarrier} and
 * {@link java.util.concurrent.CountDownLatch CountDownLatch}
 * but supporting more flexible usage.
 *
 * <p><b>Registration.</b> Unlike the case for other barriers, the
 * number of parties <em>registered</em> to synchronize on a phaser
 * may vary over time.  Tasks may be registered at any time (using
 * methods {@link #register}, {@link #bulkRegister}, or forms of
 * constructors establishing initial numbers of parties), and
 * optionally deregistered upon any arrival (using {@link
 * #arriveAndDeregister}).  As is the case with most basic
 * synchronization constructs, registration and deregistration affect
 * only internal counts; they do not establish any further internal
 * bookkeeping, so tasks cannot query whether they are registered.
 * (However, you can introduce such bookkeeping by subclassing this
 * class.)
 *
 * <p><b>Synchronization.</b> Like a {@code CyclicBarrier}, a {@code
 * Phaser} may be repeatedly awaited.  Method {@link
 * #arriveAndAwaitAdvance} has effect analogous to {@link
 * java.util.concurrent.CyclicBarrier#await CyclicBarrier.await}. Each
 * generation of a phaser has an associated phase number. The phase
 * number starts at zero, and advances when all parties arrive at the
 * phaser, wrapping around to zero after reaching {@code
 * Integer.MAX_VALUE}. The use of phase numbers enables independent
 * control of actions upon arrival at a phaser and upon awaiting
 * others, via two kinds of methods that may be invoked by any
 * registered party:
 *
 * <ul>
 *
 *   <li> <b>Arrival.</b> Methods {@link #arrive} and
 *       {@link #arriveAndDeregister} record arrival.  These methods
 *       do not block, but return an associated <em>arrival phase
 *       number</em>; that is, the phase number of the phaser to which
 *       the arrival applied. When the final party for a given phase
 *       arrives, an optional action is performed and the phase
 *       advances.  These actions are performed by the party
 *       triggering a phase advance, and are arranged by overriding
 *       method {@link #onAdvance(int, int)}, which also controls
 *       termination. Overriding this method is similar to, but more
 *       flexible than, providing a barrier action to a {@code
 *       CyclicBarrier}.
 *
 *   <li> <b>Waiting.</b> Method {@link #awaitAdvance} requires an
 *       argument indicating an arrival phase number, and returns when
 *       the phaser advances to (or is already at) a different phase.
 *       Unlike similar constructions using {@code CyclicBarrier},
 *       method {@code awaitAdvance} continues to wait even if the
 *       waiting thread is interrupted. Interruptible and timeout
 *       versions are also available, but exceptions encountered while
 *       tasks wait interruptibly or with timeout do not change the
 *       state of the phaser. If necessary, you can perform any
 *       associated recovery within handlers of those exceptions,
 *       often after invoking {@code forceTermination}.  Phasers may
 *       also be used by tasks executing in a {@link ForkJoinPool},
 *       which will ensure sufficient parallelism to execute tasks
 *       when others are blocked waiting for a phase to advance.
 *
 * </ul>
 *
 * <p><b>Termination.</b> A phaser may enter a <em>termination</em>
 * state, that may be checked using method {@link #isTerminated}. Upon
 * termination, all synchronization methods immediately return without
 * waiting for advance, as indicated by a negative return value.
 * Similarly, attempts to register upon termination have no effect.
 * Termination is triggered when an invocation of {@code onAdvance}
 * returns {@code true}. The default implementation returns {@code
 * true} if a deregistration has caused the number of registered
 * parties to become zero.  As illustrated below, when phasers control
 * actions with a fixed number of iterations, it is often convenient
 * to override this method to cause termination when the current phase
 * number reaches a threshold. Method {@link #forceTermination} is
 * also available to abruptly release waiting threads and allow them
 * to terminate.
 *
 * <p><b>Tiering.</b> Phasers may be <em>tiered</em> (i.e.,
 * constructed in tree structures) to reduce contention. Phasers with
 * large numbers of parties that would otherwise experience heavy
 * synchronization contention costs may instead be set up so that
 * groups of sub-phasers share a common parent.  This may greatly
 * increase throughput even though it incurs greater per-operation
 * overhead.
 *
 * <p>In a tree of tiered phasers, registration and deregistration of
 * child phasers with their parent are managed automatically.
 * Whenever the number of registered parties of a child phaser becomes
 * non-zero (as established in the {@link #Phaser(Phaser, int)}
 * constructor, {@link #register}, or {@link #bulkRegister}), the
 * child phaser is registered with its parent.  Whenever the number of
 * registered parties becomes zero as the result of an invocation of
 * {@link #arriveAndDeregister}, the child phaser is deregistered
 * from its parent.
 *
 * <p><b>Monitoring.</b> While synchronization methods may be invoked
 * only by registered parties, the current state of a phaser may be
 * monitored by any caller.  At any given moment there are {@link
 * #getRegisteredParties} parties in total, of which {@link
 * #getArrivedParties} have arrived at the current phase ({@link
 * #getPhase}).  When the remaining ({@link #getUnarrivedParties})
 * parties arrive, the phase advances.  The values returned by these
 * methods may reflect transient states and so are not in general
 * useful for synchronization control.  Method {@link #toString}
 * returns snapshots of these state queries in a form convenient for
 * informal monitoring.
 *
 * <p><b>Sample usages:</b>
 *
 * <p>A {@code Phaser} may be used instead of a {@code CountDownLatch}
 * to control a one-shot action serving a variable number of parties.
 * The typical idiom is for the method setting this up to first
 * register, then start the actions, then deregister, as in:
 *
 *  <pre> {@code
 * void runTasks(List<Runnable> tasks) {
 *   final Phaser phaser = new Phaser(1); // "1" to register self
 *   // create and start threads
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         phaser.arriveAndAwaitAdvance(); // await all creation
 *         task.run();
 *       }
 *     }.start();
 *   }
 *
 *   // allow threads to start and deregister self
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 * <p>One way to cause a set of threads to repeatedly perform actions
 * for a given number of iterations is to override {@code onAdvance}:
 *
 *  <pre> {@code
 * void startTasks(List<Runnable> tasks, final int iterations) {
 *   final Phaser phaser = new Phaser() {
 *     protected boolean onAdvance(int phase, int registeredParties) {
 *       return phase >= iterations || registeredParties == 0;
 *     }
 *   };
 *   phaser.register();
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         do {
 *           task.run();
 *           phaser.arriveAndAwaitAdvance();
 *         } while (!phaser.isTerminated());
 *       }
 *     }.start();
 *   }
 *   phaser.arriveAndDeregister(); // deregister self, don't wait
 * }}</pre>
 * <p>
 * If the main task must later await termination, it
 * may re-register and then execute a similar loop:
 *  <pre> {@code
 *   // ...
 *   phaser.register();
 *   while (!phaser.isTerminated())
 *     phaser.arriveAndAwaitAdvance();}</pre>
 *
 * <p>Related constructions may be used to await particular phase numbers
 * in contexts where you are sure that the phase will never wrap around
 * {@code Integer.MAX_VALUE}. For example:
 *
 *  <pre> {@code
 * void awaitPhase(Phaser phaser, int phase) {
 *   int p = phaser.register(); // assumes caller not already registered
 *   while (p < phase) {
 *     if (phaser.isTerminated())
 *       // ... deal with unexpected termination
 *     else
 *       p = phaser.arriveAndAwaitAdvance();
 *   }
 *   phaser.arriveAndDeregister();
 * }}</pre>
 *
 *
 * <p>To create a set of {@code n} tasks using a tree of phasers, you
 * could use code of the following form, assuming a Task class with a
 * constructor accepting a {@code Phaser} that it registers with upon
 * construction. After invocation of {@code build(new Task[n], 0, n,
 * new Phaser())}, these tasks could then be started, for example by
 * submitting to a pool:
 *
 *  <pre> {@code
 * void build(Task[] tasks, int lo, int hi, Phaser ph) {
 *   if (hi - lo > TASKS_PER_PHASER) {
 *     for (int i = lo; i < hi; i += TASKS_PER_PHASER) {
 *       int j = Math.min(i + TASKS_PER_PHASER, hi);
 *       build(tasks, i, j, new Phaser(ph));
 *     }
 *   } else {
 *     for (int i = lo; i < hi; ++i)
 *       tasks[i] = new Task(ph);
 *       // assumes new Task(ph) performs ph.register()
 *   }
 * }}</pre>
 * <p>
 * The best value of {@code TASKS_PER_PHASER} depends mainly on
 * expected synchronization rates. A value as low as four may
 * be appropriate for extremely small per-phase task bodies (thus
 * high rates), or up to hundreds for extremely large ones.
 *
 * <p><b>Implementation notes</b>: This implementation restricts the
 * maximum number of parties to 65535. Attempts to register additional
 * parties result in {@code IllegalStateException}. However, you can and
 * should create tiered phasers to accommodate arbitrarily large sets
 * of participants.
 * <p>
 * Phaser大概的实现原理。
 * <p>
 * 首先，需要存储当前阶段phase、当前阶段的任务数（参与者）parties、未完成参与者的数量，这三个变量我们可以放在一个变量state中存储。
 * <p>
 * 其次，需要一个队列存储先完成的参与者，当最后一个参与者完成任务时，需要唤醒队列中的参与者
 *
 * @author Doug Lea
 * @since 1.7
 */
public class Phaser {
    /*
     * This class implements an extension of X10 "clocks".  Thanks to
     * Vijay Saraswat for the idea, and to Vivek Sarkar for
     * enhancements to extend functionality.
     */

    /**
     * Primary state representation, holding four bit-fields:
     * <p>
     * unarrived  -- the number of parties yet to hit barrier (bits  0-15)
     * parties    -- the number of parties to wait            (bits 16-31)
     * phase      -- the generation of the barrier            (bits 32-62)
     * terminated -- set if barrier is terminated             (bit  63 / sign)
     * <p>
     * Except that a phaser with no registered parties is
     * distinguished by the otherwise illegal state of having zero
     * parties and one unarrived parties (encoded as EMPTY below).
     * <p>
     * To efficiently maintain atomicity, these values are packed into
     * a single (atomic) long. Good performance relies on keeping
     * state decoding and encoding simple, and keeping race windows
     * short.
     * <p>
     * All state updates are performed via CAS except initial
     * registration of a sub-phaser (i.e., one with a non-null
     * parent).  In this (relatively rare) case, we use built-in
     * synchronization to lock while first registering with its
     * parent.
     * <p>
     * The phase of a subphaser is allowed to lag that of its
     * ancestors until it is actually accessed -- see method
     * reconcileState.
     * <p>
     * Phaser使用一个long类型来保存同步状态值State，并按位划分不同区域的含义，通过掩码和位运算进行赋值和操作
     * <p>
     * 同步状态值:
     * unarrived    -- 未到达的参与者数目                  (0-15位)
     * parties      -- 总参与者（需要等待的参与者）数目      (16-31位)
     * phase        -- 当前阶段                          (32-63位)
     * terminated   -- 屏障终止标志                       (第63位)
     * <p>
     * 注意：初始时，state==1，称为EMPTY,表示此Phaser 对象还没有线程来注册过
     * 之所以不是0，是因为0有特殊的含义，比如当所有的线程都到达屏障了，此时 unarrived 也为0
     * <p>
     * 初始时，parties = 0, 而unarrived == 1, 更易于辨别
     */

    // 使用long类型的状态变量，用于存储当前阶段phase、参与者数parties、未完成的参与者数unarrived_count
    // state，状态变量，高32位存储当前阶段phase，中间16位存储参与者的数量，低16位存储未完成参与者的数量
    private volatile long state;

    private static final int MAX_PARTIES = 0xffff;                          // 最大参与者数目 65535，即每个阶段最多有多少个任务
    private static final int MAX_PHASE = Integer.MAX_VALUE;                 // 最多可以有多少个阶段phase
    private static final int PARTIES_SHIFT = 16;                            // 参与者数量的偏移量
    private static final int PHASE_SHIFT = 32;                              // 当前阶段的偏移量
    private static final int UNARRIVED_MASK = 0xffff;      // to mask ints  // 掩码，用于计算未达到的参与者数目，底16位
    private static final long PARTIES_MASK = 0xffff0000L; // to mask longs  // 掩码，用于计算参与者数目，中间16位
    private static final long COUNTS_MASK = 0xffffffffL;                    // counts的掩码，counts等于参与者数和未完成的参与者数的'|'操作
    private static final long TERMINATION_BIT = 1L << 63;                   // 屏障终止标志，第63位为1

    // some special values
    private static final int ONE_ARRIVAL = 1;                               // 1个参与者达到
    private static final int ONE_PARTY = 1 << PARTIES_SHIFT;                // 1个参与者，增加减少参与者时使用
    private static final int ONE_DEREGISTER = ONE_ARRIVAL | ONE_PARTY;      // 注销一个参与者，减少参与者时使用
    private static final int EMPTY = 1;                                     // 初始值，没有参与者时使用

    // The following unpacking methods are usually manually inlined

    // 用于求未完成参与者数量
    private static int unarrivedOf(long s) {
        int counts = (int) s;
        return (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
    }

    // 用于求参与者数量（中间16位），注意int的位置
    private static int partiesOf(long s) {
        return (int) s >>> PARTIES_SHIFT;
    }

    // 用于求阶段数（高32位），注意int的位置
    private static int phaseOf(long s) {
        return (int) (s >>> PHASE_SHIFT);
    }

    // 已完成参与者的数量
    private static int arrivedOf(long s) {
        int counts = (int) s;
        return (counts == EMPTY) ? 0 :
                (counts >>> PARTIES_SHIFT) - (counts & UNARRIVED_MASK);
    }

    /**
     * The parent of this phaser, or null if none
     * <p>
     * 父结点指针，如果没有父结点，则为null
     */
    private final Phaser parent;

    /**
     * The root of phaser tree. Equals this if not in a tree.
     * <p>
     * 根结点指针，如果不在树中，则指向自身
     */
    private final Phaser root;

    /**
     * Heads of Treiber stacks for waiting threads. To eliminate
     * contention when releasing some threads while adding others, we
     * use two of them, alternating across even and odd phases.
     * Subphasers share queues with root to speed up releases.
     * <p>
     * 用于存储已完成参与者所在的线程，根据当前阶段的奇偶性选择不同的队列
     * evenQ和oddQ，已完成的参与者存储的队列，当最后一个参与者完成任务后唤醒队列中的参与者继续执行下一个阶段的任务，或者结束任务。
     * <p>
     * “无锁栈”——Treiber Stack，保存在Phaser树的根结点中，其余所有Phaser子结点共享这两个栈
     * <p>
     * evenQ和oddQ分别是两个"无锁栈"--Treiber Stack的栈顶指针， Phaser 使用Treiber Stack结构来保存等待线程。
     * <p>
     * evenQ是偶数栈，oddQ是奇数栈，分别在内部phase为偶数和奇数下交替使用，这两个栈都挂在根Phaser下
     */
    private final AtomicReference<QNode> evenQ;
    private final AtomicReference<QNode> oddQ;

    private AtomicReference<QNode> queueFor(int phase) {
        return ((phase & 1) == 0) ? evenQ : oddQ;
    }

    /**
     * Returns message string for bounds exceptions on arrival.
     */
    private String badArrive(long s) {
        return "Attempted arrival of unregistered party for " +
                stateToString(s);
    }

    /**
     * Returns message string for bounds exceptions on registration.
     */
    private String badRegister(long s) {
        return "Attempt to register more than " +
                MAX_PARTIES + " parties for " + stateToString(s);
    }

    /**
     * Main implementation for methods arrive and arriveAndDeregister.
     * Manually tuned to speed up and minimize race windows for the
     * common case of just decrementing unarrived field.
     *
     * @param adjust value to subtract from state;
     *               ONE_ARRIVAL for arrive,
     *               ONE_DEREGISTER for arriveAndDeregister
     */
    private int doArrive(int adjust) {
        final Phaser root = this.root;
        for (; ; ) {
            long s = (root == this) ? state : reconcileState();
            int phase = (int) (s >>> PHASE_SHIFT);
            if (phase < 0)
                return phase;
            int counts = (int) s;
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s -= adjust)) {
                if (unarrived == 1) {
                    long n = s & PARTIES_MASK;  // base of next state
                    int nextUnarrived = (int) n >>> PARTIES_SHIFT;
                    if (root == this) {
                        if (onAdvance(phase, nextUnarrived))
                            n |= TERMINATION_BIT;
                        else if (nextUnarrived == 0)
                            n |= EMPTY;
                        else
                            n |= nextUnarrived;
                        int nextPhase = (phase + 1) & MAX_PHASE;
                        n |= (long) nextPhase << PHASE_SHIFT;
                        UNSAFE.compareAndSwapLong(this, stateOffset, s, n);
                        releaseWaiters(phase);
                    } else if (nextUnarrived == 0) { // propagate deregistration
                        phase = parent.doArrive(ONE_DEREGISTER);
                        UNSAFE.compareAndSwapLong(this, stateOffset,
                                s, s | EMPTY);
                    } else
                        phase = parent.doArrive(ONE_ARRIVAL);
                }
                return phase;
            }
        }
    }

    /**
     * Implementation of register, bulkRegister
     * <p>
     * 注册指定数目{#registrations}的参与者
     *
     * @param registrations number to add to both parties and
     *                      unarrived fields. Must be greater than zero.
     */
    private int doRegister(int registrations) {
        // 首先计算注冊后当前State要调整的值adjust，adjust为state应该加的值，注意这里是相当于同时调整parties和unarrived
        // adjustment to state
        long adjust = ((long) registrations << PARTIES_SHIFT) | registrations;
        final Phaser parent = this.parent;
        int phase;
        for (; ; ) {
            // state的值，注意: reconcileState方法比较特殊，因为当出现树形结构时，根结点首先进行phase的更新，所以需要显式同步，使当前结点和根结点保持一致。
            long s = (parent == null) ? state : reconcileState();           // reconcileState()调整当前Phaser的State与root一致
            int counts = (int) s;                                           // state的低32位，也就是parties和unarrived的值
            int parties = counts >>> PARTIES_SHIFT;                         // 参与者数目，count >>> 16
            int unarrived = counts & UNARRIVED_MASK;                        // 未到达的数目
            if (registrations > MAX_PARTIES - parties)                      // 检查是否溢出，超过最大参与者数，抛出异常
                throw new IllegalStateException(badRegister(s));
            phase = (int) (s >>> PHASE_SHIFT);                              // 当前Phaser所处的阶段phase，s >>> 32
            if (phase < 0)
                break;
            if (counts != EMPTY) {                  // not 1st registration // CASE1: 当前Phaser已经注册过参与者，即不是第一个参与者
                if (parent == null || reconcileState() == s) {
                    // unarrived等于0说明当前阶段正在执行onAdvance()方法，等待其执行完毕
                    if (unarrived == 0)             // wait out advance     // 参与者已全部到达栅栏, 当前Phaser正在Advance, 需要阻塞等待这一过程完成（因为此时phase正在变化，增加1到下一个phase）
                        root.internalAwaitAdvance(phase, null);       // 阻塞等待调用的是internalAwaitAdvance方法，其实就是根据当前阶段phase，将线程包装成结点加入到root结点所指向的某个“无锁栈”中
                    else if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s + adjust)) // 否则,直接更新State
                        break;
                }
            } else if (parent == null) {              // 1st root registration // CASE2: 当前Phaser未注册过参与者（第一次注册）,且没有父结点，即是第一个参与者
                // 计算state的值
                long next = ((long) phase << PHASE_SHIFT) | adjust;
                if (UNSAFE.compareAndSwapLong(this, stateOffset, s, next))  // CAS更新当前Phaser的State值，如果成功就跳出循环
                    break;
            } else {                                                            // CASE3: 当前Phaser未注册过参与者（第一次注册）,且有父结点，即多层阶段的处理方式
                synchronized (this) {               // 1st sub registration
                    if (state == s) {               // recheck under lock
                        phase = parent.doRegister(1);               // 向父结点注册一个参与者
                        if (phase < 0)
                            break;
                        // finish registration whenever parent registration
                        // succeeded, even when racing with termination,
                        // since these are part of the same "transaction".
                        while (!UNSAFE.compareAndSwapLong
                                (this, stateOffset, s,
                                        ((long) phase << PHASE_SHIFT) | adjust)) {
                            s = state;
                            phase = (int) (root.state >>> PHASE_SHIFT);
                            // assert (int)s == EMPTY;
                        }
                        break;
                    }
                }
            }
        }
        return phase;
    }

    /**
     * Resolves lagged phase propagation from root if necessary.
     * Reconciliation normally occurs when root has advanced but
     * subphasers have not yet done so, in which case they must finish
     * their own advance by setting unarrived to parties (or if
     * parties is zero, resetting to unregistered EMPTY state).
     * <p>
     * 注意: reconcileState方法比较特殊，因为当出现树形结构时，根结点首先进行phase的更新，所以需要显式同步，使当前结点和根结点保持一致。
     * <p>
     * 调整当前Phaser的同步状态值，和root结点保持一致
     * <p>
     * 因为当出现树形结构时，根结点是最先进行阶段（phase）的跃迁（advance），因此需要显示同步，以便和根结点保持一致
     * reconcileState的作用就是如此，利用自旋+CAS操作修改当前Phaser的阶段
     *
     * @return reconciled state
     */
    private long reconcileState() {
        final Phaser root = this.root;
        long s = state;
        if (root != this) {
            int phase, p;
            // CAS to root phase with current parties, tripping unarrived
            while ((phase = (int) (root.state >>> PHASE_SHIFT)) !=
                    (int) (s >>> PHASE_SHIFT) &&
                    !UNSAFE.compareAndSwapLong
                            (this, stateOffset, s,
                                    s = (((long) phase << PHASE_SHIFT) |
                                            ((phase < 0) ? (s & COUNTS_MASK) :
                                                    (((p = (int) s >>> PARTIES_SHIFT) == 0) ? EMPTY :
                                                            ((s & PARTIES_MASK) | p))))))
                s = state;
        }
        return s;
    }

    /**
     * Creates a new phaser with no initially registered parties, no
     * parent, and initial phase number 0. Any thread using this
     * phaser will need to first register for it.
     * <p>
     * Phaser一共有4个构造器，可以看到，最终其实都是调用了Phaser(Phaser parent, int parties)这个构造器。
     */
    public Phaser() {
        this(null, 0);
    }

    /**
     * Creates a new phaser with the given number of registered
     * unarrived parties, no parent, and initial phase number 0.
     *
     * @param parties the number of parties required to advance to the
     *                next phase
     * @throws IllegalArgumentException if parties less than zero
     *                                  or greater than the maximum number of parties supported
     */
    public Phaser(int parties) {
        this(null, parties);
    }

    /**
     * Equivalent to {@link #Phaser(Phaser, int) Phaser(parent, 0)}.
     *
     * @param parent the parent phaser
     */
    public Phaser(Phaser parent) {
        this(parent, 0);
    }

    /**
     * Creates a new phaser with the given parent and number of
     * registered unarrived parties.  When the given parent is non-null
     * and the given number of parties is greater than zero, this
     * child phaser is registered with its parent.
     * <p>
     * Phaser一共有4个构造器，可以看到，最终其实都是调用了Phaser(Phaser parent, int parties)这个构造器。
     * <p>
     * Phaser(Phaser parent, int parties)的内部实现如下，关键就是给当前的Phaser对象指定父结点时，如果当前Phaser的参与者不为0，需要向父Phaser注册一个参与者（代表当前结点本身）
     *
     * @param parent  the parent phaser
     * @param parties the number of parties required to advance to the
     *                next phase
     * @throws IllegalArgumentException if parties less than zero
     *                                  or greater than the maximum number of parties supported
     */
    public Phaser(Phaser parent, int parties) {
        if (parties >>> PARTIES_SHIFT != 0)                     // 无符号右移16位，不为0，说明parties超过了最大限制数
            throw new IllegalArgumentException("Illegal number of parties");
        int phase = 0;                                          // 初始phase为0
        this.parent = parent;
        if (parent != null) {                                   // 存在父结点
            final Phaser root = parent.root;
            this.root = root;                                   // 当前Phaser对象的root指针，指向树的根结点
            this.evenQ = root.evenQ;                            // 共用父结点的偶数"无锁栈"
            this.oddQ = root.oddQ;                              // 共用父结点的奇数"无锁栈"
            if (parties != 0)                                   // 如果当前Phaser的参与者不为0
                phase = parent.doRegister(1);       // 向父Phaser注册一个参与者（注意，这里是1个）
        } else {
            this.root = this;                                   // root指向自身
            this.evenQ = new AtomicReference<QNode>();          // 创建一个偶数"无锁栈"
            this.oddQ = new AtomicReference<QNode>();           // 创建一个奇数"无锁栈"
        }

        // 更新同步状态值，状态变量state的存储分为三段
        // state，状态变量，高32位存储当前阶段phase，中间16位存储参与者的数量，低16位存储未完成参与者的数量
        this.state = (parties == 0) ? (long) EMPTY :
                ((long) phase << PHASE_SHIFT) |
                        ((long) parties << PARTIES_SHIFT) |
                        ((long) parties);
    }

    /**
     * Adds a new unarrived party to this phaser.  If an ongoing
     * invocation of {@link #onAdvance} is in progress, this method
     * may await its completion before returning.  If this phaser has
     * a parent, and this phaser previously had no registered parties,
     * this child phaser is also registered with its parent. If
     * this phaser is terminated, the attempt to register has
     * no effect, and a negative value is returned.
     * <p>
     * 注册一个参与者
     *
     * @return the arrival phase number to which this registration
     * applied.  If this value is negative, then this phaser has
     * terminated, in which case registration has no effect.
     * @throws IllegalStateException if attempting to register more
     *                               than the maximum supported number of parties
     */
    public int register() {
        return doRegister(1);
    }

    /**
     * Adds the given number of new unarrived parties to this phaser.
     * If an ongoing invocation of {@link #onAdvance} is in progress,
     * this method may await its completion before returning.  If this
     * phaser has a parent, and the given number of parties is greater
     * than zero, and this phaser previously had no registered
     * parties, this child phaser is also registered with its parent.
     * If this phaser is terminated, the attempt to register has no
     * effect, and a negative value is returned.
     * <p>
     * 批量注册参与者
     *
     * @param parties the number of additional parties required to
     *                advance to the next phase
     * @return the arrival phase number to which this registration
     * applied.  If this value is negative, then this phaser has
     * terminated, in which case registration has no effect.
     * @throws IllegalStateException    if attempting to register more
     *                                  than the maximum supported number of parties
     * @throws IllegalArgumentException if {@code parties < 0}
     */
    public int bulkRegister(int parties) {
        if (parties < 0)
            throw new IllegalArgumentException();
        if (parties == 0)
            return getPhase();
        return doRegister(parties);
    }

    /**
     * Arrives at this phaser, without waiting for others to arrive.
     *
     * <p>It is a usage error for an unregistered party to invoke this
     * method.  However, this error may result in an {@code
     * IllegalStateException} only upon some subsequent operation on
     * this phaser, if ever.
     *
     * @return the arrival phase number, or a negative value if terminated
     * @throws IllegalStateException if not terminated and the number
     *                               of unarrived parties would become negative
     */
    public int arrive() {
        return doArrive(ONE_ARRIVAL);
    }

    /**
     * Arrives at this phaser and deregisters from it without waiting
     * for others to arrive. Deregistration reduces the number of
     * parties required to advance in future phases.  If this phaser
     * has a parent, and deregistration causes this phaser to have
     * zero parties, this phaser is also deregistered from its parent.
     *
     * <p>It is a usage error for an unregistered party to invoke this
     * method.  However, this error may result in an {@code
     * IllegalStateException} only upon some subsequent operation on
     * this phaser, if ever.
     *
     * @return the arrival phase number, or a negative value if terminated
     * @throws IllegalStateException if not terminated and the number
     *                               of registered or unarrived parties would become negative
     */
    public int arriveAndDeregister() {
        return doArrive(ONE_DEREGISTER);
    }

    /**
     * Arrives at this phaser and awaits others. Equivalent in effect
     * to {@code awaitAdvance(arrive())}.  If you need to await with
     * interruption or timeout, you can arrange this with an analogous
     * construction using one of the other forms of the {@code
     * awaitAdvance} method.  If instead you need to deregister upon
     * arrival, use {@code awaitAdvance(arriveAndDeregister())}.
     *
     * <p>It is a usage error for an unregistered party to invoke this
     * method.  However, this error may result in an {@code
     * IllegalStateException} only upon some subsequent operation on
     * this phaser, if ever.
     * <p>
     * 参与者到达并等待
     * 当前线程当前阶段执行完毕，等待其它线程完成当前阶段,如果当前线程是该阶段最后一个到达的，则当前线程会执行onAdvance()方法，并唤醒其它线程进入下一个阶段
     * <p>
     * 首先将同步状态值State中的未到达参与者数量减1，然后判断未到达参与者数量是否为0?
     * <p>
     * 如果不为0，则阻塞当前线程，以等待其他参与者到来；
     * <p>
     * 如果为0，说明当前线程是最后一个参与者，如果有父结点则对父结点递归调用该方法。（因为只有根结点的未到达参与者数目为0时），才会进阶phase。
     *
     * @return the arrival phase number, or the (negative)
     * {@linkplain #getPhase() current phase} if terminated
     * @throws IllegalStateException if not terminated and the number
     *                               of unarrived parties would become negative
     */
    public int arriveAndAwaitAdvance() {
        // Specialization of doArrive+awaitAdvance eliminating some reads/paths
        final Phaser root = this.root;
        for (; ; ) {
            long s = (root == this) ? state : reconcileState();                   // 获取同步状态State值
            int phase = (int) (s >>> PHASE_SHIFT);                                // 当前阶段
            if (phase < 0)
                return phase;
            int counts = (int) s;                                                // parties和unarrived的值
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);   // 未到达的参与者数目，unarrived的值（state的低16位）
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));

            // CAS操作：修改state的值，将未到达的参与者数减去1
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s -= ONE_ARRIVAL)) {
                if (unarrived > 1)      // 如果还有未到达的（即当前线程不是最后一个到达者），则调用internalAwaitAdvance()方法自旋或进入队列等待
                    return root.internalAwaitAdvance(phase, null);

                // 到这里说明是最后一个到达的参与者

                if (root != this)       // 如果当前线程是最后一个达到这，但是当前是Phaser，则等待父结点进入下一个阶段
                    return parent.arriveAndAwaitAdvance();
                long n = s & PARTIES_MASK;  // base of next state，参与者数量，n只保留了state中parties的部分，也就是中16位
                int nextUnarrived = (int) n >>> PARTIES_SHIFT;  // parties的值，即下一次需要到达的参与者数量
                if (onAdvance(phase, nextUnarrived))            // 执行onAdvance()方法，返回true表示下一阶段参与者数量为0了，也就是结束了
                    n |= TERMINATION_BIT;
                else if (nextUnarrived == 0)
                    n |= EMPTY;
                else
                    n |= nextUnarrived;                         // n 加上unarrived的值
                int nextPhase = (phase + 1) & MAX_PHASE;        // 下一个阶段=当前阶段加1
                n |= (long) nextPhase << PHASE_SHIFT;           // n 加上下一阶段的值
                if (!UNSAFE.compareAndSwapLong(this, stateOffset, s, n))    // CAS 修改state的值为n
                    return (int) (state >>> PHASE_SHIFT); // terminated
                releaseWaiters(phase);                    // 唤醒其它参与者并进入下一个阶段
                return nextPhase;                         // 返回下一阶段的值
            }
        }
    }

    /**
     * Awaits the phase of this phaser to advance from the given phase
     * value, returning immediately if the current phase is not equal
     * to the given phase value or this phaser is terminated.
     *
     * @param phase an arrival phase number, or negative value if
     *              terminated; this argument is normally the value returned by a
     *              previous call to {@code arrive} or {@code arriveAndDeregister}.
     * @return the next arrival phase number, or the argument if it is
     * negative, or the (negative) {@linkplain #getPhase() current phase}
     * if terminated
     */
    public int awaitAdvance(int phase) {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int) (s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase)
            return root.internalAwaitAdvance(phase, null);
        return p;
    }

    /**
     * Awaits the phase of this phaser to advance from the given phase
     * value, throwing {@code InterruptedException} if interrupted
     * while waiting, or returning immediately if the current phase is
     * not equal to the given phase value or this phaser is
     * terminated.
     *
     * @param phase an arrival phase number, or negative value if
     *              terminated; this argument is normally the value returned by a
     *              previous call to {@code arrive} or {@code arriveAndDeregister}.
     * @return the next arrival phase number, or the argument if it is
     * negative, or the (negative) {@linkplain #getPhase() current phase}
     * if terminated
     * @throws InterruptedException if thread interrupted while waiting
     */
    public int awaitAdvanceInterruptibly(int phase)
            throws InterruptedException {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int) (s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, false, 0L);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
        }
        return p;
    }

    /**
     * Awaits the phase of this phaser to advance from the given phase
     * value or the given timeout to elapse, throwing {@code
     * InterruptedException} if interrupted while waiting, or
     * returning immediately if the current phase is not equal to the
     * given phase value or this phaser is terminated.
     *
     * @param phase   an arrival phase number, or negative value if
     *                terminated; this argument is normally the value returned by a
     *                previous call to {@code arrive} or {@code arriveAndDeregister}.
     * @param timeout how long to wait before giving up, in units of
     *                {@code unit}
     * @param unit    a {@code TimeUnit} determining how to interpret the
     *                {@code timeout} parameter
     * @return the next arrival phase number, or the argument if it is
     * negative, or the (negative) {@linkplain #getPhase() current phase}
     * if terminated
     * @throws InterruptedException if thread interrupted while waiting
     * @throws TimeoutException     if timed out while waiting
     */
    public int awaitAdvanceInterruptibly(int phase,
                                         long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int) (s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, true, nanos);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
            else if (p == phase)
                throw new TimeoutException();
        }
        return p;
    }

    /**
     * Forces this phaser to enter termination state.  Counts of
     * registered parties are unaffected.  If this phaser is a member
     * of a tiered set of phasers, then all of the phasers in the set
     * are terminated.  If this phaser is already terminated, this
     * method has no effect.  This method may be useful for
     * coordinating recovery after one or more tasks encounter
     * unexpected exceptions.
     */
    public void forceTermination() {
        // Only need to change root state
        final Phaser root = this.root;
        long s;
        while ((s = root.state) >= 0) {
            if (UNSAFE.compareAndSwapLong(root, stateOffset,
                    s, s | TERMINATION_BIT)) {
                // signal all threads
                releaseWaiters(0); // Waiters on evenQ
                releaseWaiters(1); // Waiters on oddQ
                return;
            }
        }
    }

    /**
     * Returns the current phase number. The maximum phase number is
     * {@code Integer.MAX_VALUE}, after which it restarts at
     * zero. Upon termination, the phase number is negative,
     * in which case the prevailing phase prior to termination
     * may be obtained via {@code getPhase() + Integer.MIN_VALUE}.
     *
     * @return the phase number, or a negative value if terminated
     */
    public final int getPhase() {
        return (int) (root.state >>> PHASE_SHIFT);
    }

    /**
     * Returns the number of parties registered at this phaser.
     *
     * @return the number of parties
     */
    public int getRegisteredParties() {
        return partiesOf(state);
    }

    /**
     * Returns the number of registered parties that have arrived at
     * the current phase of this phaser. If this phaser has terminated,
     * the returned value is meaningless and arbitrary.
     *
     * @return the number of arrived parties
     */
    public int getArrivedParties() {
        return arrivedOf(reconcileState());
    }

    /**
     * Returns the number of registered parties that have not yet
     * arrived at the current phase of this phaser. If this phaser has
     * terminated, the returned value is meaningless and arbitrary.
     *
     * @return the number of unarrived parties
     */
    public int getUnarrivedParties() {
        return unarrivedOf(reconcileState());
    }

    /**
     * Returns the parent of this phaser, or {@code null} if none.
     *
     * @return the parent of this phaser, or {@code null} if none
     */
    public Phaser getParent() {
        return parent;
    }

    /**
     * Returns the root ancestor of this phaser, which is the same as
     * this phaser if it has no parent.
     *
     * @return the root ancestor of this phaser
     */
    public Phaser getRoot() {
        return root;
    }

    /**
     * Returns {@code true} if this phaser has been terminated.
     *
     * @return {@code true} if this phaser has been terminated
     */
    public boolean isTerminated() {
        return root.state < 0L;
    }

    /**
     * Overridable method to perform an action upon impending phase
     * advance, and to control termination. This method is invoked
     * upon arrival of the party advancing this phaser (when all other
     * waiting parties are dormant).  If this method returns {@code
     * true}, this phaser will be set to a final termination state
     * upon advance, and subsequent calls to {@link #isTerminated}
     * will return true. Any (unchecked) Exception or Error thrown by
     * an invocation of this method is propagated to the party
     * attempting to advance this phaser, in which case no advance
     * occurs.
     *
     * <p>The arguments to this method provide the state of the phaser
     * prevailing for the current transition.  The effects of invoking
     * arrival, registration, and waiting methods on this phaser from
     * within {@code onAdvance} are unspecified and should not be
     * relied on.
     *
     * <p>If this phaser is a member of a tiered set of phasers, then
     * {@code onAdvance} is invoked only for its root phaser on each
     * advance.
     *
     * <p>To support the most common use cases, the default
     * implementation of this method returns {@code true} when the
     * number of registered parties has become zero as the result of a
     * party invoking {@code arriveAndDeregister}.  You can disable
     * this behavior, thus enabling continuation upon future
     * registrations, by overriding this method to always return
     * {@code false}:
     *
     * <pre> {@code
     * Phaser phaser = new Phaser() {
     *   protected boolean onAdvance(int phase, int parties) { return false; }
     * }}</pre>
     *
     * @param phase             the current phase number on entry to this method,
     *                          before this phaser is advanced
     * @param registeredParties the current number of registered parties
     * @return {@code true} if this phaser should terminate
     */
    protected boolean onAdvance(int phase, int registeredParties) {
        return registeredParties == 0;
    }

    /**
     * Returns a string identifying this phaser, as well as its
     * state.  The state, in brackets, includes the String {@code
     * "phase = "} followed by the phase number, {@code "parties = "}
     * followed by the number of registered parties, and {@code
     * "arrived = "} followed by the number of arrived parties.
     *
     * @return a string identifying this phaser, as well as its state
     */
    public String toString() {
        return stateToString(reconcileState());
    }

    /**
     * Implementation of toString and string-based error messages
     */
    private String stateToString(long s) {
        return super.toString() +
                "[phase = " + phaseOf(s) +
                " parties = " + partiesOf(s) +
                " arrived = " + arrivedOf(s) + "]";
    }

    // Waiting mechanics

    /**
     * Removes and signals threads from queue for phase.
     *
     * 把等待线程队列里面的节点都移除了，如果节点有线程的话，将线程唤醒
     */
    private void releaseWaiters(int phase) {
        QNode q;   // first element of queue
        Thread t;  // its thread
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        while ((q = head.get()) != null &&
                q.phase != (int) (root.state >>> PHASE_SHIFT)) {
            if (head.compareAndSet(q, q.next) &&
                    (t = q.thread) != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }

    /**
     * Variant of releaseWaiters that additionally tries to remove any
     * nodes no longer waiting for advance due to timeout or
     * interrupt. Currently, nodes are removed only if they are at
     * head of queue, which suffices to reduce memory footprint in
     * most usages.
     *
     * @return current phase on exit
     */
    private int abortWait(int phase) {
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        for (; ; ) {
            Thread t;
            QNode q = head.get();
            int p = (int) (root.state >>> PHASE_SHIFT);
            if (q == null || ((t = q.thread) != null && q.phase == p))
                return p;
            if (head.compareAndSet(q, q.next) && t != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }

    /**
     * The number of CPUs, for spin control
     */
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The number of times to spin before blocking while waiting for
     * advance, per arrival while waiting. On multiprocessors, fully
     * blocking and waking up a large number of threads all at once is
     * usually a very slow process, so we use rechargeable spins to
     * avoid it when threads regularly arrive: When a thread in
     * internalAwaitAdvance notices another arrival before blocking,
     * and there appear to be enough CPUs available, it spins
     * SPINS_PER_ARRIVAL more times before blocking. The value trades
     * off good-citizenship vs big unnecessary slowdowns.
     */
    static final int SPINS_PER_ARRIVAL = (NCPU < 2) ? 1 : 1 << 8;  //  1 << 8 =256

    /**
     * Possibly blocks and waits for phase to advance unless aborted.
     * Call only on root phaser.
     * <p>
     * 阻塞等待调用的是internalAwaitAdvance方法，其实就是根据当前阶段phase，将线程包装成结点加入到root结点所指向的某个“无锁栈”中
     * <p>
     * 等待onAdvance()方法执行完毕
     * 原理是先自旋一定次数，如果进入下一个阶段，这个方法直接就返回了，
     * 如果自旋一定次数后还没有进入下一个阶段，则当前线程入队列，等待onAdvance()执行完毕唤醒
     *
     * @param phase current phase
     * @param node  if non-null, the wait node to track interrupt and timeout;
     *              if null, denotes noninterruptible wait
     * @return current phase 返回新的阶段
     */
    private int internalAwaitAdvance(int phase, QNode node) {
        // assert root == this;
        releaseWaiters(phase - 1);          // ensure old queue clean   // 清空不用的Treiber Stack（奇偶Stack交替使用），保证队列为空
        boolean queued = false;           // true when node is enqueued        // 入队标识
        int lastUnarrived = 0;            // to increase spins upon change
        int spins = SPINS_PER_ARRIVAL;                                         // 自旋次数
        long s;
        int p;
        while ((p = (int) ((s = state) >>> PHASE_SHIFT)) == phase) {           // 检查当前阶段是否变化，如果变化了说明进入下一个阶段了，这时候就没有必要自旋了
            if (node == null) {           // spinning in noninterruptible mode // 如果node为空，注册的时候传入为空
                int unarrived = (int) s & UNARRIVED_MASK;                      // 未完成参与者数量
                if (unarrived != lastUnarrived && (lastUnarrived = unarrived) < NCPU) // 如果unarrived有变化，增加自旋次数
                    spins += SPINS_PER_ARRIVAL;
                boolean interrupted = Thread.interrupted();
                if (interrupted || --spins < 0) { // need node to record intr         // 自旋次数完了，则新建一个结点
                    node = new QNode(this, phase, false, false, 0L);
                    node.wasInterrupted = interrupted;
                }
            } else if (node.isReleasable()) // done or aborted
                break;
            else if (!queued) {           // push onto queue                     // 结点入队
                AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
                QNode q = node.next = head.get();
                if ((q == null || q.phase == phase) &&
                        (int) (state >>> PHASE_SHIFT) == phase) // avoid stale enq
                    queued = head.compareAndSet(q, node);
            } else {
                try {
                    // 阻塞等待，当前线程进入阻塞状态，跟调用LockSupport.park()一样，等待被唤醒
                    ForkJoinPool.managedBlock(node);
                } catch (InterruptedException ie) {
                    node.wasInterrupted = true;
                }
            }
        }

        // 到这里说明结点所在线程已经被唤醒了
        if (node != null) {
            // 置空结点中的线程
            if (node.thread != null)
                node.thread = null;       // avoid need for unpark()
            if (node.wasInterrupted && !node.interruptible)
                Thread.currentThread().interrupt();
            if (p == phase && (p = (int) (state >>> PHASE_SHIFT)) == phase)
                return abortWait(phase); // possibly clean up on abort
        }
        // 唤醒当前阶段阻塞着的线程
        releaseWaiters(phase);
        return p;
    }

    /**
     * Wait nodes for Treiber stack representing wait queue
     * 完成的参与者放入队列中的节点，这里我们只需要关注thread和next两个属性即可，很明显这是一个单链表，存储着入队的线程。
     * <p>
     * 结点的定义非常简单，内部保存了线程信息和Phsaer对象信息
     * <p>
     * "无锁栈"的结点定义
     * <p>
     * ForkJoinPool.ManagedBlocker是当栈包含ForkJoinWorkerThread类型的QNode阻塞的时候，ForkJoinPool内部会增加一个工作线程来保证并行度，后续讲ForkJoin框架时我们会进行分析
     */
    static final class QNode implements ForkJoinPool.ManagedBlocker {
        final Phaser phaser;
        final int phase;
        final boolean interruptible;
        final boolean timed;
        boolean wasInterrupted;
        long nanos;
        final long deadline;
        volatile Thread thread; // nulled to cancel wait
        QNode next;

        QNode(Phaser phaser, int phase, boolean interruptible,
              boolean timed, long nanos) {
            this.phaser = phaser;
            this.phase = phase;
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.timed = timed;
            this.deadline = timed ? System.nanoTime() + nanos : 0L;
            thread = Thread.currentThread();
        }

        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (phaser.getPhase() != phase) {
                thread = null;
                return true;
            }
            if (Thread.interrupted())
                wasInterrupted = true;
            if (wasInterrupted && interruptible) {
                thread = null;
                return true;
            }
            if (timed) {
                if (nanos > 0L) {
                    nanos = deadline - System.nanoTime();
                }
                if (nanos <= 0L) {
                    thread = null;
                    return true;
                }
            }
            return false;
        }

        public boolean block() {
            if (isReleasable())
                return true;
            else if (!timed)
                LockSupport.park(this);
            else if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = Phaser.class;
            stateOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("state"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
