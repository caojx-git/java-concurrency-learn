package com.caojx.javaconcurrencylearn;

import com.caojx.javaconcurrencylearn.example.threadLocal.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 调用remove()，我们使用Interceptor
 *
 * @author caojx
 * @version $Id: HttpInterceptor.java,v 1.0 2019-07-25 15:15 caojx
 * @date 2019-07-25 15:15
 */
@Slf4j
public class HttpInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        RequestHolder.remove();
        log.info("afterCompletion");
        return;
    }

}