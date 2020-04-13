package com.caojx.javaconcurrencylearn.source.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * ReentrantReadWriteLock源码与锁升降级详解
 *
 * 参考文章：https://blog.csdn.net/itcats_cn/article/details/81266508
 *
 * 相同线程，写锁可以降级为读锁，即加了写锁后，可以加读锁
 * 读锁不能升级为写锁
 *
 * @author caojx created on 2020/4/5 8:31 下午
 */
public class ReentrantReadWriteLockTest2 {
    private Map<String,Object> map = new HashMap<>();

    //读写锁
    private ReadWriteLock lock  =new ReentrantReadWriteLock();
    //读锁
    private Lock readLock = lock.readLock();
    //写锁
    private Lock writeLock =lock.writeLock();

    private volatile boolean flag = true;

    public void readwrite() {
        //为了保证flag能拿到最新的值
        readLock.lock();
        if(flag) {
            //对值进行写操作,因为读写锁互斥,若不释放读锁,则写锁无法获取
            readLock.unlock();
            //获取写锁     读锁释放完毕后,所有写锁竞争线程
            writeLock.lock();
            //写锁是排它锁,最终有一个线程获得写锁,并执行put写操作
            map.put("hello", "hi");
            //在写完后,若不加读锁,则直接释放读锁,其他线程可能同样进行put()写操作
            //在此加了读锁后,读写锁是互斥的,其他线程必须等待readLock读锁释放后才能写(put )成功
            readLock.lock();   //获取读锁进行锁降级
            //释放写锁
            writeLock.unlock();
        }
        Object value = map.get("hello");
        System.out.println(value);
        readLock.unlock();
    }

    public static void main(String[] args) {
        ReentrantReadWriteLockTest2 test = new ReentrantReadWriteLockTest2();
        test.readwrite();
    }

}