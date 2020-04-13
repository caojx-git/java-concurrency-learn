package com.caojx.javaconcurrencylearn.source.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Phaser;

/**
 * 通过Phaser实现开关。
 *
 * 在以前讲CountDownLatch时，我们给出过以CountDownLatch实现开关的示例，也就是说，我们希望一些外部条件得到满足后，然后打开开关，线程才能继续执行，我们看下如何用Phaser来实现此功能
 * <p>
 * 参考文章：https://segmentfault.com/a/1190000015979879
 *
 * @author caojx created on 2020/4/11 11:43 下午
 */
public class PhaserTest2 {

    /**
     * 只有当用户按下回车之后，任务才真正开始执行。这里主线程Main相当于一个协调者，用来控制开关打开的时机，
     * arriveAndDeregister方法不会阻塞，该方法会将到达数加1，同时减少一个参与者数量，最终返回线程到达时的phase值。
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Phaser phaser = new Phaser(1);       // 注册主线程,当外部条件满足时,由主线程打开开关
        for (int i = 0; i < 10; i++) {
            phaser.register();                      // 注册各个参与者线程
            new Thread(new Task2(phaser), "Thread-" + i).start();
        }

        // 外部条件:等待用户输入命令
        System.out.println("Press ENTER to continue");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        reader.readLine();

        // 打开开关
        phaser.arriveAndDeregister();
        System.out.println("主线程打开了开关");
    }
}

class Task2 implements Runnable {
    private final Phaser phaser;

    Task2(Phaser phaser) {
        this.phaser = phaser;
    }

    @Override
    public void run() {
        int i = phaser.arriveAndAwaitAdvance();     // 等待其它参与者线程到达

        // do something
        System.out.println(Thread.currentThread().getName() + ": 执行完任务，当前phase =" + i + "");
    }
}