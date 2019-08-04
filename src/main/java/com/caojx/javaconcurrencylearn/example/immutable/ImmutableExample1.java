package com.caojx.javaconcurrencylearn.example.immutable;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * final修饰变量
 *
 * @author caojx
 * @version $Id: ImmutableExample1.java,v 1.0 2019-07-24 23:08 caojx
 * @date 2019-07-24 23:08
 */
@Slf4j
@NotThreadSafe
public class ImmutableExample1 {

    private static final int a = 1;
    private static final String b = "2";
    private static final Map<Integer, Integer> map = Maps.newHashMap();

    static {
        map.put(1, 2);
        map.put(3, 4);
        map.put(5, 6);
    }

    public static void main(String[] args) {
        //final修饰基础数据类型，不可以修改值，编译就出错
//        a = 2;
//        b = "3";

        //final修饰引用类型，不可以指向新的引用对象
//        map = Maps.newHashMap();

        //final修饰引用类型，可以修改应用对象里边的值，这就可能引起线程安全问题
        map.put(1, 3);
        log.info("{}", map.get(1));
    }

    //传入方法的变量，不允许在传输的过程中发生变化，就可以将入参声明为final
    private void test(final int a) {
        //也不能修改
//        a = 1;
    }
}