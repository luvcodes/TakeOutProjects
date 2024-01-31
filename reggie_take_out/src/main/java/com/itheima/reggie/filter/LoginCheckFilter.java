package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ryanw
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配器, 支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // ServletResponse是Java Servlet API中的一个基础接口，而HttpServletResponse是一个更具体的实现，提供了HTTP协议特有的功能
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. 获取本次请求URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);

        // 确定不需要处理的请求路径
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                // 对用户登陆操作放行
                "/user/login",
                "/user/sendMsg"
        };

        // 2. 判断本次请求是否需要处理，也就判断是否包含在上面这个String数组中 (是否需要放行)
        boolean check = check(urls, requestURI);

        // 3. 如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            // 已经登录，取到id
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));

            // 当前登录用户的id
            Long empId = (Long) request.getSession().getAttribute("employee");
            // 设置到线程中，供后续操作使用
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }


        log.info("用户未登录");
        // 5. 如果未登录，则返回未登录结果，通过输出流方式向客户端页面响应数据
        // 使用NOTLOGIN是因为前端的backend/js/request.js中定义了相应拦截器
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
