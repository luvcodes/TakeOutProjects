package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ryanw
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 根据时间区间统计营业额
     */
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        // 把开始日期begin添加进去
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        // 循环逐日增加日期，直到包含结束日期end
        while (!begin.equals(end)){
            // 日期计算，获得指定日期后1天的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        // 计算该日的营业额
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("begin",beginTime);
            map.put("end", endTime);
            // 查询数据库并计算在这个时间范围内所有已完成订单的总营业额
            Double turnover = orderMapper.sumByMap(map);
            // 如果查询返回的营业额为null（表示没有找到符合条件的订单），则将其视为0.0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 数据封装
        // 这个对象包含了两个字符串
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }
}
