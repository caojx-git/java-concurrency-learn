package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

/**
 * 懒汉模式，使用内置静态类实现线程安全
 *
 * @author caojx
 * @version $Id: SingletonExample8.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@ThreadSafe
public class SingletonExample8 {

    private static class MyObjectHandler {
        private static SingletonExample8 singletonExample8 = new SingletonExample8();
    }

    private SingletonExample8(){

    }

    public static SingletonExample8 getInstance(){
        return MyObjectHandler.singletonExample8;
    }

    public static void main(String[] args) {
        Thread threadA = new Thread(() -> {
            System.out.println(SingletonExample8.getInstance());
        });
        threadA.start();
        Thread threadB = new Thread(() -> {
            System.out.println(SingletonExample8.getInstance());
        });
        threadB.start();
        Thread threadC = new Thread(() -> {
            System.out.println(SingletonExample8.getInstance());
        });
        threadC.start();
    }
}