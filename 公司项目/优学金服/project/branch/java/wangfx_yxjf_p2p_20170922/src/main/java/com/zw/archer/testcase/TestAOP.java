package com.zw.archer.testcase;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TestAOP {

	/**
	 * 用户角色添加
	 * 
	 * @param user
	 * @param role
	 */
	@AfterReturning(value = "execution(public void com.zw.archer.testcase.TestService.test3())")
	public void addRole() {
		System.out.println(123123);
	}

}
