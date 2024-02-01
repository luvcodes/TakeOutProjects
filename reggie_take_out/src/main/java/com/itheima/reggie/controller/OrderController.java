package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.OrdersDto;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
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
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);

        orderService.submit(orders);

        return R.success("下单成功");
    }


    /**
     * 历史订单功能分页
     * */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){
        // 获取当前id
        Long userId = BaseContext.getCurrentId();

        // 获取分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Orders::getUserId, userId);
        // 按时间降序排序
        wrapper.orderByDesc(Orders::getOrderTime);

        // 分页查询
        orderService.page(pageInfo, wrapper);

        List<OrdersDto> list = pageInfo.getRecords().stream().map(
                (item) -> {
                    OrdersDto ordersDto = new OrdersDto();
                    // 获取orderId然后根据这个id，去orderDetail表中查数据
                    Long orderId = item.getId();

                    LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(OrderDetail::getOrderId, orderId);

                    List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);

                    BeanUtils.copyProperties(item, ordersDto);

                    // 设置属性
                    ordersDto.setOrderDetails(orderDetails);
                    return ordersDto;
                }).collect(Collectors.toList());

        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        ordersDtoPage.setRecords(list);

        log.info("list: {}", list);

        return R.success(ordersDtoPage);
    }
}
