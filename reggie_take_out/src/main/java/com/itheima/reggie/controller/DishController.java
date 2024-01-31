package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ryanw
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;


    @Autowired
    private CategoryService categoryService;


    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        // 添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        // 添加排序条件, 根据更新时间降序排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        // 执行分页查询
        dishService.page(pageInfo, queryWrapper);


        // 对象拷贝
        // 忽略records属性, 不拷贝records属性, 只拷贝其他属性。因为我们想在下面自己处理records属性。
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        // 处理records属性
        List<Dish> records = pageInfo.getRecords();

        // 将records属性转换为dishDto属性, 并将其放入dishDtoPage中。
        // 因为只有DishDto中才能得到category_name这个字段的值，才可以成功显示在页面上。
        List<DishDto> list = records.stream().map((item) -> {

            DishDto dishDto = new DishDto();

            // 将dish对象拷贝到dishDto对象中, 拷贝普通属性
            BeanUtils.copyProperties(item, dishDto);

            // item是每一个dish 菜品对象
            Long categoryId = item.getCategoryId(); // 分类id
            // 根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                // 获取分类名称, 并赋值给dishDto对象的categoryName属性。
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }


            return dishDto;
        }).collect(Collectors.toList());

        // 将list放入dishDtoPage中, 并返回。
        dishDtoPage.setRecords(list);


        return R.success(dishDtoPage);
    }


    /**
     * 根据ID查询菜品信息和对应的口味信息
     * 用DishDto是因为页面上有口味配置这样的需求，Dish本身是没有这个属性的
     * 请求URL中是localhost:8080/dish/1412600741490610177 所以需要@PathVariable
     * */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        // 需要查两张表，扩展dishService
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 这里需要更新dish和flavor两张表
     * */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("更新菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * 这个是为了新增套餐界面使用
     * */
/*    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {

        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 添加条件，根据分类id进行查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        // 添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        // 添加排序条件，Dish里面有一个字段叫做sort
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 添加条件，根据分类id进行查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        // 添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        // 添加排序条件，Dish里面有一个字段叫做sort
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);


        List<DishDto> dishDtoList = list.stream().map((item) -> {

            DishDto dishDto = new DishDto();

            // 将dish对象拷贝到dishDto对象中, 拷贝普通属性
            BeanUtils.copyProperties(item, dishDto);

            // item是每一个dish 菜品对象
            Long categoryId = item.getCategoryId(); // 分类id
            // 根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                // 获取分类名称, 并赋值给dishDto对象的categoryName属性。
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            // 这里获取dishId, 用于查询口味表, 用于显示口味信息。
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);

            // SQL: select * from dish_flavor where dish_id = ?;
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());




        return R.success(dishDtoList);
    }
}
