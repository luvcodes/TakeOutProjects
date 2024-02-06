package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.Autofill;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author ryanw
 */
@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user,status) " +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
    @Autofill(value = OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号而写的，但是这里可以看EmployeeMapper.xml文件中的update动态SQL
     * 所以这个方法也可以用来使用修改员工信息
     * </p>
     * Autofill注解是为了实现公共字段自动填充。自定义切面类 AutoFillAspect，统一拦截加入了 AutoFill 注解的方法
     * */
    @Autofill(value = OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 修改员工信息功能用，这个方法主要是为了在修改界面数据回显
     * */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
