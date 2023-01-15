package com.example.wiki.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    /**
     * GET, POST, PUT, DELETE
     *
     * /user?id=1
     * /user/1
     * @return
     */
    // @PostMapping
    // @PutMapping
    // @DeleteMapping
    // @RequestMapping(value = "/user/1", method = RequestMethod.GET)
    // @RequestMapping(value = "/user/1", method = RequestMethod.DELETE)
    @RequestMapping("/hello")
    public String hello(){
        return "Hello World!";
    }
}
