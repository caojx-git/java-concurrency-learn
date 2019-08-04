package com.caojx.javaconcurrencylearn.example.syncContainer;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author caojx
 * @version $Id: VectorExample3.java,v 1.0 2019-07-25 20:25 caojx
 * @date 2019-07-25 20:25
 */
@Slf4j
@NotThreadSafe
public class VectorExample3 {

    //java.util.ConcurrentModificationException
    private static void test1(Vector<Integer> v1) { // foreach
        for (Integer i : v1) {
            if (i.equals(3)) {
                v1.remove(i);
            }
        }
    }

    //java.util.ConcurrentModificationException
    private static void test2(Vector<Integer> v1) { //iterator
        Iterator<Integer> iterator = v1.iterator();
        while (iterator.hasNext()) {
            Integer i = iterator.next();
            if (i.equals(3)) {
                v1.remove(i);
            }
        }
    }

    //success
    private static void test3(Vector<Integer> v1) { // for
        for (int i = 0; i < v1.size(); i++) {
            if (v1.get(i).equals(3)) {
                v1.remove(i);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        Vector<Integer> vector = new Vector<>();
        vector.add(1);
        vector.add(2);
        vector.add(3);

//        VectorExample3.test1(vector); //失败 java.util.ConcurrentModificationException
//        VectorExample3.test2(vector);  // 失败 java.util.ConcurrentModificationException
        VectorExample3.test3(vector);  //成功
    }
}