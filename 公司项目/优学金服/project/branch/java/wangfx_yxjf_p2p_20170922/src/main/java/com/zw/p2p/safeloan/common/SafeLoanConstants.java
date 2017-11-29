package com.zw.p2p.safeloan.common;

import java.util.Date;

public class SafeLoanConstants {

	/**
	 * 
	 * @Description: TODO(无忧宝状态)
	 * @author cuihang
	 * @date 2016-1-12 下午2:21:22
	 */
	public enum SafeLoanStatus {

		/**
		 * 0 审核中
		 */
		CS("审核中", 0),

		/**
		 * 1 投资中
		 */
		TZZ("投标中", 1),
		/**
		 * 2复核中
		 */
		FHZ("复核中", 2),

		/**
		 * 3已满标
		 */
		YMB("已满标", 3),
		/**
		 * 4审核不通过
		 */
		BTG("不通过", 4);
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private SafeLoanStatus(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (SafeLoanStatus c : SafeLoanStatus.values()) {
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
	/**
	 * 
	* @Description: TODO(用一句话描述该文件做什么) 
	* @author cuihang   
	* @date 2016-6-7 上午10:42:01
	 */
	public enum SafeLoanInteerType{
		
		/**
		 * 1自动匹配散标，自动复利投资 第一版无忧宝
		 */
		ONE("自动匹配散标，自动复利投资", 1),
		
		/**
		 * 2自动匹配期限一致的散标，不复利投资
		 */
		TWO("自动匹配期限一致的散标，不复利投资", 2);
		// 成员变量
		private String name;
		private int index;
		
		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private SafeLoanInteerType(String name, int index) {
			this.name = name;
			this.index = index;
		}
		
		// 普通方法
		public static String getName(int index) {
			
			for (SafeLoanInteerType c : SafeLoanInteerType.values()) {
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
public enum SafeLoanUnit{
		
		/**
		 * 1自动匹配散标，自动复利投资 第一版无忧宝
		 */
		DAY("天", "day"),
		
		/**
		 * 2自动匹配期限一致的散标，不复利投资
		 */
		MONTH("月", "month");
		// 成员变量
		private String name;
		private String index;
		
		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private SafeLoanUnit(String name, String index) {
			this.name = name;
			this.index = index;
		}
		
		// 普通方法
		public static String getName(String index) {
			
			for (SafeLoanUnit c : SafeLoanUnit.values()) {
				if (c.getIndex().equals(index) ) {
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
		
		public String getIndex() {
			
			return index;
		}
		
		public void setIndex(String index) {
			
			this.index = index;
		}
	}
	/**
	 * @Description: TODO(启用停用状态)
	 * @author cuihang
	 * @date 2016-1-12 下午12:01:57
	 */
	public enum EnableStatus {
		/**
		 * 1 启用
		 */
		Y("可用", 1),

		/**
		 * 2停用
		 */
		N("停用", 2);

		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private EnableStatus(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (EnableStatus c : EnableStatus.values()) {
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

	public enum EnableExit {
		/**
		 * 1 允许退出
		 */
		Y("允许退出", 1),

		/**
		 * 2不允许退出
		 */
		N("不允许退出", 2);

		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private EnableExit(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (EnableExit c : EnableExit.values()) {
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

	/**
	 * 
	 * @Description: TODO(无忧宝状态)
	 * @author cuihang
	 * @date 2016-1-12 下午2:21:22
	 */
	public enum SafeLoanRecordStatus {


		/**
		 * 1 封闭期
		 */
		FBQ("封闭期", 1),
		/**
		 * 2 结算期
		 */
		JSQ("结算期", 2),
		/**
		 * 3已结清
		 */
		YJQ("已结清", 3);
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private SafeLoanRecordStatus(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (SafeLoanRecordStatus c : SafeLoanRecordStatus.values()) {
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

	public enum SafeLoanUserLoanStatus {
		/**
		 * 0投资中
		 */
		TZZ("投资中", 0),

		/**
		 * 1 流标
		 */
		LB("流标", 1),
		/**
		 * 2已收益
		 */
		YSY("已收益", 2),
		/**
		 * 3已转让
		 */
		YZR("已转让", 3);
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private SafeLoanUserLoanStatus(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (SafeLoanUserLoanStatus c : SafeLoanUserLoanStatus.values()) {
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

	public enum TurnLoanRecordStatus {

		/**
		 * 0初始
		 */
		CS("初始", 0),

		/**
		 * 1已转出
		 */
		YZC("已转出", 1),
		/**
		 * 2债权到期
		 */
		ZQDQ("债权到期", 2);
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private TurnLoanRecordStatus(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (TurnLoanRecordStatus c : TurnLoanRecordStatus.values()) {
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
	public enum MoneyDetailRecordType {

		/**
		 * 0接收债权
		 */
		JSZQ("接收债权", 0),

		/**
		 * 1自动投标
		 */
		ZDTB("自动投标", 1),
		/**
		 * 2债权到期
		 */
		ZQDQ("债权到期", 2),
		/**
		 * 3债权转出
		 */
		ZQZC("债权转出", 3),
		/**
		 * 4扣除管理费
		 */
		KCGLF("扣除管理费", 4),
		/**
		 * 5返本付息
		 */
		FBFX("返本付息", 5),
		/**
		 * 6结息
		 */
		JX("结息", 6),
		/**
		 * 7流标
		 */
		LB("流标", 7),
		/**
		 * 8补贴利息
		 */
		BT("补齐收益", 8),
		/**
		 *9未投返还
		 */
		WTFH("未投返还", 9)
		;
		// 成员变量
		private String name;
		private int index;

		// 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
		private MoneyDetailRecordType(String name, int index) {
			this.name = name;
			this.index = index;
		}

		// 普通方法
		public static String getName(int index) {

			for (MoneyDetailRecordType c : MoneyDetailRecordType.values()) {
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
	public static Date remindTime=new Date(); 
}
