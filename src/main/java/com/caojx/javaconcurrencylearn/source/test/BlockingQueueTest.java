package com.caojx.javaconcurrencylearn.source.test;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 参考：https://segmentfault.com/a/1190000016296278
 * <p>
 * 利用BlockingQueue来实现生产者-消费者模式。在生产者-消费者模式中，
 * 一共有四类角色：生产者、消费者、消息队列、消息体。我们利用BlockingQueue来实现消息队列。
 *
 * @author caojx created on 2020/4/19 5:04 下午
 */
public class BlockingQueueTest {

    /**
     * 生产者
     */
    static final class Producer implements Runnable {

        private Channel channel;

        public Producer(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {

            while (true) {
                String v = String.valueOf(ThreadLocalRandom.current().nextInt());
                Data data = new Data(v);
                try {
                    channel.put(data);
                    System.out.println(Thread.currentThread().getName() + " produce :" + data);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Thread.yield();
            }
        }
    }

    /**
     * 消费者
     */
    static final class Consumer implements Runnable {
        private final Channel channel;

        public Consumer(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            while (true) {

                try {
                    Object obj = channel.take();
                    System.out.println(Thread.currentThread().getName() + " consume :" + obj.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Thread.yield();
            }
        }
    }

    /**
     * 通道类
     */
    static final class  Channel {

        private final BlockingQueue blockingQueue;


        public Channel(BlockingQueue blockingQueue) {
            this.blockingQueue = blockingQueue;
        }

        public Object take() throws InterruptedException {
            return blockingQueue.take();
        }

        public void put(Object o) throws InterruptedException {
            blockingQueue.put(o);
        }

    }

    /**
     * 数据/消息
     */
    static final class  Data<T> implements Serializable {
        private T data;

        public Data(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "data=" + data +
                    '}';
        }
    }

    public static void main(String[] args) {
        BlockingQueue blockingQueue = new ArrayBlockingQueue(8);
        Channel channel = new Channel(blockingQueue);

        Producer p = new Producer(channel);
        Consumer c1 = new Consumer(channel);
        Consumer c2 = new Consumer(channel);

        new Thread(p).start();
        new Thread(c1).start();
        new Thread(c2).start();
    }
}