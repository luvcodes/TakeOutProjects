package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.SetmealDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
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
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("setmealDto: {}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 分页查询功能实现
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        // 添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 调用service来进行查询 (分页查询)
        setmealService.page(pageInfo, queryWrapper);

        // 完成上面page这行代码之后，pageInfo里面的相关属性都已经配上值了
        // 实现拷贝属性, 排除records
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");


        // 自定义处理records, 将records转换为dto对象, 并将其放入dtoPage中。
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            // 拷贝普通属性
            BeanUtils.copyProperties(item, setmealDto);

            // 每一个item就是一个setmeal，获得category_id就可以查询数据库中的category_name
            Long categoryId = item.getCategoryId();
            // 根据分类id查询分类对象，所以需要一个categoryService对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                // 获取category_name
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            // 到这里SetmealDto中的各个属性都成功赋值了
            return setmealDto;
        }).collect(Collectors.toList());


        // 上面这些都是在处理自定义records，这样再把这个自定义records，也就是list给到dtoPage
        dtoPage.setRecords(list);


        return R.success(dtoPage);
    }


    /**
     * 删除套餐
     * */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids: {}", ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }


    /**
     * 根据条件查询套餐数据。移动端展示
     * */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        // 构造查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        // 返回list数据给前端，前端会根据list的长度来判断是否有数据，如果有数据就显示。
        return R.success(list);
    }
}
