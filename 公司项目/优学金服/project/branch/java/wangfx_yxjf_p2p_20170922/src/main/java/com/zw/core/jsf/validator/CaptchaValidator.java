package com.zw.core.jsf.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.sun.faces.util.MessageFactory;
import com.zw.archer.common.service.CaptchaService;
import com.zw.core.util.SpringBeanUtil;

//@Component
//FIXME:jar包中，注入有问题？
@FacesValidator(value = "com.zw.core.validator.CaptchaValidator")
public class CaptchaValidator implements Validator {

	public static final String VALIDATOR_ID = "com.zw.core.validator.CaptchaValidator";

//	@Resource
	CaptchaService captchaService;

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object arg2) throws ValidatorException {
		
		String value = (String) arg2;
		if (StringUtils.isEmpty(value)) {
			throw new ValidatorException(MessageFactory.getMessage(context,
					"javax.faces.component.UIInput.REQUIRED",
					MessageFactory.getLabel(context, component)));
		}
		if (captchaService == null) {
			captchaService = (CaptchaService) SpringBeanUtil.getBeanByName("captchaService");
		}
		if (!captchaService.verifyCaptcha(value, (HttpSession) context
				.getExternalContext().getSession(true))) {
			throw new ValidatorException(
					MessageFactory
							.getMessage(
									context,
									"com.zw.core.validator.CaptchaValidator.CAPTCHA_ERROR",
									MessageFactory.getLabel(context, component),
									value));
		}
	}

}
