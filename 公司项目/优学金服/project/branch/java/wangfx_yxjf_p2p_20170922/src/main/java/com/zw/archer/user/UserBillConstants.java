package com.zw.archer.user;

public final class UserBillConstants {
	private UserBillConstants() {
	}
	
	/**
	 * 造成资金转移的操作类型信息
	 * @author Administrator
	 *
	 */
	public final static class OperatorInfo {
		/**
		 * 投资成功
		 */
		public final static String INVEST_SUCCESS = "invest_success";
		
		/**管理员干预*/	
		public final static String ADMIN_OPERATION = "admin_operation";
		
		/**充值成功*/
		public final static String RECHARGE_SUCCESS = "recharge_success";
		
		/**申请借款*/
		public final static String APPLY_LOAN = "apply_loan";
		
		/**借款申请未通过*/
		public final static String REFUSE_APPLY_LOAN = "refuse_apply_loan";
		
		/**申请提现*/
		public final static String APPLY_WITHDRAW = "apply_withdraw";

		/**提现申请未通过*/
		public final static String REFUSE_APPLY_WITHDRAW = "refuse_apply_withdraw";

		/**正常还款*/
		public final static String NORMAL_REPAY = "normal_repay";
		
		/**提前还款*/
		public final static String ADVANCE_REPAY = "advance_repay";
		
		/**逾期还款*/
		public final static String OVERDUE_REPAY = "overdue_repay";

		/**借款流标*/
		public final static String CANCEL_LOAN = "cancel_loan";

		/**借款撤标*/
		public final static String WITHDRAW_LOAN = "withdraw_loan";
		
		/**借款放款*/
		public final static String GIVE_MONEY_TO_BORROWER = "give_money_to_borrower";
		
		/**提现成功*/
		public final static String WITHDRAW_SUCCESS = "withdraw_success";

		/**提现扣款,by lijin,2015-03-10*/
		public final static String WITHDRAW_PAYMENT = "withdraw_payment";
		/**充值扣款,by lijin,2015-03-10*/
		public final static String RECHARGE_PAYMENT = "recharge_payment";
		/**提现失败*/
		public final static String WITHDRAW_FAIL = "withdraw_fail";
		/**优惠券*/
		public final static String COUPON = "coupon";
		/**体验标用户收益 */
		public final static String TASTEINVETSIC = "tasteInvestincome";

		/**投资流标*/
		public static final String CANCEL_INVEST = "cancel_invest";
		
		/**债权转让*/
		public static final String TRANSFER = "transfer";
		
		/**众筹投资*/
		public static final String RAISE_INVEST = "raise_invest";
		
		/**众筹放款*/
		public static final String RAISE_GIVE_MONEY_TO_BORROWER = "raise_give_money_to_borrower";
		
		/**众筹流标*/
		public final static String RAISE_CANCEL_LOAN = "raise_cancel_loan";
		/**
		 * 签约无忧保
		 */
		public final static String INVEST_SAFE_LOAN = "invest_safe_loan";
		/**
		 * 无忧宝计划投资债权
		 */
		public final static String SAFE_LOAN_INVEST = "safe_loan_invest";
		/**
		 * 无忧宝投资转让债权
		 */
		public final static String SAFE_LOAN_INVEST_TURNL = "safe_loan_invest_turnl";
		/**
		 * 无忧宝转让债权
		 */
		public final static String SAFE_LOAN_TURN = "safe_loan_turn";
		/**
		 * 债权到期收益
		 */
		public final static String SAFE_LOAN_INVSET_INCOM = "safe_loan_invest_income";
		/**
		 * 债权流标返还
		 */
		public final static String SAFE_LOAN_INVSET_FAILRETURN = "safe_loan_invest_failreturn";
		/**
		 * 未匹配债权退还本金
		 */
		public final static String SAFE_LOAN_NOINVSET_RETURN = "safe_loan_noinvest_return";
		/**
		 * 投资无忧宝收益
		 */
		public final static String INVEST_SAFE_LOAN_INCOM = "invest_safe_loan_incom";
		/**
		 * 投资无忧宝扣除管理费
		 */
		public final static String INVEST_SAFE_LOAN_FEE = "invest_safe_loan_fee";
		/**
		 * 投资无忧宝补齐收益
		 */
		public final static String INVEST_SAFE_LOAN_ADDFEE = "invest_safe_loan_addfee";
		/**推广领取体验今奖励**/
		public final static String TG_LQTYJ = "tg_lqtyj";
		/**推广实名认证奖励**/
		public final static String TG_REALNAME = "tg_realname";
		/**推广投资体验今奖励**/
		public final static String TG_LOANTYJ = "tg_loantyj";
		/**推广绑卡奖励**/
		public final static String TG_BINDCARD = "tg_bindcard";
	}

	
	public final static class Type{
		
		/**
		 * 冻结
		 */
		public final static String FREEZE = "freeze";

		/**
		 * 解冻
		 */
		public final static String UNFREEZE = "unfreeze";
		
		/**
		 * 从余额转出 transfer out from balance
		 */
		public final static String TO_BALANCE = "to_balance";

		/**
		 * 转入到余额 tansfer into balance
		 */
		public final static String TI_BALANCE = "ti_balance";
		
		/**
		 * 从冻结金额中转出 transfer out frome frozen money
		 */
		public final static String TO_FROZEN = "to_frozen";

		/**
		 * 退还服务费
		 */
		public final static String PAYBACK = "payback";

		/**
		 * 手续费调整
		 */
		public final static String ADJUST_FEE = "adjust_fee";

		/**
		 * 手续费调整
		 */
		public final static String WITHDRAW_FAIL = "withdraw_fail";

		/**转入优惠券*/
		public final static String TI_COUPON = "ti_coupon";
		/**转出优惠券*/
		public final static String TO_COUPON = "to_coupon";
		/**新增无忧宝记录*/
		public final static String SAFELOAN_RECORD = "safeloan_record";
		
		/**余额转入无忧宝冻结金额**/
		public final static String SAFELOANFREEZE = "safeloan_freeze";
		/**解冻无忧宝冻结金额**/
		public final static String UNSAFELOANFREEZE = "unsafeloan_freeze";
		
		/**无忧宝冻结金额转出**/
		public final static String OUTSAFELOANFREEZE = "outsafeloan_freeze";
		/**转入无忧宝冻结金额**/
		public final static String INSAFELOANFREEZE = "insafeloan_freeze";
		/**无忧宝冻结金额转到冻结金额**/
		public final static String SAFELOANFREEZETOFREEZE = "safeloanfreeze_to_freeze";
		/**冻结金额转到无忧宝冻结金额**/
		public final static String FREEZETOSAFELOANF = "freeze_to_safeloanfreeze";

	}
	
}
