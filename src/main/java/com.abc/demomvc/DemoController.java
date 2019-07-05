package com.abc.demomvc;

import com.abc.spring.annotation.Autowired;
import com.abc.spring.annotation.Controller;
import com.abc.spring.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    private DemoService service;

    @RequestMapping("/say")
    public void say(String name){
        service.say("Hi ");
    }
}

