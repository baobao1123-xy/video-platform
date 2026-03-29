package com.example.video_platform.service;

import com.example.video_platform.entity.Category;
import com.example.video_platform.entity.CategorySimple;
import com.example.video_platform.mapper.CategoryMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "category:tree";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取分类树（优先从 Redis 缓存读取）
     */
    public List<CategorySimple> getCategoryTree() {
        // 1. 从 Redis 读取
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CategorySimple.class));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 2. 缓存未命中，从数据库查询
        List<Category> allCategories = categoryMapper.findAll();
        List<Category> tree = buildTree(allCategories);

        // 转换为简化版
        List<CategorySimple> simpleTree = new ArrayList<>();
        convertToSimple(tree, simpleTree);

        // 3. 写入 Redis
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(simpleTree), 30, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return simpleTree;
    }

    /**
     * 构建树形结构
     */
    private List<Category> buildTree(List<Category> allCategories) {
        List<Category> tree = new ArrayList<>();

        for (Category category : allCategories) {
            if (category.getParentId() == 0) {
                category.setChildren(getChildren(category.getId(), allCategories));
                tree.add(category);
            }
        }

        return tree;
    }

    /**
     * 递归获取子分类
     */
    private List<Category> getChildren(Long parentId, List<Category> allCategories) {
        List<Category> children = new ArrayList<>();

        for (Category category : allCategories) {
            if (category.getParentId().equals(parentId)) {
                category.setChildren(getChildren(category.getId(), allCategories));
                children.add(category);
            }
        }

        return children;
    }

    /**
     * 添加分类（同时清除缓存）
     */
    public void addCategory(Category category) {
        categoryMapper.insert(category);
        clearCache();
    }

    /**
     * 更新分类（同时清除缓存）
     */
    public void updateCategory(Category category) {
        categoryMapper.update(category);
        clearCache();
    }

    /**
     * 删除分类（同时清除缓存）
     */
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
        clearCache();
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        redisTemplate.delete(CACHE_KEY);
    }

    /**
     * 缓存预热（项目启动时调用）
     */
    public void warmUpCache() {
        List<Category> allCategories = categoryMapper.findAll();
        List<Category> tree = buildTree(allCategories);

        List<CategorySimple> simpleTree = new ArrayList<>();
        convertToSimple(tree, simpleTree);

        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(simpleTree), 30, TimeUnit.MINUTES);
            System.out.println("分类缓存预热完成，共 " + tree.size() + " 个一级分类");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转换为简化版分类
     */
    private void convertToSimple(List<Category> source, List<CategorySimple> target) {
        for (Category cat : source) {
            CategorySimple simple = new CategorySimple();
            simple.setId(cat.getId());
            simple.setParentId(cat.getParentId());
            simple.setName(cat.getName());
            simple.setLevel(cat.getLevel());
            simple.setSortOrder(cat.getSortOrder());

            if (cat.getChildren() != null && !cat.getChildren().isEmpty()) {
                List<CategorySimple> simpleChildren = new ArrayList<>();
                convertToSimple(cat.getChildren(), simpleChildren);
                simple.setChildren(simpleChildren);
            }

            target.add(simple);
        }
    }
}