package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

/**
 * 饿汉模式, 类加载时创建，线程安全
 *
 * 如果单例类的构造方法中没有包含过多的处理其实还是可以接受的，饿汉模式有什么不足呢，如果构造函数中存在过多的处理会导致这个类加载的变慢，
 * 可能会引起性能问题，如果使用饿汉模式，只要类加载就会被实例化，却没有实际的调用的话，会造成资源的浪费，因此，如果使用饿汉模式的话，
 * 要考虑两个问题，首先一个是私有构造函数在实现的时候没有太多处理，
 * 第二点，这个类在实际过程中肯定会被使用，不会带来资源的浪费。
 * 饿汉模式是线程安全的懒汉模式，那么懒汉模式是否也可以做成线程安全的呢？那是肯定的，我们在线程安全性里面提到过sychronized的使用方法，
 * 因此，这里引入sychronized修饰的描述就可以了。
 *
 * @author caojx
 * @version $Id: SingletonExample2.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@ThreadSafe
public class SingletonExample2 {

    // 私有的构造函数
    private SingletonExample2() {

    }

    // 单例对象
    private static SingletonExample2 instance = new SingletonExample2();

    // 静态工厂方法
    public static SingletonExample2 getInstance() {
        return instance;
    }
}