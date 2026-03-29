package com.example.video_platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "项目运行成功！当前时间：" + System.currentTimeMillis();
    }
}