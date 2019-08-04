package com.caojx.javaconcurrencylearn.example.sync;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * synchronized 示例 synchronized修饰方法和代码块
 * 修饰代码块：大括号括起来的代码，作用于调用的对象
 * 修饰方法：整个方法，作用于调用的对象
 * @author caojx
 * @version $Id: SynchronizedExample1.java,v 1.0 2019-07-24 11:07 caojx
 * @date 2019-07-24 11:07
 */
@Slf4j
public class SynchronizedExample1 {

    /**
     * 修饰代码块，大括号括起来的代码，作用于调用的对象
     */
    public void test1() {
        synchronized (this) { //被修饰的代码块称为同步代码块
            for (int i = 0; i < 10; i++) {
                log.info("test1 - {}", i);
            }
        }
    }

    /**
     * 修饰一个方法，被修饰的方法称为同步方法，整个方法，作用于调用的对象
     */
    public synchronized void test2(){
        for (int i = 0; i < 10; i++) {
            log.info("test2 - {}", i);
        }
    }

    /**
     * 同一个对象调用synchronized修饰的方法，先从0~10执行一次，再从0~10执行行一次
     * @param args
     */
    public static void main(String[] args) {
        SynchronizedExample1 synchronizedExample1 = new SynchronizedExample1();
        ExecutorService executorService = Executors.newCachedThreadPool();
        /**
         * 这里调用两次。大家可能更容易理解，因为我们要验证的是，对于修饰一个代码块，他的作用范围是当前的大括号，作用对象是调用同步代码块的对象，因此，我们让这个对象方法调用两次，他们肯定是先从0~10执行一次，再从0~10执行行一次。
         * 为什么还要使用线程池，如果我们这里不使用线程池，这个类对象，两次都调用同样的方法，肯定本身就是同步执行的，因此如果不适用线程池，我们是没法验证他们具体的影响的，而我们使用线程池之后呢，他相当于是分别启动了两个进程去执行。
         * 然后就相当于不等第一个线程池执行完后立马就执行了第二个线程池。这样我们才能看到同一个对象的两个进程同时来调用这段代码的时候他的执行情况。因此，这里面我们就是通过了线程池以及两次调用的方式来模拟了同一个调用对象，同时来就用这个方法，准确的说，是同步代码块的执行情况。
         * 调用test2（）也是一样的
         */
        executorService.execute(()->{
            synchronizedExample1.test1();
        });
        executorService.execute(()->{
            synchronizedExample1.test1();
        });
    }

}