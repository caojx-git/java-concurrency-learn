package com.caojx.javaconcurrencylearn.example.threadPool;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Executors.newScheduledThreadPool 使用示例
 * ScheduledExecutorService可以延期，或定时、或周期性的执行任务，可以对比Timer使用
 */
@Slf4j
public class ThreadPoolExample4 {

    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

        //可以延迟执行，延迟3s执行
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                log.info("success schedule run");
            }
        }, 3, TimeUnit.SECONDS);

        //以指定的速率执行，每次间隔3s，可以对比Timer，也可以做延时
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("success scheduleAtFixedRate run");
            }
        }, 1, 3, TimeUnit.SECONDS);

        //executorService.shutdown();


        //每次执行完成之后，间隔5s再执行
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("success timer.schedule run");
            }
        }, new Date(), 5 * 1000);
    }
}
