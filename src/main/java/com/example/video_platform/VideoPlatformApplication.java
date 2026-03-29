package com.example.video_platform;

import com.example.video_platform.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VideoPlatformApplication implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    public static void main(String[] args) {
        SpringApplication.run(VideoPlatformApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 缓存预热
        categoryService.warmUpCache();
        System.out.println("=== 分类缓存预热完成 ===");
    }
}