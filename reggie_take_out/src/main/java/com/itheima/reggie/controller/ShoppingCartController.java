package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ryanw
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 添加购物车逻辑
     * */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart);

        // 设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);


        // 判断当前菜品或套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);


        if (dishId != null) {
            // 说明添加到购物车的是dish
            queryWrapper.eq(ShoppingCart::getDishId, dishId);

        } else {
            // 添加到购物车的是setmeal
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        // select * from shoppingCart where user_id = ? and dish_id/setmeal_id = ?;
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null) {
            // 获取carServiceOne本来的数量
            Integer number = cartServiceOne.getNumber();
            // 如果存在则数量+1
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            // 因为传页面请求的时候没有传递number这个字段，所以直接设置成1
            shoppingCart.setNumber(1);
            // 设置购物车的创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());

            // 如果不存在则添加到购物车，数量默认就是一
            shoppingCartService.save(shoppingCart);

            cartServiceOne = shoppingCart;
        }


        // 返回购物车对象，方便页面使用数据
        return R.success(cartServiceOne);
    }


    /**
     * 查看购物车，移动端查看
     * */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        // 排序条件, 根据时间升序。最后加的菜品，最上面展示
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     *  清空购物车，移动端查看
     * */
    @DeleteMapping("/clean")
    public R<String> clean() {
        // delete from shopping_cart where user_id = ?;
        // 条件构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }


    /**
     * http://localhost:8080/shoppingCart/sub
     * POST
     * {   dishId: null,
     *     setmealId: "1579044544635232258"
     * }
     * 下单的时候，只有加号按钮能用，实现减号按钮
     * */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long dishId = shoppingCart.getDishId();
        Long setMealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        // 只查询当前用户ID的购物车
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        // 代表数量减少的是菜品数量
        if (dishId != null) {
            // 通过dishId得到购物车内的dish数据
            wrapper.eq(ShoppingCart::getDishId, dishId);
            ShoppingCart cartDish = shoppingCartService.getOne(wrapper);
            // 数量减一
            cartDish.setNumber(cartDish.getNumber() - 1);

            // 判断当前cart中的dish的数量
            Integer currentDishNum = cartDish.getNumber();

            // 区分cart中dish的数量的各种情况
            if (currentDishNum > 0) {
                shoppingCartService.updateById(cartDish);
            } else {
                shoppingCartService.removeById(cartDish.getId());
            }

            return R.success(cartDish);
        }

        // 上面查询了dish的情况，现在要判断setmeal的情况
        if (setMealId != null) {
            // 通过setmealId得到购物车内的setmeal数据
            wrapper.eq(ShoppingCart::getSetmealId, setMealId);
            ShoppingCart cartSetMeal = shoppingCartService.getOne(wrapper);
            // 数量减一
            cartSetMeal.setNumber(cartSetMeal.getNumber() - 1);

            // 判断当前cart中的setmeal的数量
            Integer currentSetMealNum = cartSetMeal.getNumber();

            // 区分cart中setmeal的数量的各种情况
            if (currentSetMealNum > 0) {
                shoppingCartService.updateById(cartSetMeal);
            } else {
                shoppingCartService.removeById(cartSetMeal);
            }

            return R.success(cartSetMeal);
        }

        return R.error("减单失败，请重试");
    }

}
