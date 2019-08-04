package com.caojx.javaconcurrencylearn.example.publish;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 发布对象，不安全的示例
 *
 * @author caojx
 * @version $Id: UnsafePublish.java,v 1.0 2019-07-24 17:03 caojx
 * @date 2019-07-24 17:03
 */
@Slf4j
@NotThreadSafe
public class UnsafePublish {

    private String[] states = {"a", "b", "c"};

    public String[] getStates(){
        return states;
    }

    public static void main(String[] args) {
        UnsafePublish unsafePublish = new UnsafePublish();
        log.info("{}", Arrays.toString(unsafePublish.getStates()));

        //暴露的共有方法对私有变量进行修改
        unsafePublish.getStates()[0] = "d";
        log.info("{}", Arrays.toString(unsafePublish.getStates()));
    }
}