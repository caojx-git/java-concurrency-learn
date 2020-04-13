package com.caojx.javaconcurrencylearn.source.test;

import java.util.concurrent.Phaser;

/**
 * 定义一个需要4个阶段完成的大任务，每个阶段需要3个小任务，针对这些小任务，我们分别起3个线程来执行这些小任务
 *
 * 参考文章：https://www.cnblogs.com/tong-yuan/p/11614755.html
 *
 * @author caojx created on 2020/4/12 10:56 下午
 */
public class PhaserTest5 {

    public static final int PARTIES = 3;
    public static final int PHASES = 4;

    public static void main(String[] args) {

        Phaser phaser = new Phaser(PARTIES) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                // 【本篇文章由公众号“彤哥读源码”原创，请支持原创，谢谢！】
                System.out.println("=======phase: " + phase + " finished=============");
                return super.onAdvance(phase, registeredParties);
            }
        };

        for (int i = 0; i < PARTIES; i++) {
            new Thread(() -> {
                for (int j = 0; j < PHASES; j++) {
                    System.out.println(String.format("%s: phase: %d", Thread.currentThread().getName(), j));
                    phaser.arriveAndAwaitAdvance();
                }
            }, "Thread " + i).start();
        }
    }
}