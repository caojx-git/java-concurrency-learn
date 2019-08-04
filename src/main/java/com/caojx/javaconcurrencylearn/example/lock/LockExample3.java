package com.caojx.javaconcurrencylearn.example.lock;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 使用 ReentrantReadWriteLock 测试读写锁
 *
 * @author caojx
 * @version $Id: CountExample.java,v 1.0 2019-05-26 17:36 caojx
 * @date 2019-05-26 17:36
 */
@Slf4j
@ThreadSafe
public class LockExample3 {

    /**
     * 这一来，我们做了什么事情呢？ 比如说我们这里LockExample3封装了一个内部的map，而这个map我不想把所有的函数都暴露给别人，完全通过我提供的方法来使用，
     * 这时我就可以单独封装一些方法没给外部用，用的时候也不用担心他出现并发问题，这个时候我们就可以考虑加上， ReentrantReadWriteLock 读和写的时候分别加锁，
     * 这里面那我们使用ReentrantReadWriteLock他可以保证在没有任何读写锁到才可以进行写入操作，对数据同步做得更多一些，需要注意的是，他实现的是悲观读取，
     * 如果你要获得写入锁的时候呢？坚决不允许有任何的读锁还保持着，这样的话就保证了你如果写的时候，所有当前那个能做的事情做完了才可以允许你单独去做写操作。
     * 因此他就有一个问题，就是如果那读取情况很多，写入很少的时候，使用这个类，可能就会使我们线程遭遇饥饿，什么是饥饿呢？我们具体说一下，就是我们的写锁一直想执行，
     * 但是你总有读在操作导致我的写操作永远也没法去执行，一致处于等待，不知道什么时候能真正去操作写，我这里只是为了演示ReentrantReadWriteLock的读写锁然后举了这个例子，
     * 实际中能使用它的场景并不多。大家遇到了再使用，如果没遇到，就当多点理解了。
     */
    private final Map<String, Data> map =new TreeMap<>();

    private  final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public Data get(String key){
        readLock.lock();
        try {
            return map.get(key);
        }finally {
            readLock.unlock();
        }
    }

    public Set<String> getAllKeys(){
        readLock.lock();
        try {
            return map.keySet();
        }finally {
            readLock.unlock();
        }
    }

    public Data put(String key, Data value){
        writeLock.lock();
        try {
            return map.put(key, value);
        }finally {
            writeLock.unlock();
        }
    }

    class Data {

    }

}