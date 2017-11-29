package com.zw.p2p.cashticket;

import com.zw.p2p.rateticket.RateTicketConstants.RESOURCETYPE;

public class CashticketConstants {
	/**
	 * 现金券状态
	 * 
	 * @author Administrator
	 * r
	 */
	public static class CashticketStatus {
		/**
		 * 可用
		 */
		public static final String ENABLE = "enable";
		/**
		 * 用户使用
		 */
		public static final String USERUSE = "useruse";
		/**
		 * 管理员停用
		 */
		public static final String ADMINDISABLE = "admindisable";
	}
	public enum RESOURCETYPE {
		/**
		 * 管理员干预
		 */
		admindo("专属现金券", 3);

		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private RESOURCETYPE(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (RESOURCETYPE c : RESOURCETYPE.values()) {
				if (c.getIndex() == index) {
					return c.name;
				}
			}
			return null;
		}

		public static int getIndex(String name) {

			for (RESOURCETYPE c : RESOURCETYPE.values()) {
				if (c.getName().equals(name)) {
					return c.index;
				}
			}
			return 0;
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
