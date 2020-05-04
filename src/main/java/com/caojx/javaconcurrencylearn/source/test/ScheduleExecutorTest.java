package com.caojx.javaconcurrencylearn.source.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 参考文章：https://segmentfault.com/a/1190000016586578#item-1-3
 * <p>
 * 示例先创建一个ScheduledExecutorService类型的执行器，然后利用scheduleAtFixedRate方法提交了一个“蜂鸣”任务，每隔10s该任务会执行一次
 *
 * @author caojx created on 2020/4/26 12:47 下午
 */
public class ScheduleExecutorTest {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();     // 创建一个ScheduledExecutorService实例

        final ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(new BeepTask(), 10, 10,
                TimeUnit.SECONDS);                              // 每隔10s蜂鸣一次

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                scheduledFuture.cancel(true);
            }
        }, 1, TimeUnit.HOURS);       // 1小时后, 取消蜂鸣任务
    }

    private static class BeepTask implements Runnable {
        @Override
        public void run() {
            System.out.println("beep!");
        }
    }
}