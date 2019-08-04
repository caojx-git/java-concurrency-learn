package com.caojx.javaconcurrencylearn.example.immutable;

import com.caojx.javaconcurrencylearn.annoations.ThreadSafe;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

/**
 * 使用Collections.unmodifiableXXXX() 定义不可变对象
 * 使用Collections.unmodifiableMap处理map，之后map里边的值不可以被修改
 *
 * @author caojx
 * @version $Id: ImmutableExample2.java,v 1.0 2019-07-24 23:08 caojx
 * @date 2019-07-24 23:08
 */
@Slf4j
@ThreadSafe
public class ImmutableExample2 {

    //注意没有使用final修饰
    private static Map<Integer, Integer> map = Maps.newHashMap();

    static {
        map.put(1, 2);
        map.put(3, 4);
        map.put(5, 6);

        // Collections.unmodifiableMap 处理过map之后，map是不可以被修改的
        map = Collections.unmodifiableMap(map);
    }

    public static void main(String[] args) {

        // 修改map的值，会报错
        map.put(1, 3);

        log.info("{}", map.get(1));
    }
}