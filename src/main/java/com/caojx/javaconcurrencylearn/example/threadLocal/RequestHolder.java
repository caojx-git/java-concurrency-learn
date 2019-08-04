package com.caojx.javaconcurrencylearn.example.threadLocal;

/**
 * 类注释，描述
 *
 * @author caojx
 * @version $Id: RequestHolder.java,v 1.0 2019-07-25 14:59 caojx
 * @date 2019-07-25 14:59
 */
public class RequestHolder {

    public static final ThreadLocal<Long> requestHolder  = new ThreadLocal<>();

    public static void add(Long id){
        requestHolder.set(id);
    }

    public static Long getId(){
        return requestHolder.get();
    }

    /**
     * 如果不做移除的话，那么会造成内存泄露，数据永远不会释放掉，因为这个类会一直伴随着我们的项目，只有当项目存启动的时候，它里面存着信息才可以被释放
     */
    public static void remove(){
        requestHolder.remove();
    }
}