package com.caojx.javaconcurrencylearn.example.atomic;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * AtomicStampedReference 使用示例，后台使用多个线程对用户充值, 要求只能充值一次
 *
 * <p>
 * AtomicStampReference，这个类核心是要解决CAS的ABA问题。
 * <p>
 * 什么是ABA问题呢？
 * 线程一准备用CAS将变量的值由A替换为B，在此之前线程二将变量的值由A替换为C， 线程三又将C替换为A，
 * 然后线程一执行CAS时发现变量的值仍然为A， 所以线程一CAS成功。这时候其实该值已经被其它线程改变过，
 * 这与设计思想是不符合的。因此，ABA问题的解决思路，每次更新的时候，把变量的版本+1，那么之前那个A改成B再改成A，
 * 就会变成A变成1版本，B变成2版本，再改成A变成3版本。这个时候，只要变量被某一个线程修改过，该变量对应的版本号就会发生递增变化，
 * 从而解决了ABA问题。
 * <p>
 * <p>
 */
@ThreadSafe
public class AtomicStampedReferenceExample {

    private static AtomicStampedReference<Integer> money = new AtomicStampedReference<Integer>(19, 0);

    public static void main(String[] args) {
        //模拟多个线程同时更新后台数据库，为用户充值
        for (int i = 0; i < 3; i++) {
            final int timestamp = money.getStamp();
            new Thread() {
                public void run() {
                    while (true) {
                        Integer m = money.getReference();
                        if (m < 20) {
                            //比较设置 参数依次为：期望值 写入新值 期望Stamp 新时Stamp
                            if (money.compareAndSet(m, m + 20, timestamp, timestamp + 1)) {
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
                        int timestamp = money.getStamp();
                        Integer m = money.getReference();
                        if (m > 10) {
                            System.out.println("大于10元");
                            if (money.compareAndSet(m, m - 10, timestamp, timestamp + 1)) {
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

