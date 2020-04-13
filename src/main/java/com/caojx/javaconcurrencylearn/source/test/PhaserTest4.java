package com.caojx.javaconcurrencylearn.source.test;

import java.io.IOException;
import java.util.concurrent.Phaser;

/**
 * Phaser支持分层功能
 *
 * 我们先来考虑下如何用利用Phaser的分层来实现高并发时的优化，在示例三中，我们创建了10个任务，
 * 如果任务数继续增大，那么同步产生的开销会非常大，利用Phaser分层的功能，我们可以限定每个Phaser对象的最大使用线程（任务数）
 *
 * 参考文章：https://segmentfault.com/a/1190000015979879#item-2-9
 *
 * @author caojx created on 2020/4/12 11:35 上午
 */
public class PhaserTest4 {
    private static final int TASKS_PER_PHASER = 4;      // 每个Phaser对象对应的工作线程（任务）数

    public static void main(String[] args) throws IOException {

        int repeats = 3;    // 指定任务最多执行的次数
        Phaser phaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("---------------PHASE[" + phase + "],Parties[" + registeredParties + "] ---------------");
                return phase + 1 >= repeats || registeredParties == 0;
            }
        };

        Task4[] taskers = new Task4[10];
        build(taskers, 0, taskers.length, phaser);       // 根据任务数,为每个任务分配Phaser对象

        for (int i = 0; i < taskers.length; i++) {          // 执行任务
            Thread thread = new Thread(taskers[i]);
            thread.start();
        }
    }

    private static void build(Task4[] taskers, int lo, int hi, Phaser phaser) {
        if (hi - lo > TASKS_PER_PHASER) {
            for (int i = lo; i < hi; i += TASKS_PER_PHASER) {
                int j = Math.min(i + TASKS_PER_PHASER, hi);
                build(taskers, i, j, new Phaser(phaser));
            }
        } else {
            for (int i = lo; i < hi; ++i)
                taskers[i] = new Task4(i, phaser);
        }

    }
}

class Task4 implements Runnable {
    private final Phaser phaser;
    private int count;

    Task4(Phaser phaser) {
        this.phaser = phaser;
        this.phaser.register();
    }

    Task4(int i,Phaser phaser) {
        this.count = i;
        this.phaser = phaser;
        this.phaser.register();
    }

    @Override
    public void run() {
        while (!phaser.isTerminated()) {   //只要Phaser没有终止, 各个线程的任务就会一直执行
            int i = phaser.arriveAndAwaitAdvance();     // 等待其它参与者线程到达
            // do something
            System.out.println(Thread.currentThread().getName() + ": 执行完任务,count="+count);
        }
    }
}