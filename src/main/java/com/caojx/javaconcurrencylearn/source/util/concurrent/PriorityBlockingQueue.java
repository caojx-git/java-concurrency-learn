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

import com.caojx.javaconcurrencylearn.source.util.concurrent.locks.Condition;
import com.caojx.javaconcurrencylearn.source.util.concurrent.locks.ReentrantLock;
import sun.misc.SharedSecrets;

import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.TimeUnit;

/**
 * 源码分析参考文章：https://segmentfault.com/a/1190000016353839
 * <p>
 * PriorityBlockingQueue，是在JDK1.5时，随着J.U.C包引入的一种阻塞队列，它实现了BlockingQueue接口，底层基于堆实现
 * PriorityBlockingQueue是一种无界阻塞队列，在构造的时候可以指定队列的初始容量。具有如下特点：
 * 1.PriorityBlockingQueue与之前介绍的阻塞队列最大的不同之处就是：它是一种优先级队列，也就是说元素并不是以FIFO的方式出/入队，而是以按照权重大小的顺序出队；
 * 2.PriorityBlockingQueue是真正的无界队列（仅受内存大小限制），它不像ArrayBlockingQueue那样构造时必须指定最大容量，也不像LinkedBlockingQueue默认最大容量为Integer.MAX_VALUE；
 * 3.由于PriorityBlockingQueue是按照元素的权重进入排序，所以队列中的元素必须是可以比较的，也就是说元素必须实现Comparable接口；
 * 4.由于PriorityBlockingQueue无界队列，所以插入元素永远不会阻塞线程；
 * 5.PriorityBlockingQueue底层是一种基于数组实现的堆结构。
 * <p>
 * 注意：堆分为“大顶堆”和“小顶堆”，PriorityBlockingQueue会依据元素的比较方式选择构建大顶堆或小顶堆。比如：如果元素是Integer这种引用类型，那么默认就是“小顶堆”，也就是每次出队都会是当前队列最小的元素。
 * <p>
 * 总结：
 * PriorityBlockingQueue属于比较特殊的阻塞队列，适用于有元素优先级要求的场景。它的内部和ArrayBlockingQueue一样，
 * 使用一个了全局独占锁来控制同时只有一个线程可以进行入队和出队，另外由于该队列是无界队列，所以入队线程并不会阻塞。
 * PriorityBlockingQueue始终保证出队的元素是优先级最高的元素，并且可以定制优先级的规则，
 * 内部通过使用堆（数组形式）来维护元素顺序，它的内部数组是可扩容的，扩容和出/入队可以并发进行。
 * <p>
 * An unbounded {@linkplain BlockingQueue blocking queue} that uses
 * the same ordering rules as class {@link PriorityQueue} and supplies
 * blocking retrieval operations.  While this queue is logically
 * unbounded, attempted additions may fail due to resource exhaustion
 * (causing {@code OutOfMemoryError}). This class does not permit
 * {@code null} elements.  A priority queue relying on {@linkplain
 * Comparable natural ordering} also does not permit insertion of
 * non-comparable objects (doing so results in
 * {@code ClassCastException}).
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.  The Iterator provided in method {@link
 * #iterator()} is <em>not</em> guaranteed to traverse the elements of
 * the PriorityBlockingQueue in any particular order. If you need
 * ordered traversal, consider using
 * {@code Arrays.sort(pq.toArray())}.  Also, method {@code drainTo}
 * can be used to <em>remove</em> some or all elements in priority
 * order and place them in another collection.
 *
 * <p>Operations on this class make no guarantees about the ordering
 * of elements with equal priority. If you need to enforce an
 * ordering, you can define custom classes or comparators that use a
 * secondary key to break ties in primary priority values.  For
 * example, here is a class that applies first-in-first-out
 * tie-breaking to comparable elements. To use it, you would insert a
 * {@code new FIFOEntry(anEntry)} instead of a plain entry object.
 *
 * <pre> {@code
 * class FIFOEntry<E extends Comparable<? super E>>
 *     implements Comparable<FIFOEntry<E>> {
 *   static final AtomicLong seq = new AtomicLong(0);
 *   final long seqNum;
 *   final E entry;
 *   public FIFOEntry(E entry) {
 *     seqNum = seq.getAndIncrement();
 *     this.entry = entry;
 *   }
 *   public E getEntry() { return entry; }
 *   public int compareTo(FIFOEntry<E> other) {
 *     int res = entry.compareTo(other.entry);
 *     if (res == 0 && other.entry != this.entry)
 *       res = (seqNum < other.seqNum ? -1 : 1);
 *     return res;
 *   }
 * }}</pre>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements held in this collection
 * @author Doug Lea
 * @since 1.5
 */
