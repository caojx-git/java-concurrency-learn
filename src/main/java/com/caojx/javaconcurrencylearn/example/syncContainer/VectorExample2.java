package com.caojx.javaconcurrencylearn.example.syncContainer;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;

/**
 * 测试Vector同步容器，在某些情况下也会出现线程不安全问题，会出现如下问题
 *
 * 	at com.caojx.javaconcurrencylearn.example.syncContainer.VectorExample2$2.run(VectorExample2.java:41)
 * Exception in thread "Thread-15492" java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0
 * 	at java.util.Vector.remove(Vector.java:834)
 *
 * @author caojx
 * @version $Id: VectorExample2.java,v 1.0 2019-07-25 20:25 caojx
 * @date 2019-07-25 20:25
 */
@Slf4j
@NotThreadSafe
public class VectorExample2 {

    public static Vector<Integer> vector = new Vector<>();

    public static void main(String[] args) throws Exception {
        while (true){
            for (int i = 0; i < 10; i++) {
                vector.add(i);
            }

            Thread thread1 = new Thread(){
                @Override
                public void run(){
                    for (int i = 0; i < vector.size(); i++) {
                        vector.remove(i);
                    }
                }
            };

            Thread thread2 = new Thread(){
                @Override
                public void run(){
                    for (int i = 0; i < vector.size(); i++) {
                        vector.get(i);
                    }
                }
            };

            thread1.start();
            thread2.start();
        }
    }
}