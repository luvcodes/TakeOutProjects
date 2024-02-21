package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.Autofill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author ryanw
 */
@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Autofill(value = OperationType.INSERT)
    void insert(Dish dish);

    // 菜品分页查询
    // 动态sql
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    // 删除菜品所用的，根据主键查询菜品
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    // 删除菜品所用，根据主键删除菜品
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    // 根据菜品id集合批量删除菜品
    void deleteByIds(List<Long> ids);

    // 写动态SQL，执行更新操作的时候看Dish里面是不是null，如果不是null再执行修改操作
    // 根据id来动态修改菜品
    @Autofill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 动态条件查询菜品
     */
    List<Dish> list(Dish dish);


    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 根据条件统计菜品数量
     */
    Integer countByMap(Map map);
}