@SuppressWarnings("unchecked")
public class PriorityBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = 5595510919245408276L;

    /*
     * The implementation uses an array-based binary heap, with public
     * operations protected with a single lock. However, allocation
     * during resizing uses a simple spinlock (used only while not
     * holding main lock) in order to allow takes to operate
     * concurrently with allocation.  This avoids repeated
     * postponement of waiting consumers and consequent element
     * build-up. The need to back away from lock during allocation
     * makes it impossible to simply wrap delegated
     * java.util.PriorityQueue operations within a lock, as was done
     * in a previous version of this class. To maintain
     * interoperability, a plain PriorityQueue is still used during
     * serialization, which maintains compatibility at the expense of
     * transiently doubling overhead.
     */

    /**
     * 默认容量.
     * <p>
     * Default array capacity.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * 最大容量.
     * <p>
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 内部堆数组, 保存实际数据, 可以看成一颗二叉树:
     * 对于顶点queue[n], queue[2*n+1]表示左子结点, queue[2*(n+1)]表示右子结点.
     * <p>
     * Priority queue represented as a balanced binary heap: the two
     * children of queue[n] are queue[2*n+1] and queue[2*(n+1)].  The
     * priority queue is ordered by comparator, or by the elements'
     * natural ordering, if comparator is null: For each node n in the
     * heap and each descendant d of n, n <= d.  The element with the
     * lowest value is in queue[0], assuming the queue is nonempty.
     */
    private transient Object[] queue;

    /**
     * 队列中的元素个数
     * <p>
     * The number of elements in the priority queue.
     */
    private transient int size;

    /**
     * 比较器, 如果为null, 表示以元素自身的自然顺序进行比较（元素必须实现Comparable接口）.
     * <p>
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    private transient Comparator<? super E> comparator;

    /**
     * 全局锁
     * <p>
     * Lock used for all public operations
     */
    private final ReentrantLock lock;

    /**
     * 当队列为空时，出队线程在该条件队列上等待.
     * <p>
     * Condition for blocking when empty
     */
    private final Condition notEmpty;

    /**
     * Spinlock for allocation, acquired via CAS.
     */
    private transient volatile int allocationSpinLock;

    /**
     * A plain PriorityQueue used only for serialization,
     * to maintain compatibility with previous versions
     * of this class. Non-null only during serialization/deserialization.
     */
    private PriorityQueue<E> q;

    /**
     * 默认构造器.
     * 默认初始容量11, 以元素自然顺序比较(元素必须实现Comparable接口)
     * <p>
     * Creates a {@code PriorityBlockingQueue} with the default
     * initial capacity (11) that orders its elements according to
     * their {@linkplain Comparable natural ordering}.
     */
    public PriorityBlockingQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    /**
     * 指定初始容量的构造器.
     * 以元素自然顺序比较(元素必须实现Comparable接口)
     * <p>
     * Creates a {@code PriorityBlockingQueue} with the specified
     * initial capacity that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @throws IllegalArgumentException if {@code initialCapacity} is less
     *                                  than 1
     */
    public PriorityBlockingQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * 指定初始容量和比较器的构造器.
     * <p>
     * Creates a {@code PriorityBlockingQueue} with the specified initial
     * capacity that orders its elements according to the specified
     * comparator.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @param comparator      the comparator that will be used to order this
     *                        priority queue.  If {@code null}, the {@linkplain Comparable
     *                        natural ordering} of the elements will be used.
     * @throws IllegalArgumentException if {@code initialCapacity} is less
     *                                  than 1
     */
    public PriorityBlockingQueue(int initialCapacity,
                                 Comparator<? super E> comparator) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.comparator = comparator;
        this.queue = new Object[initialCapacity];
    }

    /**
     * 从已有集合构造队列
     * 如果已经集合是SortedSet或者PriorityBlockingQueue, 则保持原来的元素顺序
     * <p>
     * Creates a {@code PriorityBlockingQueue} containing the elements
     * in the specified collection.  If the specified collection is a
     * {@link SortedSet} or a {@link PriorityQueue}, this
     * priority queue will be ordered according to the same ordering.
     * Otherwise, this priority queue will be ordered according to the
     * {@linkplain Comparable natural ordering} of its elements.
     *
     * @param c the collection whose elements are to be placed
     *          into this priority queue
     * @throws ClassCastException   if elements of the specified collection
     *                              cannot be compared to one another according to the priority
     *                              queue's ordering
     * @throws NullPointerException if the specified collection or any
     *                              of its elements are null
     */
    public PriorityBlockingQueue(Collection<? extends E> c) {
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        boolean heapify = true; // true if not known to be in heap order
        boolean screen = true;  // true if must screen for nulls

        if (c instanceof SortedSet<?>) {    // 如果是有序集合
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            heapify = false;
        } else if (c instanceof PriorityBlockingQueue<?>) { // 如果是优先级队列
            PriorityBlockingQueue<? extends E> pq =
                    (PriorityBlockingQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            screen = false;
            if (pq.getClass() == PriorityBlockingQueue.class) // exact match
                heapify = false;
        }

        // 复制元素到队列
        Object[] a = c.toArray();
        int n = a.length;
        // If c.toArray incorrectly doesn't return Object[], copy it.
        if (a.getClass() != Object[].class)
            a = Arrays.copyOf(a, n, Object[].class);
        if (screen && (n == 1 || this.comparator != null)) {    // 校验是否存在null元素
            for (int i = 0; i < n; ++i)
                if (a[i] == null)
                    throw new NullPointerException();
        }
        this.queue = a;
        this.size = n;
        if (heapify)    // 堆排序
            heapify();
    }

    /**
     * 尝试增加数组以容纳至少一个其他元素（但通常会扩大约50％），
     * 放弃（允许重试）争用（我们预计这种情况很少见）。仅在持锁。
     * <p>
     * Tries to grow array to accommodate at least one more element
     * (but normally expand by about 50%), giving up (allowing retry)
     * on contention (which we expect to be rare). Call only while
     * holding lock.
     *
     * @param array  the heap array
     * @param oldCap the length of the array
     */
    private void tryGrow(Object[] array, int oldCap) {
        lock.unlock(); // must release and then re-acquire main lock    // 扩容和入队/出队可以同时进行, 所以先释放全局锁
        Object[] newArray = null;
        if (allocationSpinLock == 0 &&
                UNSAFE.compareAndSwapInt(this, allocationSpinLockOffset, 0, 1)) {   // allocationSpinLock置1表示正在扩容
            try {
                // 计算新的数组大小
                int newCap = oldCap + ((oldCap < 64) ?
                        (oldCap + 2) : // grow faster if small
                        (oldCap >> 1));
                if (newCap - MAX_ARRAY_SIZE > 0) {    // possible overflow 判断是否溢出
                    int minCap = oldCap + 1;
                    if (minCap < 0 || minCap > MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError();
                    newCap = MAX_ARRAY_SIZE;
                }
                if (newCap > oldCap && queue == array)
                    newArray = new Object[newCap];   // 分配新数组
            } finally {
                allocationSpinLock = 0;
            }
        }
        if (newArray == null) // back off if another thread is allocating
            // 扩容失败（可能有其它线程正在扩容，导致allocationSpinLock竞争失败）
            Thread.yield();


        lock.lock();    // 获取全局锁(因为要修改内部数组queue)
        if (newArray != null && queue == array) {
            queue = newArray;   // 指向新的内部数组
            System.arraycopy(array, 0, newArray, 0, oldCap);
        }
    }

    /**
     * 从dequeue方法可以看出，每次出队的元素都是“堆顶结点”，
     * 对于“小顶堆”就是队列中的最小值，对于“大顶堆”就是队列中的最大值
     * <p>
     * Mechanics for poll().  Call only while holding lock.
     */
    private E dequeue() {
        int n = size - 1;   // n表示出队后的剩余元素个数
        if (n < 0)          // 队列为空, 则返回null
            return null;
        else {
            Object[] array = queue;
            E result = (E) array[0];    // array[0]是堆顶结点, 每次出队都删除堆顶结点
            E x = (E) array[n];         // array[n]是堆的最后一个结点, 也就是二叉树的最右下结点
            array[n] = null;
            Comparator<? super E> cmp = comparator;
            if (cmp == null)
                siftDownComparable(0, x, array, n);
            else
                siftDownUsingComparator(0, x, array, n, cmp);
            size = n;
            return result;
        }
    }

    /**
     * 将元素x插入到array[k]的位置.
     * 然后按照元素的自然顺序进行堆调整——"上浮"，以维持"堆"有序.
     * 最终的结果是一个"小顶堆".
     * <p>
     * siftUpComparable方法的作用其实就是堆的“上浮调整”，可以把堆可以想象成一棵完全二叉树，
     * 每次插入元素都链接到二叉树的最右下方，然后将插入的元素与其父结点比较，如果父结点大，
     * 则交换元素，直到没有父结点比插入的结点大为止。这样就保证了堆顶（二叉树的根结点）一定是最小的元素。
     * <p>
     * Inserts item x at position k, maintaining heap invariant by
     * promoting x up the tree until it is greater than or equal to
     * its parent, or is the root.
     * <p>
     * To simplify and speed up coercions and comparisons. the
     * Comparable and Comparator versions are separated into different
     * methods that are otherwise identical. (Similarly for siftDown.)
     * These methods are static, with heap state as arguments, to
     * simplify use in light of possible comparator exceptions.
     *
     * @param k     the position to fill
     * @param x     the item to insert
     * @param array the heap array
     */
    private static <T> void siftUpComparable(int k, T x, Object[] array) {
        Comparable<? super T> key = (Comparable<? super T>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;        // 相当于(k-1)除2, 就是求k结点的父结点索引parent
            Object e = array[parent];
            if (key.compareTo((T) e) >= 0)     // 如果插入的结点值大于父结点, 则退出
                break;

            // 否则，交换父结点和当前结点的值
            array[k] = e;
            k = parent;
        }
        array[k] = key;
    }

    private static <T> void siftUpUsingComparator(int k, T x, Object[] array,
                                                  Comparator<? super T> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            if (cmp.compare(x, (T) e) >= 0)
                break;
            array[k] = e;
            k = parent;
        }
        array[k] = x;
    }

    /**
     * 堆的"下沉"调整.
     * 删除array[k]对应的结点,并重新调整堆使其有序.
     * <p>
     * Inserts item x at position k, maintaining heap invariant by
     * demoting x down the tree repeatedly until it is less than or
     * equal to its children or is a leaf.
     *
     * @param k     the position to fill        待删除的位置
     * @param x     the item to insert          待比较的健
     * @param array the heap array              堆数组
     * @param n     heap size                   堆的大小
     */
    private static <T> void siftDownComparable(int k, T x, Object[] array, int n) {
        if (n > 0) {
            Comparable<? super T> key = (Comparable<? super T>) x;
            int half = n >>> 1;           // loop while a non-leaf  // 相当于n除2, 即找到索引n对应结点的父结点
            while (k < half) {
                /**
                 * 下述代码中:
                 * c保存k的左右子结点中的较小结点值
                 * child保存较小结点对应的索引
                 */
                int child = (k << 1) + 1; // assume left child is least // k的左子结点
                Object c = array[child];

                int right = child + 1;  // k的右子结点
                if (right < n &&
                        ((Comparable<? super T>) c).compareTo((T) array[right]) > 0)
                    c = array[child = right];
                if (key.compareTo((T) c) <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = key;
        }
    }

    private static <T> void siftDownUsingComparator(int k, T x, Object[] array,
                                                    int n,
                                                    Comparator<? super T> cmp) {
        if (n > 0) {
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && cmp.compare((T) c, (T) array[right]) > 0)
                    c = array[child = right];
                if (cmp.compare(x, (T) c) <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = x;
        }
    }

    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     */
    private void heapify() {
        Object[] array = queue;
        int n = size;
        int half = (n >>> 1) - 1;
        Comparator<? super E> cmp = comparator;
        if (cmp == null) {
            for (int i = half; i >= 0; i--)
                siftDownComparable(i, (E) array[i], array, n);
        } else {
            for (int i = half; i >= 0; i--)
                siftDownUsingComparator(i, (E) array[i], array, n, cmp);
        }
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws ClassCastException   if the specified element cannot be compared
     *                              with elements currently in the priority queue according to the
     *                              priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * Inserts the specified element into this priority queue.
     * As the queue is unbounded, this method will never return {@code false}.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws ClassCastException   if the specified element cannot be compared
     *                              with elements currently in the priority queue according to the
     *                              priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        final ReentrantLock lock = this.lock;   // 加锁
        lock.lock();
        int n, cap;
        Object[] array;
        while ((n = size) >= (cap = (array = queue).length))  // 队列已满, 则进行扩容
            tryGrow(array, cap);
        try {
            Comparator<? super E> cmp = comparator;
            if (cmp == null)                    // 比较器为空, 则按照元素的自然顺序进行堆调整
                siftUpComparable(n, e, array);
            else                                // 比较器非空, 则按照比较器进行堆调整
                siftUpUsingComparator(n, e, array, cmp);
            size = n + 1;                      // 队列元素总数+1
            notEmpty.signal();                 // 唤醒一个可能正在等待的"出队线程"
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 向队列中插入指定元素.
     * 由于队列是无界的，所以不会阻塞线程.
     * <p>
     * Inserts the specified element into this priority queue.
     * As the queue is unbounded, this method will never block.
     *
     * @param e the element to add
     * @throws ClassCastException   if the specified element cannot be compared
     *                              with elements currently in the priority queue according to the
     *                              priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public void put(E e) {
        offer(e); // never need to block
    }

    /**
     * Inserts the specified element into this priority queue.
     * As the queue is unbounded, this method will never block or
     * return {@code false}.
     *
     * @param e       the element to add
     * @param timeout This parameter is ignored as the method never blocks
     * @param unit    This parameter is ignored as the method never blocks
     * @return {@code true} (as specified by
     * {@link BlockingQueue#offer(Object, long, TimeUnit) BlockingQueue.offer})
     * @throws ClassCastException   if the specified element cannot be compared
     *                              with elements currently in the priority queue according to the
     *                              priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e); // never need to block
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 出队一个元素.
     * 如果队列为空, 则阻塞线程.
     *
     * @return
     * @throws InterruptedException
     */
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();   // 获取全局锁
        E result;
        try {
            while ((result = dequeue()) == null)    // 队列为空
                notEmpty.await();                   // 线程在noEmpty条件队列等待
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ((result = dequeue()) == null && nanos > 0)
                nanos = notEmpty.awaitNanos(nanos);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (size == 0) ? null : (E) queue[0];
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the comparator used to order the elements in this queue,
     * or {@code null} if this queue uses the {@linkplain Comparable
     * natural ordering} of its elements.
     *
     * @return the comparator used to order the elements in this queue,
     * or {@code null} if this queue uses the natural
     * ordering of its elements
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Always returns {@code Integer.MAX_VALUE} because
     * a {@code PriorityBlockingQueue} is not capacity constrained.
     *
     * @return {@code Integer.MAX_VALUE} always
     */
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    private int indexOf(Object o) {
        if (o != null) {
            Object[] array = queue;
            int n = size;
            for (int i = 0; i < n; i++)
                if (o.equals(array[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Removes the ith element from queue.
     */
    private void removeAt(int i) {
        Object[] array = queue;
        int n = size - 1;
        if (n == i) // removed last element
            array[i] = null;
        else {
            E moved = (E) array[n];
            array[n] = null;
            Comparator<? super E> cmp = comparator;
            if (cmp == null)
                siftDownComparable(i, moved, array, n);
            else
                siftDownUsingComparator(i, moved, array, n, cmp);
            if (array[i] == moved) {
                if (cmp == null)
                    siftUpComparable(i, moved, array);
                else
                    siftUpUsingComparator(i, moved, array, cmp);
            }
        }
        size = n;
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.  Returns {@code true} if and only if this queue contained
     * the specified element (or equivalently, if this queue changed as a
     * result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i == -1)
                return false;
            removeAt(i);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Identity-based version for use in Itr.remove
     */
    void removeEQ(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = queue;
            for (int i = 0, n = size; i < n; i++) {
                if (o == array[i]) {
                    removeAt(i);
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return indexOf(o) != -1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue.
     * The returned array elements are in no particular order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return Arrays.copyOf(queue, size);
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (n == 0)
                return "[]";
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < n; ++i) {
                Object e = queue[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (i != n - 1)
                    sb.append(',').append(' ');
            }
            return sb.append(']').toString();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = Math.min(size, maxElements);
            for (int i = 0; i < n; i++) {
                c.add((E) queue[0]); // In this order, in case add() throws.
                dequeue();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = queue;
            int n = size;
            size = 0;
            for (int i = 0; i < n; i++)
                array[i] = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If this queue fits in the specified array with room to spare
     * (i.e., the array has more elements than this queue), the element in
     * the array immediately following the end of the queue is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     * <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     * <p>
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this queue
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (a.length < n)
                // Make a new array of a's runtime type, but my contents:
                return (T[]) Arrays.copyOf(queue, size, a.getClass());
            System.arraycopy(queue, 0, a, 0, n);
            if (a.length > n)
                a[n] = null;
            return a;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an iterator over the elements in this queue. The
     * iterator does not return the elements in any particular order.
     *
     * <p>The returned iterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * @return an iterator over the elements in this queue
     */
    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    /**
     * Snapshot iterator that works off copy of underlying q array.
     */
    final class Itr implements Iterator<E> {
        final Object[] array; // Array of all elements
        int cursor;           // index of next element to return
        int lastRet;          // index of last element, or -1 if no such

        Itr(Object[] array) {
            lastRet = -1;
            this.array = array;
        }

        public boolean hasNext() {
            return cursor < array.length;
        }

        public E next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return (E) array[cursor++];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            removeEQ(array[lastRet]);
            lastRet = -1;
        }
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     * <p>
     * For compatibility with previous version of this class, elements
     * are first copied to a java.util.PriorityQueue, which is then
     * serialized.
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        lock.lock();
        try {
            // avoid zero capacity argument
            q = new PriorityQueue<E>(Math.max(size, 1), comparator);
            q.addAll(this);
            s.defaultWriteObject();
        } finally {
            q = null;
            lock.unlock();
        }
    }

    /**
     * Reconstitutes this queue from a stream (that is, deserializes it).
     *
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *                                could not be found
     * @throws java.io.IOException    if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        try {
            s.defaultReadObject();
            int sz = q.size();
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, sz);
            this.queue = new Object[sz];
            comparator = q.comparator();
            addAll(q);
        } finally {
            q = null;
        }
    }

    // Similar to Collections.ArraySnapshotSpliterator but avoids
    // commitment to toArray until needed
    static final class PBQSpliterator<E> implements Spliterator<E> {
        final PriorityBlockingQueue<E> queue;
        Object[] array;
        int index;
        int fence;

        PBQSpliterator(PriorityBlockingQueue<E> queue, Object[] array,
                       int index, int fence) {
            this.queue = queue;
            this.array = array;
            this.index = index;
            this.fence = fence;
        }

        final int getFence() {
            int hi;
            if ((hi = fence) < 0)
                hi = fence = (array = queue.toArray()).length;
            return hi;
        }

        public Spliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new PBQSpliterator<E>(queue, array, lo, index = mid);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            Object[] a;
            int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array) == null)
                fence = (a = queue.toArray()).length;
            if ((hi = fence) <= a.length &&
                    (i = index) >= 0 && i < (index = hi)) {
                do {
                    action.accept((E) a[i]);
                } while (++i < hi);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            if (getFence() > index && index >= 0) {
                @SuppressWarnings("unchecked") E e = (E) array[index++];
                action.accept(e);
                return true;
            }
            return false;
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    /**
     * Returns a {@link Spliterator} over the elements in this queue.
     *
     * <p>The returned spliterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#NONNULL}.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @implNote The {@code Spliterator} additionally reports {@link Spliterator#SUBSIZED}.
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new PBQSpliterator<E>(this, null, 0, -1);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long allocationSpinLockOffset;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = PriorityBlockingQueue.class;
            allocationSpinLockOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("allocationSpinLock"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
