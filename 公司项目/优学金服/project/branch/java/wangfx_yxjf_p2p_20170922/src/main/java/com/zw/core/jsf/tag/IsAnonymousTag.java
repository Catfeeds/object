package com.zw.core.jsf.tag;
import java.io.IOException;

import javax.annotation.Resource;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.view.facelets.*;

import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.jsf.el.SpringSecurityELLibrary;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.statistics.controller.InvestStatistics;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * Taglib to combine the Spring-Security Project with Facelets <br />
 *
 * This is the Class responsible for making the <br />
 * <code><br />
 *     &lt;sec:isAnonymous;&gt;<br />
 *         The components you want to show only when the user is anonymous<br />
 *     lt;/sec:isAnonymous&gt;<br />
 * </code>
 * work.
 *
 *
 * @author Grzegorz Blaszczyk - http://www.blaszczyk-consulting.com/
 * @version %I%, %G%
 * @since 0.5
 */
public class IsAnonymousTag extends TagHandler
{
        public void apply(FaceletContext faceletContext, UIComponent uiComponent)
                        throws IOException, FacesException, FaceletException, ELException {

//                if(SpringSecurityELLibrary.isAnonymous()) {
//                        this.nextHandler.apply(faceletContext, uiComponent);
//                }
            //TODO 临时解决方案,最终还是得要完成 isInvestingTag
            //有投资的时候不能借款
            InvestStatistics investStatistics;
            FacesContext context= FacesUtil.getCurrentInstance();
            ValueBinding binding= context.getApplication().createValueBinding("#{investStatistics}");
            investStatistics= (InvestStatistics)binding.getValue(context);

            String loginUserId;
            Long activeInvestsCount=0l;
            SecurityContextImpl securityContextImpl = (SecurityContextImpl) FacesUtil
                    .getSessionAttribute("SPRING_SECURITY_CONTEXT");
            if (securityContextImpl != null) {
                loginUserId = securityContextImpl.getAuthentication().getName();
                activeInvestsCount= investStatistics.getActiveInvestsCount(loginUserId);
            }
            if(activeInvestsCount >0) {
                    this.nextHandler.apply(faceletContext, uiComponent);
            }
        }

        public IsAnonymousTag(ComponentConfig componentConfig) {
                super(componentConfig);
        }

}
