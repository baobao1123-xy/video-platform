package com.example.video_platform.controller;

import com.example.video_platform.entity.Category;
import com.example.video_platform.entity.CategorySimple;
import com.example.video_platform.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public List<CategorySimple> getTree() {
        return categoryService.getCategoryTree();
    }

    @PostMapping("/add")
    public String addCategory(@RequestBody Category category) {
        categoryService.addCategory(category);
        return "添加成功";
    }

    @PutMapping("/update")
    public String updateCategory(@RequestBody Category category) {
        categoryService.updateCategory(category);
        return "更新成功";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "删除成功";
    }

    @DeleteMapping("/clear-cache")
    public String clearCache() {
        categoryService.clearCache();
        return "缓存已清除";
    }
}