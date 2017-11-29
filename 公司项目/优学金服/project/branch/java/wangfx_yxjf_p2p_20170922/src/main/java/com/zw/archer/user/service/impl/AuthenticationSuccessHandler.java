package com.zw.archer.user.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lijin on 15/5/13.
 */
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        Cookie cookie = new Cookie("JSESSIONID", request.getSession().getId());
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
        super.onAuthenticationSuccess(request, response, authentication);
//        String redirectUrl= "http://"+ request.getServerName();
//        if (request.getServerPort()==80 || request.getServerPort()==8443 || request.getServerPort()==443)
//            redirectUrl+=request.getContextPath();
//        else
//            redirectUrl+=":"+ String.valueOf(request.getServerPort())+request.getContextPath();
//
//        redirectUrl+= this.getDefaultTargetUrl();
//
//        response.sendRedirect(redirectUrl);
    }
}
