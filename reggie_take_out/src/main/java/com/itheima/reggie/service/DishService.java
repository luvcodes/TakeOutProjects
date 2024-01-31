package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.entity.Dish;

/**
 * @author ryanw
 */
public interface DishService extends IService<Dish> {

    // 新增菜品，同时插入菜品对应的口味数据
    // 需要操作两张表，dish和dishflavor
    public void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品信息和对应的口味信息，针对修改菜品功能的查询菜品信息而做的
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
