package com.example.video_platform.mapper;

import com.example.video_platform.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category ORDER BY sort_order ASC")
    List<Category> findAll();

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} ORDER BY sort_order ASC")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(@Param("id") Long id);

    @Insert("INSERT INTO category (parent_id, name, level, sort_order) VALUES (#{parentId}, #{name}, #{level}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Category category);

    @Update("UPDATE category SET name = #{name}, sort_order = #{sortOrder} WHERE id = #{id}")
    void update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}