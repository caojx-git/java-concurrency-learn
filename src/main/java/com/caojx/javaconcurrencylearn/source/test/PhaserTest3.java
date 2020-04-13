package com.caojx.javaconcurrencylearn.source.test;

import java.io.IOException;
import java.util.concurrent.Phaser;

/**
 * 通过Phaser控制任务的执行轮数
 * <p>
 * 参考文章：https://segmentfault.com/a/1190000015979879
 *
 * @author caojx created on 2020/4/11 11:43 下午
 */
public class PhaserTest3 {
    public static void main(String[] args) throws IOException {

        int repeats = 3;    // 指定任务最多执行的次数

        /**
         * 我们在创建Phaser对象时，覆写了onAdvance方法，这个方法类似于CyclicBarrier中的barrierAction任务。
         * 也就是说，当最后一个参与者到达时，会触发onAdvance方法，入参phase表示到达时的phase(阶段)值，registeredParties表示到达时的参与者数量，返回true表示需要终止Phaser
         */
        Phaser phaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("---------------PHASE[" + phase + "],Parties[" + registeredParties + "] ---------------");

                // 通过phase + 1 >= repeats ，来控制阶段（phase）数的上限为2（从0开始计），最终控制了每个线程的执行任务次数为repeats次
                return phase + 1 >= repeats || registeredParties == 0;
            }
        };

        for (int i = 0; i < 10; i++) {
            phaser.register();                      // 注册各个参与者线程
            new Thread(new Task3(phaser), "Thread-" + i).start();
        }

    }
}

class Task3 implements Runnable {
    private final Phaser phaser;

    Task3(Phaser phaser) {
        this.phaser = phaser;
    }

    @Override
    public void run() {
        while (!phaser.isTerminated()) {   //只要Phaser没有终止, 各个线程的任务就会一直执行
            int i = phaser.arriveAndAwaitAdvance();     // 等待其它参与者线程到达
            // do something
            System.out.println(Thread.currentThread().getName() + ": 执行完任务");
        }
    }
}