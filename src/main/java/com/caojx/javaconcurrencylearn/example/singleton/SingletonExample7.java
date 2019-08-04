package com.caojx.javaconcurrencylearn.example.singleton;

import com.caojx.javaconcurrencylearn.annoations.Recommend;
import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

/**
 * 枚举模式，最安全线程安全
 * 枚举中JVM 会保证枚举中的构造方法绝对只调用一次，因此，这里面就可以很好的保证了线程的安全，我们推荐使用这种方式，因为它相比我们的懒汉模式在安全性方面，更容易保证，
 * 其次他相比我们的恶汉模式，在实际的调用的时候才做开始的初始化，而在后续生时候的时候也可以直接到对应的值不会造成资源的浪费。
 *
 * @author caojx
 * @version $Id: SingletonExample2.java,v 1.0 2019-07-24 18:12 caojx
 * @date 2019-07-24 18:12
 */
@ThreadSafe
@Recommend
public class SingletonExample7 {

    // 私有的构造函数
    private SingletonExample7() {

    }

    public static SingletonExample7 getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    private enum Singleton {
        INSTANCE;

        private SingletonExample7 singletonExample7;

        // JVM 保证这个方法绝对只调用一次
        Singleton() {
            singletonExample7 = new SingletonExample7();
        }

        public SingletonExample7 getInstance() {
            return singletonExample7;
        }
    }

    public static void main(String[] args) {
        System.out.println(SingletonExample7.getInstance().hashCode());
        System.out.println(SingletonExample7.getInstance().hashCode());
    }
}