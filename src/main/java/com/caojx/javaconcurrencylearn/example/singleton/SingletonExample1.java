package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;

/**
 * 懒汉模式,类第一次实例时创建 线程不安全
 *
 * 这样写的并不好，他在单线程运行的时候，肯定没问题，原因是他先判断了这个实例是否实例化过？如果没有实例话过的话就实例化一下，如果已经实例化过就直接返回。
 * 在多线程环境下，代这段码就可能会出现问题，原因出在这里面的 if(instance == null) 这段代码，我们之前在计数时，演示过比如两个线程同时开始访问这个方法，
 * 比如执行到if(instance == null)行的时候，都拿到这个实例是空值（null），都会去做一次实例化，这样的话，这个方法可能会被调用两次，这时候那两个线程他们拿到的实例其实是不一样的。
 * 有的同学那可能会问我就方法里面就为了拿一个实例，即使执行两次也不会有什么影响，但是我们避免不了的问题是，如果我这方法在实现的时候私有函数真正在做操作的时要做很多操作，
 * 包括对某些资源的处理，包括预算等等，如果运算两次，就可能会出现错误，我们这里只是通过简单的示例来说明下他是否运行两次。而如果真运行两次的话，
 * 我们说的是线程不安全，线程不安全的结果不代表一定会产生什么不好的印象。有的时候甚至不会出现，这里，大家知道这样写是线程不安全的就可以了。
 *
 * @author caojx
 * @version $Id: SingletonExample1.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@NotThreadSafe
public class SingletonExample1 {

    // 私有的构造函数
    private SingletonExample1() {

    }

    // 单例对象
    private static SingletonExample1 instance = null;

    // 静态工厂方法
    public static SingletonExample1 getInstance() {
        if(instance == null) {
            instance = new SingletonExample1();
        }
        return instance;
    }
}