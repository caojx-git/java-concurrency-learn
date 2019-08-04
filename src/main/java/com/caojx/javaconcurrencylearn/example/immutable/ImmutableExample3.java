package com.caojx.javaconcurrencylearn.example.immutable;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;

/**
 * Immutable的其他的类
 *
 * Google Guava提供的Immutable相关类
 *
 * @author caojx
 * @version $Id: ImmutableExample3.java,v 1.0 2019-07-24 23:08 caojx
 * @date 2019-07-24 23:08
 */
@Slf4j
@ThreadSafe
public class ImmutableExample3 {

    private static final ImmutableList list = ImmutableList.of(1, 2, 3);

    private static final ImmutableSet set = ImmutableSet.copyOf(list);

    //key value key value
    private static final ImmutableMap<Integer, Integer> map = ImmutableMap.of(1,2);

    private static final ImmutableMap<Integer, Integer> map2 = ImmutableMap.<Integer, Integer>builder().put(1, 2).put(3, 4).build();

    public static void main(String[] args) {
        // ImmutableXXX相关的类是不可以被修改的，会抛出异常
        list.add(4);

        //抛出异常
        map.put(1, 4);

        System.out.println(map.get(1));
    }
}