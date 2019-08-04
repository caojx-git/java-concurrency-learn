package com.caojx.javaconcurrencylearn.example.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * FutureTask的使用示例，Future会等Callable结果拿到之后才会继续往下执行
 */
@Slf4j
public class FutureTaskExample {

    public static void main(String[] args) throws Exception {
        FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("do something in callable");
                Thread.sleep(5000);
                return "Done";
            }
        });
        new Thread(futureTask).start();
        log.info("do something in main");
        String result = futureTask.get(); ////获取线程执行的结果
        log.info("result:{}", result);
    }
}
