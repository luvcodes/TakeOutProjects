package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author ryanw
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * @param user 其中包括了手机号和验证码，见front/page/login.html中的sendMsgApi方法
     * */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
        // 获取手机号
        String phone = user.getPhone();

        if (!phone.isEmpty()) {
            //随机生成一个验证码
            String code = MailUtils.achieveCode();
            log.info("code = {}", code);

            // 这里的phone其实就是邮箱，code是我们生成的验证码
            // MailUtils.sendTestMail(phone, code);

            //验证码存session，方便后面拿出来比对
            session.setAttribute(phone, code);

            return R.success("验证码发送成功");
        }


        return R.error("验证码发送失败");
    }

    /**
     * 移动端登录
     * */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {

        log.info(map.toString());

        // 获取邮箱
        String phone = map.get("phone").toString();

        // 获取验证码
        String code = map.get("code").toString();

        // 从session中获取验证码, 也就是从浏览器中获取phone
        String codeInSession = session.getAttribute(phone).toString();

        // 比较这用户输入的验证码和session中存的验证码是否一致
        if (code != null && code.equals(codeInSession)) {
            // 如果能够比对成功，说明登录成功
            // 如果输入正确，判断一下当前用户是否存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

            // 判断当前手机号对应的用户是否为新用户，从数据库中查询是否有其邮箱
            queryWrapper.eq(User::getPhone, phone);

            // 手机号是唯一标识，所以用getOne
            User user = userService.getOne(queryWrapper);

            // 如果不存在，则创建一个，存入数据库
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                user.setName("用户" + codeInSession);
            }

            // 存个session，表示登录状态
            session.setAttribute("user", user.getId());

            // 并将其作为结果返回
            return R.success(user);
        }


        return R.error("登录失败");
    }


    /**
     * 移动端登出功能
     * */
    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest servletRequest) {
        // 这里的user参数就是上面存了session的登录状态的session
        servletRequest.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
