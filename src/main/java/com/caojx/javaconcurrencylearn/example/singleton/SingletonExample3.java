package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.NotRecommend;
import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

/**
 * 懒汉模式，类第一次实例时创建 线程安全，使用synchronized达到让懒汉模式线程安全, 不推荐
 *
 * 这种写法我们并不推荐，原因是加了synchronized描述以后，他通过同一时间内只允许一个线程来访问的方式来保证线程安全，
 * 但是他却带来了性能上面的开销。而这个开销是我们不希望的。接下来，我们尝试修改我们的懒汉模式来提供更好的写法，同时保证线程安全。
 *
 * @author caojx
 * @version $Id: SingletonExample3.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@ThreadSafe
@NotRecommend
public class SingletonExample3 {

    // 私有的构造函数
    private SingletonExample3() {

    }

    // 单例对象
    private static SingletonExample3 instance = null;

    // 静态工厂方法
    public synchronized static SingletonExample3 getInstance() {
        if(instance == null) {
            instance = new SingletonExample3();
        }
        return instance;
    }
}