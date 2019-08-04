package com.caojx.javaconcurrencylearn.example.publish;

import com.caojx.javaconcurrencylearn.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;

/**
 * 对象溢出，线程不安全
 *
 * @author caojx
 * @version $Id: Escape.java,v 1.0 2019-07-24 17:18 caojx
 * @date 2019-07-24 17:18
 */
@Slf4j
@NotThreadSafe
public class Escape {

    private int thisCanBeEscape = 0;

    public  Escape(){
        new InnerClass();
    }

    private class  InnerClass {
        public InnerClass() {
            log.info("{}", Escape.this.thisCanBeEscape);
        }
    }

    public static void main(String[] args) {
        new Escape();
    }
}