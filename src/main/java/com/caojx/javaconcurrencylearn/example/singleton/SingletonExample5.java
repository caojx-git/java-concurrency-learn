package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

/**
 * 懒汉模式，双重同步锁单例模式，使用 volatile 限制指令重排 线程安全
 *
 * @author caojx
 * @version $Id: SingletonExample5.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@ThreadSafe
public class SingletonExample5 {

    // 私有的构造函数
    private SingletonExample5() {

    }

    /**
     * instance = new SingletonExample4()，他要执行哪些操作？这里组要分3步指令
     * 1. memory = allocate() 分配对象内存空间
     * 2. ctorInstance() 初始化对象
     * 3. instance = memory 设置instance指向的分配内存
     *
     * 使用 volatile 限制指令重排
     */

    // 单例对象 volatile + 双重检测机制 ->禁止指令重排序
    private volatile static SingletonExample5 instance = null;

    // 静态工厂方法
    public synchronized static SingletonExample5 getInstance() {
        if (instance == null) {//双重检测机制
            synchronized (SingletonExample5.class) { //同步锁
                if (instance == null) {
                    instance = new SingletonExample5();
                }
            }
        }
        return instance;
    }
}