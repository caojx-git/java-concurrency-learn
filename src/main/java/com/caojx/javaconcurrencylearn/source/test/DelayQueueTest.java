package com.caojx.javaconcurrencylearn.source.test;


import java.time.format.DateTimeFormatter;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 源码分析参考文章：https://segmentfault.com/a/1190000016388106
 * <p>
 * 示例中，我们创建了一个生产者，一个消费者，生产者不断得入队元素，每个元素都会有个截止有效期；
 * 消费者不断得从队列者获取元素。从输出可以看出，消费者每次获取到的元素都是有效期最小的，且都是已经失效了的。
 * （因为DelayQueue每次出队只会删除有效期最小且已经过期的元素）
 *
 * @author caojx created on 2020/4/21 2:58 下午
 */
public class DelayQueueTest {

    public static void main(String[] args) {
        DelayQueue<Data> queue = new DelayQueue<>();

        Thread c1 = new Thread(new Consumer(queue), "consumer-1");
        Thread p1 = new Thread(new Producer(queue), "producer-1");

        c1.start();
        p1.start();
    }
}

/**
 * 队列元素
 * <p>
 * 队列元素必须实现Delayed接口，我们先来定义一个Data类，作为队列元素
 * <p>
 * 1.每个元素的time字段保存失效时间点）的纳秒形式（构造时指定，比如当前时间+60s）；
 * 2.seqno字段表示元素序号，每个元素唯一，仅用于失效时间点一致的元素之间的比较。
 * 3.getDelay方法返回元素的剩余有效时间，可以根据入参的TimeUnit选择时间的表示形式（秒、微妙、纳秒等），一般选择纳秒以提高精度；
 * 4.compareTo方法用于比较两个元素的大小，以便在队列中排序。由于DelayQueue基于优先级队列实现，所以内部是“堆”的形式，
 * 我们定义的规则是先失效的元素将先出队，所以先失效元素应该在堆顶，即compareTo方法返回结果<0的元素优先出队；
 */
class Data implements Delayed {
    private static final AtomicLong atomic = new AtomicLong(0);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss-n");

    // 数据的失效时间点
    private final long time;

    // 序号
    private final long seqno;

    /**
     * @param deadline 数据失效时间点
     */
    public Data(long deadline) {
        this.time = deadline;
        this.seqno = atomic.getAndIncrement();
    }

    /**
     * 返回剩余有效时间
     *
     * @param unit 时间单位
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.time - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    /**
     * 比较两个Delayed对象的大小, 比较顺序如下:
     * 1. 如果是对象本身, 返回0;
     * 2. 比较失效时间点, 先失效的返回-1,后失效的返回1;
     * 3. 比较元素序号, 序号小的返回-1, 否则返回1.
     * 4. 非Data类型元素, 比较剩余有效时间, 剩余有效时间小的返回-1,大的返回1,相同返回0
     */
    @Override
    public int compareTo(Delayed other) {
        if (other == this)  // compare zero if same object
            return 0;

        if (other instanceof Data) {
            Data x = (Data) other;

            // 优先比较失效时间
            long diff = this.time - x.time;
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;

            else if (this.seqno < x.seqno)    // 剩余时间相同则比较序号
                return -1;
            else
                return 1;
        }

        // 一般不会执行到此处，除非元素不是Data类型
        long diff = this.getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
        return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    @Override
    public String toString() {
        return "Data{" +
                "time=" + time +
                ", seqno=" + seqno +
                "}, isValid=" + isValid();
    }

    private boolean isValid() {
        return this.getDelay(TimeUnit.NANOSECONDS) > 0;
    }
}

/**
 * 生产者
 */
class Producer implements Runnable {
    private final DelayQueue<Data> queue;

    public Producer(DelayQueue<Data> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {

            long currentTime = System.nanoTime();
            long validTime = ThreadLocalRandom.current().nextLong(1000000000L, 7000000000L);

            Data data = new Data(currentTime + validTime);
            queue.put(data);

            System.out.println(Thread.currentThread().getName() + ": put " + data);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * 消费者
 */
class Consumer implements Runnable {
    private final DelayQueue<Data> queue;

    public Consumer(DelayQueue<Data> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Data data = queue.take();
                System.out.println(Thread.currentThread().getName() + ": take " + data);

                Thread.yield();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}