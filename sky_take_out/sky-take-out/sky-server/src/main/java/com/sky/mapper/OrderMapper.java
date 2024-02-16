package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ryanw
 */
@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     */
    void insert(Orders order);
}
