package com.zw.p2p.bankcard;


/**
 * BankCard constants.
 *
 */
public class BankCardConstants {
	
	/**
	 * Package name.
	 */
	public static final String Package = "com.zw.archer.bank";
	
	public static class BankCardStatus{
		public static final String NEWADD = "新增";
		public static final String BINDING = "绑定";
		public static final String DELETED = "已删除";
	}
	public static class BankCardPayType{
		public static final String QUICKPAY = "fast";//汇付快捷
		public static final String QUXIAN = "qx";//取现卡
	}
	public enum BankCardUnbindApproveStatus {

		/**
		 * 1 审核中
		 */
		CS("审核中", 1),

		/**
		 * 2 审核通过
		 */
		TG("审核通过", 2),
		/**
		 * 3审核不通过
		 */
		BH("审核不通过", 3);
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private BankCardUnbindApproveStatus(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (BankCardUnbindApproveStatus c : BankCardUnbindApproveStatus.values()) {
				if (c.getIndex() == index) {
					return c.name;
				}
			}
			return null;
		}

		// get set 方法
		public String getName() {

			return name;
		}

		public void setName(String name) {

			this.name = name;
		}

		public int getIndex() {

			return index;
		}

		public void setIndex(int index) {

			this.index = index;
		}
	}	
	public enum BankCardUnbindApproveResouse {

		/**
		 * 1 PC端
		 */
		PC("PC端", 1),

		/**
		 * 2 微信端
		 */
		WX("手机端", 2),
		/**
		 * 2 审核通过
		 */
		APP("APP", 3),;
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private BankCardUnbindApproveResouse(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (BankCardUnbindApproveResouse c : BankCardUnbindApproveResouse.values()) {
				if (c.getIndex() == index) {
					return c.name;
				}
			}
			return null;
		}

		// get set 方法
		public String getName() {

			return name;
		}

		public void setName(String name) {

			this.name = name;
		}

		public int getIndex() {

			return index;
		}

		public void setIndex(int index) {

			this.index = index;
		}
	}	
	public enum BankCardUnbindApprovetype {
		/**
		 * 1 手动
		 */
		SD("手动审核", 1),

		/**
		 * 2 审核通过
		 */
		ZD("自动审核", 2);
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private BankCardUnbindApprovetype(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (BankCardUnbindApprovetype c : BankCardUnbindApprovetype.values()) {
				if (c.getIndex() == index) {
					return c.name;
				}
			}
			return null;
		}

		// get set 方法
		public String getName() {

			return name;
		}

		public void setName(String name) {

			this.name = name;
		}

		public int getIndex() {

			return index;
		}

		public void setIndex(int index) {

			this.index = index;
		}
	}	
}
