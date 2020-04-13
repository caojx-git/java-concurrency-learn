package com.caojx.javaconcurrencylearn.source.test;

import lombok.Data;

import java.util.concurrent.Exchanger;

/**
 * Exchanger——交换器，是JDK1.5时引入的一个同步器，从字面上就可以看出，这个类的主要作用是交换数据
 * <p>
 * 利用Exchanger实现生产者-消息者模式
 *
 * 参考文章：https://segmentfault.com/a/1190000015963932
 *
 * @author caojx created on 2020/4/11 10:59 下午
 */
public class ExchangerTest {

    @Data
    public class Message {
        private String V;

        public Message(String v) {
            this.V = v;
        }
    }

    public class Producer implements Runnable {
        private final Exchanger<Message> exchanger;

        public Producer(Exchanger<Message> exchanger) {
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            Message message = new Message(null);
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);

                    message.setV(String.valueOf(i));
                    System.out.println(Thread.currentThread().getName() + ": 生产了数据[" + i + "]");

                    message = exchanger.exchange(message);

                    System.out.println(Thread.currentThread().getName() + ": 交换得到数据[" + String.valueOf(message.getV()) + "]");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public class Consumer implements Runnable {
        private final Exchanger<Message> exchanger;

        public Consumer(Exchanger<Message> exchanger) {
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            Message msg = new Message(null);
            while (true) {
                try {
                    Thread.sleep(1000);
                    msg = exchanger.exchange(msg);
                    System.out.println(Thread.currentThread().getName() + ": 消费了数据[" + msg.getV() + "]");
                    msg.setV(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        ExchangerTest test = new ExchangerTest();
        test.doExchange();
    }

    public void doExchange() {
        Exchanger<Message> exchanger = new Exchanger<>();
        Thread t1 = new Thread(new Consumer(exchanger), "消费者-t1");
        Thread t2 = new Thread(new Producer(exchanger), "生产者-t2");

        t1.start();
        t2.start();
    }
}