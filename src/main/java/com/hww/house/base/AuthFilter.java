package com.hww.house.base;

import com.hww.house.entity.User;
import com.hww.house.exception.HouseException;
import com.hww.house.service.MessageService;
import com.hww.house.service.UserService;
import com.hww.house.util.LoginUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/26
 * @Time: 16:04
 * Description:
 */

public class AuthFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;


    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String name = obtainUsername(request);
        if (!"".equals(name)) {
            request.setAttribute("username", name);
            return super.attemptAuthentication(request, response);
        }

        String telephone = request.getParameter("telephone");
        if (telephone==null){
            throw new HouseException("Wrong telephone number");
        }
        if (!LoginUserUtil.checkTelephone(telephone)) {
            throw new HouseException("Wrong telephone number");
        }

        User user = userService.findUserByTelephone(telephone);
        String inputCode = request.getParameter("smsCode");
        String sessionCode = messageService.getMessage(telephone);
        if (Objects.equals(inputCode, sessionCode)) {
            // 如果用户第一次用手机登录 则自动注册该用户
            if (user == null) {
                user = userService.addUserByPhone(telephone);
            }
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } else {
            throw new BadCredentialsException("smsCodeError");
        }
    }
}
