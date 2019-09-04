package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;

/**
 * 懒汉模式，双重同步锁单例模式，非线程安全
 *
 * @author caojx
 * @version $Id: SingletonExample4.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@NotThreadSafe
public class SingletonExample4 {

    // 私有的构造函数
    private SingletonExample4() {

    }

    /**
     *
     * instance = new SingletonExample4()，他要执行哪些操作？这里组要分3步指令
     * 1. memory = allocate() 分配对象内存空间
     * 2. ctorInstance() 初始化对象
     * 3. instance = memory 设置instance指向的分配内存
     *
     * 在单线程情况下，没有任何问题，但是在多线程情况下可能会发生指令重排序
     *
     * JVM和CPU优化，发生了指令重排序，因为第2步跟第3步没有前后逻辑关系
     *
     * 1. memory = allocate() 分配对象内存空间
     * 3. instance = memory 设置instance指向的分配内存
     * 2. ctorInstance() 初始化对象
     */

    // 单例对象
    private static SingletonExample4 instance = null;

    // 静态工厂方法
    public static SingletonExample4 getInstance() {
        if (instance == null) {//双重检测机制 // 线程 B
            synchronized (SingletonExample4.class) { //同步锁
                if (instance == null) {
                    instance = new SingletonExample4(); // 假如线程 A 先执行 3. instance = memory 设置instance指向的分配内存
                }
            }
        }
        return instance;
    }
}