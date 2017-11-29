package com.zw.core.jsf.tag;

import com.zw.core.jsf.el.SpringSecurityELLibrary;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.view.facelets.*;

import java.io.IOException;

/**
 * 判断用户是否正在投资
 * Taglib to combine the Spring-Security Project with Facelets <br />
 *
 * This is the Class responsible for making the <br />
 * <code><br />
 *     &lt;sec:isAuthenticated;&gt;<br />
 *         The components you want to show only when the user is authenticated<br />
 *     lt;/sec:isAuthenticated&gt;<br />
 * </code>
 * work.
 *
 *
 * @author Grzegorz Blaszczyk - http://www.blaszczyk-consulting.com/
 * @version %I%, %G%
 * @since 0.5
 */
class IsInvestingTag extends TagHandler
{

        public void apply(FaceletContext faceletContext, UIComponent uiComponent)
                        throws IOException, FacesException, FaceletException, ELException {

//                if(SpringSecurityELLibrary.isInvesting()) {
//                        this.nextHandler.apply(faceletContext, uiComponent);
//                }
        }

        public IsInvestingTag(ComponentConfig componentConfig) {
                super(componentConfig); 
        }

}