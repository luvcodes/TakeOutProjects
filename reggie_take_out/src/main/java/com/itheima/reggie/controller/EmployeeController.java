package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author ryanw
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @Param @RequestBody Employee employee 这里使用@RequestBody的原因是因为要传递的是JSON形式
     * @Param HttpServletRequest 使用HttpServletRequest是因为要把employee的id存入到session一份，
     * 这样未来想要取employee id的时候就可以随时从session中取出
     * */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        // 1. 页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<Employee>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3. 如果没有查询到username则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        // 4. 密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登陆失败");
        }

        // 5. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        // 6. 登录成功，将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());

        // 登陆成功
        return R.success(emp);
    }

    /**
     * 员工退出
     * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee 新增员工的信息，包括员工名、密码、手机号、员工状态等信息
     * */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {

        log.info("新增员工，员工信息：{}",employee.toString());

        // 可以先设置初始密码，然后让用户登录之后再进行修改
        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        //Long empId = (Long) request.getSession().getAttribute("employee");

        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page 页码
     * @param pageSize 页面大小
     * @param name  员工名称，用于模糊查询 (搜索)
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        // 添加过滤条件
        queryWrapper.like(name != null, Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * PutMapping不需要写url，因为发送的请求路由就是/employee
     * */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        // 这些替换成了封装工具类 BaseContext
        //Long empId = (Long)request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);

        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 点击编辑按钮时，页面将跳转到add.html，并在url中携带参数员工id
     * */
    @GetMapping("/{id}")
    // 这里使用@PathVariable是因为要传递的是路径上的id，而不是json形式中的id
    public R<Employee> getById(@PathVariable("id") Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
