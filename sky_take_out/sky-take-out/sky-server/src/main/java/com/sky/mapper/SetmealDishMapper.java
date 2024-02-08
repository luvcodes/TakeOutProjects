package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * @author ryanw
 */
@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询对应的套餐id。多对多关系，有可能查出来多个套餐。
     * 这个方法就是为了批量删除菜品做的。
     * @param dishIds
     * @return
     */
    // select setmeal_id from setmeal_dish where dish_id in (1,2,3,4) 这后面的List集合是动态SQL
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}