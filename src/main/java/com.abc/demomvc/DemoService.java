package com.abc.demomvc;

import com.abc.spring.annotation.Service;

@Service
public class DemoService {
    private String name;

    public void say(String msg) {
        System.out.println("============process service========");
        System.out.println(msg);
    }
}
