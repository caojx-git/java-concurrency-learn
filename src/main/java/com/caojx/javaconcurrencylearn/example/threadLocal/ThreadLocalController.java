package com.caojx.javaconcurrencylearn.example.threadLocal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类注释，描述
 *
 * @author caojx
 * @version $Id: ThreadLocalController.java,v 1.0 2019-07-25 15:22 caojx
 * @date 2019-07-25 15:22
 */
@RestController
@RequestMapping("/threadLocal")
public class ThreadLocalController {

    @RequestMapping("test")
    public Long test(){
        return RequestHolder.getId();
    }
}