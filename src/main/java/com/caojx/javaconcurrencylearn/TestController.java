package com.caojx.javaconcurrencylearn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试类
 *
 * @author caojx
 * @version $Id: TestController.java,v 1.0 2019-07-23 15:03 caojx
 * @date 2019-07-23 15:03
 */
@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}