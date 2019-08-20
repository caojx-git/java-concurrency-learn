package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

/**
 * 饿汉模式, 使用静态代码块实现单例模式，类加载时创建，线程安全
 *
 * @author caojx
 * @version $Id: SingletonExample2.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@ThreadSafe
public class SingletonExample6 {

    // 私有的构造函数
    private SingletonExample6() {

    }

    // 单例对象 第一处
    private static SingletonExample6 instance = null;

    // 第二处
    static {
        instance = new SingletonExample6();
    }

    // 静态工厂方法
    public static SingletonExample6 getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        //如果第一处与第二处改变代码编写顺序，会报空指针
        System.out.println(getInstance().hashCode());
        System.out.println(getInstance().hashCode());
    }
}