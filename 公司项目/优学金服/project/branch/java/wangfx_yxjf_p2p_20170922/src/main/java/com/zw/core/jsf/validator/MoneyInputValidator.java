package com.zw.core.jsf.validator;

import org.omnifaces.util.Components;
import org.omnifaces.util.Messages;
import org.omnifaces.validator.ValueChangeValidator;

import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

//@Component
@FacesValidator(value = "com.zw.core.validator.MoneyInputValidator")
public class MoneyInputValidator extends ValueChangeValidator implements PartialStateHolder {

	public static final String VALIDATOR_ID = "com.zw.core.validator.MoneyInputValidator";

	// 必须为多少的倍数
	private Long cardinal, min, max;

	@Override
	public void validateChangedObject(FacesContext context,
									  UIComponent component, Object arg2) throws ValidatorException {
		Long number;
		if (arg2 instanceof Long) {
			number = (Long) arg2;
		} else if (arg2 instanceof Double) {
			number = ((Double) arg2).longValue();
		} else {
			throw new RuntimeException(arg2.toString() + " 不是一个有效数字");
		}
		if (number != null) {
			checkRange(component, number);
			checkCardinal(component, number);
		}

	}

	private void checkRange(UIComponent component, Long number){
		if ( min!=null && max!=null ){
			if ( min>number || max<number )
				throw new ValidatorException(Messages.createError("{0}必须在{1}与{2}之间", Components.getLabel(component), min, max));
		}
		else if ( min!=null && min>number ){
			throw new ValidatorException(Messages.createError("{0}必须大于{1}", Components.getLabel(component), min));
		}
		else if ( max!=null && max<number ){
			throw new ValidatorException(Messages.createError("{0}必须小于{1}", Components.getLabel(component), max));
		}

	}

	private void checkCardinal(UIComponent component, Long number) {
		if (cardinal != null) {
			if (cardinal == 0) {
				// 除数为0，抛异常
				throw new ValidatorException(Messages.createError(
						"{0}不能为0的倍数",
						Components.getLabel(component)));
			}
			if (number % cardinal != 0) {
				// 验证未通过
				throw new ValidatorException(Messages.createError("{0}必须为"
						+ cardinal + "的倍数",
						Components.getLabel(component)));
			}
		}
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		if (context == null) {
			throw new NullPointerException();
		}
		if (state != null) {
			Object values[] = (Long[]) state;
			cardinal = (Long) values[0];
			min = (Long) values[1];
			max = (Long) values[2];
		}
	}

	@Override
	public Object saveState(FacesContext context) {
		if (context == null) {
			throw new NullPointerException();
		}
		if (!initialStateMarked()) {
			Object values[] = new Long[3];
			values[0] = cardinal;
			values[1] = min;
			values[2] = max;
			return values;
		}
		return null;
	}

	@Override
	public void setTransient(boolean arg0) {

	}

	public Long getCardinal() {
		return cardinal;
	}

	public void setCardinal(Long cardinal) {
		this.cardinal = cardinal;
	}

	public Long getMin() {
		return min;
	}

	public void setMin(Long min) {
		this.min = min;
	}

	public Long getMax() {
		return max;
	}

	public void setMax(Long max) {
		this.max = max;
	}

	private boolean initialState;

	public void markInitialState() {
		initialState = true;
	}

	public boolean initialStateMarked() {
		return initialState;
	}

	public void clearInitialState() {
		initialState = false;
	}
}