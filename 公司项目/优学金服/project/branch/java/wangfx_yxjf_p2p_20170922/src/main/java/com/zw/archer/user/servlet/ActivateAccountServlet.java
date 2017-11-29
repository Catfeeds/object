package com.zw.archer.user.servlet;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Component;

import com.zw.archer.common.exception.AuthInfoAlreadyActivedException;
import com.zw.archer.common.exception.AuthInfoOutOfDateException;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.service.UserService;
import com.zw.core.annotations.Logger;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.SpringBeanUtil;

/**
 * 邮件激活账号
 * 
 * @author yinjunlu
 * 
 */
@Component
public class ActivateAccountServlet extends HttpServlet {

	private static final long serialVersionUID = -6074598131359403903L;

	@Logger
	static Log log;

	@Resource
	UserService userService;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String code = request.getParameter("activeCode");
		if (StringUtils.isNotEmpty(code)) {
			try {
				userService.activateUserByEmailActiveCode(code);
				response.sendRedirect(request.getContextPath() + "/regSuccess");
			} catch (AuthInfoOutOfDateException e) {
				response.sendRedirect(request.getContextPath() + "/activefail");
			} catch (UserNotFoundException e) {
				response.sendRedirect(request.getContextPath() + "/activefail");
			} catch (NoMatchingObjectsException e) {
				response.sendRedirect(request.getContextPath() + "/activefail");
			} catch (AuthInfoAlreadyActivedException e) {
				response.sendRedirect(request.getContextPath() + "/regSuccess");
			}
		} else {
			response.sendRedirect(request.getContextPath() + "/activefail");
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);

	}

	public void init() throws ServletException {
		// Put your code here
	}

}
