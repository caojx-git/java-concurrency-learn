package com.caojx.javaconcurrencylearn.example.atomic;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

import java.util.concurrent.atomic.AtomicMarkableReference;


/**
 * 我们在讲ABA问题的时候，引入了AtomicStampedReference。
 *
 * AtomicStampedReference可以给引用加上版本号，追踪引用的整个变化过程，如：
 * A -> B -> C -> D - > A，通过AtomicStampedReference，我们可以知道，引用变量中途被更改了3次。
 *
 * 但是，有时候，我们并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了AtomicMarkableReference
 *
 * AtomicMarkableReference与AtomicStampedReference的唯一区别就是不再用int标识引用，而是使用boolean变量——表示引用变量是否被更改过，可以看AtomicMarkableReference的构造函数
 *
 * @author caojx created on 2020/4/7 12:43 下午
 */
@ThreadSafe
public class AtomicMarkableReferenceExample {

    private static AtomicMarkableReference<Integer> money = new AtomicMarkableReference<Integer>(19, false);

    public static void main(String[] args) {
        //模拟多个线程同时更新后台数据库，为用户充值
        for (int i = 0; i < 3; i++) {
            boolean marked = money.isMarked();
            new Thread() {
                public void run() {
                    while (true) {
                        Integer m = money.getReference();
                        if (m < 20) {
                            //比较设置 参数依次为：期望值 写入新值 期望的Stamp 新的Stamp
                            if (money.compareAndSet(m, m + 20, marked, Boolean.TRUE)) {
                                System.out.println("余额小于20元，充值成功，余额:" + money.getReference() + "元");
                                break;
                            }
                        } else {
                            //System.out.println("余额大于20元，无需充值");
                            break;
                        }
                    }
                }
            }.start();
        }

        //用户消费线程，模拟消费行为
        new Thread() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    while (true) {
                        Integer m = money.getReference();
                        if (m > 10) {
                            System.out.println("大于10元");
                            if (money.compareAndSet(m, m - 10, money.isMarked(), Boolean.TRUE)) {
                                System.out.println("成功消费10元，余额:" + money.getReference());
                                break;
                            }
                        } else {
                            System.out.println("没有足够的金额");
                            break;
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();
    }

}