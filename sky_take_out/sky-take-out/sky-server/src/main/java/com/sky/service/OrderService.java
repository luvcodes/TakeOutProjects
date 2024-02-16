package com.sky.service;

import com.sky.dto.*;
import com.sky.vo.OrderSubmitVO;

/**
 * @author ryanw
 */
public interface OrderService {

    /**
     * 用户下单
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
}