/**NEW**/
var email_reg = /([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)/;
var phone_reg=/^1[34578]\d{9}$/;
var loginUserAccountStatus=false;
var loginPasswordStatus=false;
var loginVerifyStatus=false;
var AUTOLOGINMD5PWD="0";
var register_step="1";
/**OLD**/
var checkMsgButton = true;
var pwdStatus = false;
var mInvitecodeStatus = false;
var tel_status = false;
var nameStatus = false;
var mailStatus = false;
var mobilePwdStatus = false;
var msgcodeStatus = false;
var wait = 120;
var mobilePhone = null;
var invitecodeStatus = false;
var name_status = false;
var tel_status = false;
var tel_remark="0";
var card_status = false;
var bankCard_status=false;
var verifyword_status = false;
var verifyword_status_sms = false;
var Wi = [ 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 ]; // 加权因子
var ValideCode = [ 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2 ]; // 身份证验证位值.10代表X
var reset_ul2_phone = null;
var mdflag="";
var bankStatus=false;
var bankSelectStatus=false;
var bankNo=0;
var bankRemark="";
var bankName="";
var bankInfo="";
$(function() {
	$("#user-password").click(function(){
		$(this).val('');
		mdflag="1";
	});
	$(".remember_ul").children("li").children("i").click(function() {
		if ($(this).attr("class") == 'checked') {
			$(this).attr("class", "unchecked");
			$("#checkbox1").val("no");
			$("#checkbox" + $(this).data("value")).removeAttr("checked");
		} else {
			$(this).attr("class", "checked");
			$("#checkbox1").val("yes");
			$("#checkbox" + $(this).data("value")).attr("checked", "checked");
		}
	});
	$(".btn_register").children("button").hover(function() {
		$(this).stop(true, true).attr("class", "notdefalut");
	}, function() {
		$(this).stop(true, true).attr("class", "defalut");
	});
	
	$(".input_box").find("label").click(function() {
		var label = $(this);
		if ($(this).siblings("input").attr("class") == "box_input1") {
			var inputObj = $(this).siblings("input[class=box_input1]");
			inputObj.focus();
		} else if ($(this).siblings("input").attr("class") == "box_input2") {
			var inputObj = $(this).siblings("input[class=box_input2]");
			inputObj.focus();
		} else if ($(this).siblings("input").attr("class") == "box_input3") {
			var inputObj = $(this).siblings("input[class=box_input3]");
			inputObj.focus();
		}
		label.hide();
	});
	$("input[class=box_input1]").focus(function() {
		var inputObj = $(this);
		var label = $(this).siblings("label");
		label.hide();
		inputObj.blur(function() {
			if (inputObj.val().trim().length == 0) {
				label.show();
			}
		});
	});
	$("input[class=box_input2]").focus(function() {
		var inputObj = $(this);
		var label = $(this).siblings("label");
		label.hide();
		inputObj.blur(function() {
			if (inputObj.val().trim().length == 0) {
				label.show();
			}
		});
	});
	$("input[class=box_input3]").focus(function() {
		var inputObj = $(this);
		var label = $(this).siblings("label");
		label.hide();
		inputObj.blur(function() {
			if (inputObj.val().trim().length == 0) {
				label.show();
			}
		});
	});
	if(!bankStatus){
		$("#bankCardsNo").siblings("label").unbind("click");
	}
});
//验证手机号是否存在
function checkTel(tel) {
	$(".box_phone_tip").html(" ");
	if(register_step == "2"){//实名认证
		if(tel_remark!=$("#phoneatt").val().trim()){
			var telNo = $.trim(tel);
			if (telNo != '' && telNo.length > 0 && phone_reg.test(telNo.replace(" ","").replace(" ",""))) {
				$.post($.rootPath() + "user/validateMobile.do?tel=" + telNo.replace(" ","").replace(" ",""), null,function(data) {
					if ($.trim(data) == "fail") {
						tel_status = false;
						$("#phoneatt").siblings("i").attr("class","box_check_icon check_wrong");
						$(".box_phoneatt_tip").html("该手机号码已经存在，您可以直接<a href=\""+$.rootPath()+"user/login.html\" style=\"color:#FFFFFF;\">&nbsp;登录&nbsp;</a>");
					} else {
						tel_status = true;
						$("#phoneatt").siblings("i").attr("class","box_check_icon check_right");
						$(".box_phoneatt_tip").html("请确认是否是银行预留手机号？&nbsp;<a href=\"javascript:changBankTeslStatus();\" style=\"text-decoration: underline;color: #FFFFFF\">我要更换</a>");
						mobilePhone = telNo;
					}
				});
			} else if (telNo == '') {
				$("#phoneatt").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_phoneatt_tip").html("请输入手机号码!");
				tel_status = false;
				return;
			} else {
				$("#phoneatt").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_phoneatt_tip").html("手机号输入有误!");
				tel_status = false;
				return;
			}
		}else{
			$("#phoneatt").siblings("i").attr("class","box_check_icon check_right");
		}
		}else{//
		var telNo = $.trim(tel);
		if (telNo != '' && telNo.length > 0 && phone_reg.test(telNo.replace(" ","").replace(" ",""))) {
			mobilePhone = telNo;
			tel_status = true;
		} else if (telNo == '') {
			$("#mPhone").siblings("i").attr("class","box_check_icon check_wrong");
			$(".box_phone_tip").html("请输入手机号码!");
			tel_status = false;
			return;
		} else {
			$("#mPhone").siblings("i").attr("class","box_check_icon check_wrong");
			$(".box_phone_tip").html("手机号输入有误!");
			tel_status = false;
			return;
		}
	}
}
function formatTelNo(p){
	if(window.event.keyCode == 8){
	}else{
		if(p.length>0 && p.length<=4){
			if(p.length>=3){
				p+=" ";
			}
		}
		if(p.length>4 && p.length<=9){
			if(p.length>=8){
				var a = p.substring(0,8);
				var b = p.substring(8,p.length+1);
				p=a+" "+b;
			}
		}
		$("#mPhone").val(p);
	}
}
//发送短信校验码
function sendMsg(p) {
	if (!verifyword_status_sms) {
		alert("验证码输入有误，请重新输入！");
		return false;
	} else if (checkMsgButton) {
		checkMsgButton = false;
		if (wait != 120) {
			return;
		}
		if (!tel_status) {
			if(p == 'register'){
				$("#mPhone").siblings("i").attr("class","box_check_icon check_wrong");
			}else{
			$(".phonetip").attr("style", "color:#e43448");
			}
			$(".phonetip").html("您的手机号码输入有误，请重新输入");
			checkMsgButton = true;
		} else {
			var telNo = $.trim($("#mPhone").val());
			if (!telNo) {
				if(p == 'register'){
					$("#mPhone").siblings("i").attr("class","box_check_icon check_wrong");
				}else{
				$(".phonetip").attr("style", "color:#e43448");
				}
				$(".phonetip").html("您的手机号码输入有误，请重新输入");
				checkMsgButton = true;
				return;
			}
			$.post($.rootPath() + "msg/sendMSG.do?tel=" + telNo.replace(/[ ]/g, ""),function(data){
				var fl = data.split(",", 1);
				var f = data.split(",", 2);
				if (fl == "fail") {
					console.warn("当天发送短信验证码已达："+f[1]);
					if(f[1] == undefined || f[1] == 'undefined'){
						alert("短信发送失败，请稍后再试！");
					}else{
						alert("当天发送短信验证码已达" + f[1] + "条，请勿频繁发送。");
					}
					checkMsgButton = true;
				} else if (data == "failByUser") {
					alert("您当天的发送短信验证码已超限额，请联系客服解除限制...");
					checkMsgButton = true;
				} else if (data == "success") {
					alert("短信验证码已经发送，请您注意查收");
					var secondsremained = "";
					if (p == 'register') {
						addCookie('secondsremainedregister', 120, 120);
						secondsremained = "register";
					} else if (p == 'restpwd') {
						addCookie('secondsremainedrestpwd', 120, 120);
						secondsremained = "restpwd";
					}
					time(wait, secondsremained);
				} else {
					alert("系统繁忙，请稍后再试");
					checkMsgButton = true;
				}				
			});
		}
	}
}
//Register   鼠标移开开短信校验
function checkMSGCode(msgCode) {
	if (null == msgCode || "" == msgCode) {
		msgcodeStatus = false;
		$("#smsverifyword").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_smsverifycode_tip").html("请输入短信验证码");
	} else {
		$.post($.rootPath() + "msg/verifyMSGCode.do",{"msgCode" : msgCode,"mPhone" : $("#mPhone").val()},function(data){
			if (data == "codeErr") {
				$("#smsverifyword").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_smsverifycode_tip").html("验证码校验失败");
				msgcodeStatus = false;
			} else if (data == "expire") {
				$("#smsverifyword").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_smsverifycode_tip").html("您收到的校验码已过期，请重新获取验证码！");
				msgcodeStatus = false;
			} else {
				$("#smsverifyword").siblings("i").attr("class","box_check_icon check_right");
				$(".box_smsverifycode_tip").html("验证码校验成功");
				msgcodeStatus = true;
			}			
		});
	}
}
// 设置短信验证码120s发送时间
function time(param, param2) {
	if (param == 0) {
		checkMsgButton = true;
		$(".getVerifyWord").attr("style","background-color:#e43448;color:#ffffff");
		$(".getVerifyWord").html("点击获取");
		param = 120;
	} else {
		checkMsgButton = false;
		$(".getVerifyWord").attr("style","background-color:#EEEEF0;color:#7c7c7c");
		$(".getVerifyWord").html(param + "秒");
		setTimeout(function() {
			param--;
			time(param, param2);
		}, 1000);
		if (param2 == 'register') {
			editCookie("secondsremainedregister", param, param + 1);
		} else if (param2 == 'restpwd') {
			editCookie("secondsremainedrestpwd", param, param + 1);
		}
	}
}
// 判读密码非空
function checkPass() {
	var mpassword = $("#mpassword").val();
	if (mpassword.trim().length == 0) {
		$("#mpassword").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_password_tip").html("请输入密码");
		pwdStatus = false;
	} else if (mpassword.trim().length < 6 || mpassword.trim().length > 16) {
		$("#mpassword").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_password_tip").html("请输入6到16位密码");
		pwdStatus = false;
	} else {
		$("#mpassword").siblings("i").attr("class","box_check_icon check_right");
		$(".box_password_tip").html("");
		pwdStatus = true;
	}
}
// 验证邀请码是否存在
function mInvitecode() {
	var mInv = $("#invite").val();
	var str = mInv.substring(0, 1);
	if (null != mInv && "" != mInv && "I" != str) {
		$("#invite").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_invitecode_tip").html("请注意邀请码的格式");
		mInvitecodeStatus = false;
	} else if(null != mInv && "" != mInv){
		$.post($.rootPath() + "user/mInvitecode.do",{"mInvitecode" : $("#invite").val()},function(data){
			if ("0" == data) {
				$(".invitenumbertip").attr("style", "color:#e43448");
				$(".invitenumbertip").html("您输入的邀请码不存在");
				mInvitecodeStatus = false;
				$(".invite").foucs();
			} else if ("1" == data) {
				$(".invitenumbertip").attr("style", "color:#34A242");
				$(".invitenumbertip").html("邀请码校验成功");
				mInvitecodeStatus = true;
			} else if (null == $(".invite").val()
					|| "" == $(".invite").val()) {
				mInvitecodeStatus = true;
			}			
		});
	}
}
var wnflag = false;
function registerPost() {
	var registerSource ="";
	if($.session.get(b64_md5("invest-source"))!=null&&$.session.get(b64_md5("invest-source"))!=""){
		registerSource =$.session.get(b64_md5("invest-source"));
	}
	if(null!=queryString("marketSource")&&""!=queryString("marketSource")){
		registerSource =queryString("marketSource");
	}
	if (!wnflag) {
		if ($("input[name='magree']").val() != "0") {
			wnflag = false;
			$.dialog("您未同意使用协议，无法注册");
		} else if (pwdStatus && msgcodeStatus && tel_status) {
			try {
			wnflag = true;
				$(".register").attr("style","background-color: #EEEEF0;color: #7C7C7C;border: #EEEEF0 2px solid;").removeAttr("onclick");
			$(".register").html("正在注册中，请稍后");
				if ($("#invite").val().trim().length != 0 && mInvitecodeStatus) {
				mobilePhone = $("#mPhone").val().replace(/[ ]/g, "");
				var regg = /^(\d{3})(\d{4})(\d{4})$/;
				var matches = regg.exec(mobilePhone);
				var newNum = matches[1] + ' ' + matches[2] + ' ' + matches[3];
				
				$("#tjzhuce").attr('disabled', 'disabled');
					$.post($.rootPath() + "user/mobileRegister.do",{"mRegisterType" : $("#mRegisterType").val(),"mPhone" : $("#mPhone").val().replace(/[ ]/g, ""), "mpassword" : $("#mpassword").val(),"mCaptcha" : $("#verifyword").val(),"mInvitecode" : $("#invite").val(),"sourcedb":registerSource},function(result) {
						if (result.msg == '0') {
							$.session.set("userAccount",result.userAccount);
							$.session.set("userId", result.userId);
							$.session.set("useableMoney",result.useableMoney);
							$.session.set("totalMoney",result.totalMoney);
							$.session.set("totalCaculus",result.totalCaculus);
							if($("#thd_register_type").val() !=null || $("#thd_register_type").val() !=undefined){
								location.href=$.rootPath()+"acct/index.html";
							}else{
								$("#step1").hide();
								$("#step2").show();
								register_step="2";
								/*$(".icon1").attr("style","background:url("+$.rootPath()+"images/user/register_step_gray2.png) no-repeat");
								$(".icon2").attr("style","background:url("+$.rootPath()+"images/user/register_step_gray1.png) no-repeat");*/
								/*$(".right_step .li1").find(".p1").html("完成账号注册<span style=\"color: #FAE66F;\">（已完成）</span>");
								$(".right_step .li2").find(".p1").html("完成实名认证<span style=\"color: #FAE66F;\">（进行中）</span>");*/
								$("input[name=phoneatt]").val(newNum);
							}
						} else if (result.msg == '3') {
							$.dialog("信息已提交，请勿重复提交..");
						} else {
							wnflag = false;
							$(".register").attr("style","background-color: #ffffff;color: #e43448;border: #e43448 2px solid;").attr("onclick","registerPost();");
							$(".register").html("提交注册");
						}
					});
			} else {
				mobilePhone = $("#mPhone").val();
				$("#tjzhuce").attr('disabled', 'disabled');
					$.post($.rootPath() + "user/mobileRegister.do",{"mRegisterType" : $("#mRegisterType").val(),"mPhone" : $("#mPhone").val().replace(/[ ]/g, ""),"mpassword" : $("#mpassword").val(),"mCaptcha" : $("#verifyword").val(),"mInvitecode" : $("#invite").val(),"sourcedb":registerSource},function(result) {
						if (result.msg == '0') {
							$.session.set("userAccount",result.userAccount);
							$.session.set("userId", result.userId);
							$.session.set("useableMoney",result.useableMoney);
							$.session.set("totalMoney",result.totalMoney);
							$.session.set("totalCaculus",result.totalCaculus);
							if($("#thd_register_type").val() !=null || $("#thd_register_type").val() !=undefined){
								location.href=$.rootPath()+"acct/index.html";
							}else{
								$("#step1").hide();
								$("#step2").show();
								register_step="2";
								/*$(".right_step .li1").find(".p1").html("完成账号注册<span style=\"color: #FAE66F;\">（已完成）</span>");
								$(".right_step .li2").find(".p1").html("完成实名认证（<span style=\"color: #FAE66F;\">进行中</span>）");*/
								$("input[name=phoneatt]").val(mobilePhone);
							}
						} else if (result.msg == '3') {
							$.dialog("信息已提交，请勿重复提交..");
						} else {
							wnflag = false;
							$(".register").attr("style","background-color: #f8802a;color: #ffffff;").attr("onclick","registerPost();");
							$(".register").html("提交注册");
						}
					});
				}
			} catch (e) {
				$.dialog("注册失败，请稍后再试或联系客服！");
				wnflag = false;
				console.warn(e);
				$(".register").attr("style","background-color: #f8802a;color: #ffffff;").attr("onclick","registerPost();");
				$(".register").html("提交注册");
			}
		}
	}
}
function selectAgree() {
	var agreeStatus = $(".magree").val();
	if (agreeStatus == '0') {
		$(".magree").removeAttr("checked");
		$(".magree").val("1");
	} else {
		$(".magree").attr("checked", "checked");
		$(".magree").val("0");
	}
}
// 实名认证
function checkName(name) {
	var realName = $.trim(name);
	var regName = /^[\u4e00-\u9fa5]{2,4}$/;
	if (realName != '' && realName.length > 0 && regName.test(realName)) {
		name_status = true;
		$("#realName").siblings("i").attr("class","box_check_icon check_right");
		$(".box_realName_tip").html("");
	} else {
		$("#realName").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_realName_tip").html("您的姓名格式不正确！");
		name_status = false;
	}
}
function checkCard(card) {	
	var cardNo = $.trim(card);
	var la6=cardNo.substr(0,6);
	var la8=cardNo.substr(6,8);
	var la4=cardNo.substr(14,17);
	if (cardNo != '' && cardNo.length > 0 && IdCardValidate(cardNo)) {
		$.post($.rootPath() + "user/validateCardId.do?card_id=" + cardNo, null,function(data) {
					if ($.trim(data) == "fail") {
						card_status = false;
				$("#cardNo").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_cardNo_tip").html("该身份证号码已经存在");
					} else {
						card_status = true;
				$("#cardNo").siblings("i").attr("class","box_check_icon check_right");
				$(".box_cardNo_tip").html("");
				/*$(".box_cardNo_tip").html("");
						 var newNum = la6 + ' ' + la8 + ' ' + la4;
				$("#cardNo").val(newNum);*/
						return;
					}
				});
	} else {
		$("#cardNo").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_cardNo_tip").html("身份证号码输入有误");
		card_status = false;
	}
}
function checkBankCard(p) {
	if(p.trim().length==0){
		$("#bankCardsNo").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_bankCardsNo_tip").html("请输入绑定的借记卡号！");
		bankCard_status=false;
	}else{
		$(".box_bankCardsNo_tip").html("正在检测中...");
		var data={};
		console.warn(bankName);
		data.bandName=bankName;
		data.bankCard=p.replace(/[ ]/g, "");
		$.post($.rootPath()+"user/checkBankCard.do",data,function(result){
			if (result.msg == "0") {
				$("#bankCardsNo").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_bankCardsNo_tip").html("请输入绑定的借记卡号！");
				bankCard_status=false;
			}else{
				$("#bankCardsNo").siblings("i").attr("class","box_check_icon check_right");
				$("#bankCard").val(result.bankCard);
				$(".box_bankCardsNo_tip").html(result.msg);
				bankCard_status=true;
			}
		});
	}
}
function cardcallback(data) {
	if ($.trim(data) == "fail") {
		$("#cardsNo").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_invitecode_tip").html("该身份证号码已经存在");
		card_status = false;
	} else {
		$("#cardsNo").siblings("i").attr("class","box_check_icon check_right");
		$(".box_invitecode_tip").html("");
		card_status = true;
		return;
	}
}
function IdCardValidate(idCard) {
	idCard = trim(idCard.replace(/ /g, "")); // 去掉字符串头尾空格
	if (idCard.length == 15) {
		return isValidityBrithBy15IdCard(idCard); // 进行15位身份证的验证
	} else if (idCard.length == 18) {
		var a_idCard = idCard.split(""); // 得到身份证数组
		if (isValidityBrithBy18IdCard(idCard)
				&& isTrueValidateCodeBy18IdCard(a_idCard)) { // 进行18位身份证的基本验证和第18位的验证
			return true;
		} else {
			return false;
		}
	} else {
		return false;
	}
}
/**
 * 判断身份证号码为18位时最后的验证位是否正确
 * 
 * @param a_idCard
 *            身份证号码数组
 * @return
 */
function isTrueValidateCodeBy18IdCard(a_idCard) {
	var sum = 0; // 声明加权求和变量
	if (a_idCard[17].toLowerCase() == 'x') {
		a_idCard[17] = 10; // 将最后位为x的验证码替换为10方便后续操作
	}
	for (var i = 0; i < 17; i++) {
		sum += Wi[i] * a_idCard[i]; // 加权求和
	}
	valCodePosition = sum % 11; // 得到验证码所位置
	if (a_idCard[17] == ValideCode[valCodePosition]) {
		return true;
	} else {
		return false;
	}
}
/**
 * 验证18位数身份证号码中的生日是否是有效生日
 * 
 * @param idCard
 *            18位书身份证字符串
 * @return
 */
function isValidityBrithBy18IdCard(idCard18) {
	var year = idCard18.substring(6, 10);
	var month = idCard18.substring(10, 12);
	var day = idCard18.substring(12, 14);
	var temp_date = new Date(year, parseFloat(month) - 1, parseFloat(day));
	// 这里用getFullYear()获取年份，避免千年虫问题
	if (temp_date.getFullYear() != parseFloat(year)
			|| temp_date.getMonth() != parseFloat(month) - 1
			|| temp_date.getDate() != parseFloat(day)) {
		return false;
	} else {
		return true;
	}
}
/**
 * 验证15位数身份证号码中的生日是否是有效生日
 * 
 * @param idCard15
 *            15位书身份证字符串
 * @return
 */
function isValidityBrithBy15IdCard(idCard15) {
	var year = idCard15.substring(6, 8);
	var month = idCard15.substring(8, 10);
	var day = idCard15.substring(10, 12);
	var temp_date = new Date(year, parseFloat(month) - 1, parseFloat(day));
	// 对于老身份证中的你年龄则不需考虑千年虫问题而使用getYear()方法
	if (temp_date.getYear() != parseFloat(year)
			|| temp_date.getMonth() != parseFloat(month) - 1
			|| temp_date.getDate() != parseFloat(day)) {
		return false;
	} else {
		return true;
	}
}
// 去掉字符串头尾空格
function trim(str) {
	return str.replace(/(^\s*)|(\s*$)/g, "");
}
var creatFlg=true;
function toCreate() {
	if(!creatFlg){
		$.dialog("实名认证中，请稍候！");
		return;
	}else{
		if(!bankStatus){
			$(".box_bankName_tip").html("请先选择银行！");
		}else if(!bankCard_status){
			$(".box_bankCardsNo_tip").html("请输入要绑定的借记卡号！");
		} else if(!tel_status){
			$(".box_phoneatt_tip").html("手机号输入有误!");
		} else if(!name_status){
			$(".box_realName_tip").html("您的姓名格式不正确！");
		} else if(!card_status){
			$(".box_cardNo_tip").html("请输入您本人的身份证号码！");
		}
		$(".create").attr("style","background-color: #EEEEF0;color: #7C7C7C;border: #EEEEF0 2px solid;").removeAttr("onclick");
		$(".create").html("正在开通，请稍后");
		// var realName = $.trim($("#realName").val());
		var realName = escape($.trim(encodeURI($('#realName').val(), "UTF-8")));
		var cardNo = $.trim($("#cardNo").val());
		var cardsNo = $.trim($("#bankCardsNo").val());
		var tel = $.trim($("input[name=phoneatt]").val());
		if (card_status && name_status && bankCard_status) {
			$.post($.rootPath() + "user/validateCardId.do?card_id=" + cardNo, null, function(data) {
								if ($.trim(data) == "fail") {
									card_status = false;
					$("#cardsNo").siblings("i").attr("class","box_check_icon check_wrong");
					$(".box_cardNo_tip").html("该身份证号码已经存在");
					$(".create").attr("style","background-color: #F8802A;color: #ffffff;").attr("onclick","toCreate()");
									$(".create").html("立即开通");
								} else {
									card_status = true;
					$("#cardsNo").siblings("i").attr("class","box_check_icon check_right");
					$(".box_cardNo_tip").html("");
					try {
						var url = $.rootPath() + "/nccbank/register.do?confirm=Y&realName=" + realName.replace(/[ ]/g, "") + "&tel=" + tel.replace(/[ ]/g, "") + "&cardNo=" + cardNo.replace(/[ ]/g, "") + "&cardsNo=" + cardsNo.replace(/[ ]/g, "");
						// var url = $.rootPath()+"/nccbank/register.do?confirm=Y&realName="+escape(encodeURI(realName))+"&tel="+tel+"&cardNo="+cardNo+"&cardsNo="+cardsNo;
									$.post(url, function(result) {
										if (result.msgCode == '0') {
								/*$(".right_step .li2").find(".p1").html("开通江西银行存管账号（已完成）");
								$(".right_step .li3").find(".p1").html("充值、理财返现金（<a style=\"color: #FAE66F;text-decoration:underline;\" href=\""+$.rootPath()+"acct/index.html\">去完成</a>）");*/
								location.href=$.rootPath()+"acct/index.html";
								/*window.location.href = $.rootPath() + "nccbank/respsuccess.html";*/
										} else {
								$(".create").attr("style","background-color: #F8802A;color: #ffffff;").attr("onclick","toCreate()");
											$(".create").html("立即开通");
											if (result.msgContent == '')
									$.dialog("实名认证未成功,请点击先看看浏览");
											else
									$.dialog(result.msgContent);
										}
									});
					} catch (e) {
						console.warn(e);
						$(".create").attr("style","background-color: #F8802A;color: #ffffff;").attr("onclick","toCreate()");
						$(".create").html("立即开通");
					}
								}
							});
		} else {
			$(".create").attr("style","background-color: #F8802A;color: #ffffff;").attr("onclick","toCreate()");
			$(".create").html("立即开通");
		}
	}
}
function getVerifyWord() {
	var url = $("#verifyA").data("url-verifyword") + "?d=" + new Date().getTime();
	$("#verify_code").attr("src", url);
}
function checkVerigyWordBeforSms() {
	if($("#captcha").val().trim()== ""){
		$("#captcha").siblings("i").attr("class","box_check_icon check_wrong");
		$(".box_verifycode_tip").html("验证码不能为空！");
		verifyword_status_sms = false;
	}else{
	var data = {};
	data.captcha = $("#captcha").val();
	$.post($.rootPath() + "user/isyanzheng.do", data, function(result) {
		if (result == '0') {
				$("#captcha").siblings("i").attr("class","box_check_icon check_right");
				$(".box_verifycode_tip").html("");
			verifyword_status_sms = true;
		} else {
				$("#captcha").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_verifycode_tip").html("验证码有误！");
			getVerifyWord();
			verifyword_status_sms = false;
		}
	}, "text");
}
}
function checkVerigyWord() {
	var data = {};
	data.captcha = $("#captcha").val();
	$.post($.rootPath() + "user/isyanzheng.do", data, function(result) {
		if (result == '0') {
			$(".verifywordatttip").attr("style", "color:#34A242");
			$(".verifywordatttip").html("");
			verifyword_status = true;
		} else {
			$(".verifywordatttip").attr("style", "color:#e43448");
			$(".verifywordatttip").html("验证码有误！");
			getVerifyWord();
			verifyword_status = false;
		}
	}, "text");
}
/* reset js area */
// check phone num exist
function checkTelReset(tel) {
	var reg = /^1[34578]\d{9}$/;
	var telNo = $.trim(tel);
	if (telNo != '' && telNo.length > 0 && reg.test(telNo)) {
		$.post($.rootPath() + "user/validateMobile.do?tel=" + telNo, null,function(data) {
					if ($.trim(data) == "fail") {
						tel_status = true;
						$(".phonetip").attr("style", "color:#34A242");
						$(".phonetip").html("手机号码输入正确");
						updateResetBtn1();
					} else {
						tel_status = false;
						$(".phonetip").attr("style", "color:#e43448");
						$(".phonetip").html("不存在该手机号！");
						mobilePhone = telNo;
					}
				});
	} else {
		$(".phonetip").attr("style", "color:#e43448");
		$(".phonetip").html("请输入手机号码");
		tel_status = false;
		return;
	}
}
// check sms verifyword is right
function checkMSGCodeReset(msgCode) {
	if (null == msgCode || "" == msgCode) {
		msgcodeStatus = false;
		$(".smsverifywordtip").attr("style", "color:#e43448");
		$(".smsverifywordtip").html("请输入短信验证码");
	} else {
		$.post($.rootPath() + "msg/verifyMSGCode.do",{"msgCode" : msgCode,"mPhone" : $("#mPhone").val()},function(data){
			if (data == "codeErr") {
				$(".smsverifywordtip").attr("style", "color:#e43448");
				$(".smsverifywordtip").html("验证码校验失败");
				msgcodeStatus = false;
			} else if (data == "expire") {
				$(".smsverifywordtip").attr("style", "color:#e43448");
				$(".smsverifywordtip").html("您收到的校验码已过期，请重新获取验证码！");
				msgcodeStatus = false;
			} else {
				$(".smsverifywordtip").attr("style", "color:#34A242");
				$(".smsverifywordtip").html("验证码校验成功");
				msgcodeStatus = true;
				updateResetBtn1();
			}			
		});
	}
}
// check verifyword is right
function checkVerigyWordReset() {
	var data = {};
	data.captcha = $("#captcha").val();
	$.post($.rootPath() + "user/isyanzheng.do", data, function(result) {
		if (result == '0') {
			$(".verifywordtip").attr("style", "color:#34A242");
			$(".verifywordtip").html("验证码校验正确！");
			verifyword_status = true;
			verifyword_status_sms = true;
			updateResetBtn1();
		} else {
			$(".verifywordtip").attr("style", "color:#e43448");
			$(".verifywordtip").html("验证码有误！");
			getVerifyWord();
			verifyword_status = false;
			verifyword_status_sms = false;
		}
	}, "text");
}
function updateResetBtn1() {
	if (verifyword_status && msgcodeStatus && tel_status) {
		$(".to_reset_next_a").attr("style","border:#e43448 2px solid;background-color:#ffffff;color:#e43448");
		$(".to_reset_next_a").hover(function() {
			$(".to_reset_next_a").attr("style","border:#e43448 2px solid;background-color:#e43448;color:#ffffff");
		},function() {
			$(".to_reset_next_a").attr("style","border:#e43448 2px solid;background-color:#ffffff;color:#e43448");
						});
		reset_ul2_phone = $("#mPhone").val();
		$(".to_reset_next_a").attr("onclick", "updateResetNextClick()");
	} else {
		$(".to_reset_next_a").attr("style","border:#BBBBBB 2px solid;background-color:#BBBBBB;color:#7C7C7C");
		reset_ul2_phone = null;
		$(".to_reset_next_a").removeAttr("onclick");
	}
}
function updateResetNextClick(status) {
	$(".ul1").attr("style", "display:none");
	$(".ul2").attr("style", "display:block");
}
// check new password is right
function checkPassReset() {
	var mpassword = $("#mpassword").val();
	if (mpassword.trim().length == 0) {
		$(".passwordtip").attr("style", "color:#e43448");
		$(".passwordtip").html("请输入密码");
		pwdStatus = false;
	} else if (mpassword.trim().length < 6 || mpassword.trim().length > 16) {
		$(".passwordtip").attr("style", "color:#e43448");
		$(".passwordtip").html("请输入6到16位密码");
		pwdStatus = false;
	} else {
		$(".passwordtip").attr("style", "color:#34A242");
		$(".passwordtip").html("密码校验成功");
		pwdStatus = true;
		updateResetBtn2();
	}
}
// check second password is right
function checkMobilePwdReset(pwd) {
	var pwd1 = $("#mpassword").val();
	if (pwd1.length < 6 || pwd1.length > 15) {
		$(".surepasswordtip").attr("style", "color:#e43448");
		$(".surepasswordtip").html("请输入6-15位字符，英文，数字");
		mobilePwdStatus = false;
	}
	if (!pwd1) {
		$(".surepasswordtip").attr("style", "color:#e43448");
		$(".surepasswordtip").html("请重复输入上面的密码");
		mobilePwdStatus = false;
	} else {
		var pwd2 = $.trim(pwd);
		if ($.trim(pwd1) != pwd2) {
			$(".surepasswordtip").attr("style", "color:#e43448");
			$(".surepasswordtip").html("两次密码不一致！");
			mobilePwdStatus = false;
		} else {
			$(".surepasswordtip").attr("style", "color:#34A242");
			$(".surepasswordtip").html("两次密码校验成功");
			mobilePwdStatus = true;
			updateResetBtn2();
		}
	}
}
function updateResetBtn2() {
	if (mobilePwdStatus && pwdStatus) {
		$(".to_reset_a").attr("style","border:#e43448 2px solid;background-color:#ffffff;color:#e43448");
		$(".to_reset_a").hover(function() {
			$(".to_reset_a").attr("style","border:#e43448 2px solid;background-color:#e43448;color:#ffffff");
		},function() {
			$(".to_reset_a").attr("style","border:#e43448 2px solid;background-color:#ffffff;color:#e43448");
						});
		$(".to_reset_a").attr("onclick", "updateResetClick();");
	} else {
		$(".to_reset_a").attr("style","border:#BBBBBB 2px solid;background-color:#BBBBBB;color:#7C7C7C");
		$(".to_reset_a").removeAttr("onclick");
	}
}
var secs = 1; // 倒计时的秒数
var URL;
function load(url) {
	URL = url;
	for (var i = secs; i >= 0; i--) {
		window.setTimeout('doUpdate(' + i + ')', (secs - i) * 1000);
	}
}
function doUpdate(num) {
	if (num == 0) {
		window.location = URL;
	}
}
function updateResetClick() {
	try {
		$.post($.rootPath() + "user/mobilePwd.do",{"mobile" : reset_ul2_phone,"password" : $("#mpassword").val(),"mobileType" : 'mobileType'},function(data){
			if ("success" == data.resu) {
				$.confirmWithBoth("重置登陆密码成功,即将跳转登录页面",function(){
					load($.rootPath() + "user/login.html");
				},function(){
					location.href=location.href;
				});
			}else if("fail" == data.resu){
				$.alert("重置密码失败，请稍后再试！");
			}
		});
	} catch (e) {
		// TODO: handle exception
		console.warn(e);
		$.alert("重置密码异常，请稍后再试！");
	}
}
function showOrHide() {
	var con = "<div  class=\"bankMain\"> "
			+ "<table class=\"bank_header\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;\">"
			+ "<tr>" + "<td width='200px'>渠道银行</td> "
			+ "<td width='300px'>代扣限额</td> " + "<td width='400px'>备注</td> "
			+ "</tr> " + "</tr> " + "<tr>" + "<td>建设银行</td> "
			+ "<td width='300px'>单笔50万单日50万</td> "
			+ "<td>在建设银行柜面或个人网银开通电子支付功能</td> " + "</tr> " + "<tr>"
			+ "<td>兴业银行</td> " + "<td>单笔50万单日50万</td> " + "<td></td> "
			+ "</tr> " + "<tr>" + "<td>中信银行</td> " + "<td>单笔50万单日50万</td> "
			+ "<td></td> " + "</tr> " + "<tr>" + "<td>民生银行</td> "
			+ "<td>单笔50万单日50万</td> " + "<td></td> " + "</tr> " + "<tr>"
			+ "<td>邮储银行</td> " + "<td>单笔50万单日50万</td> " + "<td></td> "
			+ "</tr> " + "<tr>" + "<td>光大银行</td> " + "<td>单笔50万单日50万</td> "
			+ "<td>在光大银行柜面或个人网银开通电子支付功能</td> " + "</tr> " + "<tr>"
			+ "<td>平安银行</td> " + "<td>单笔50万单日50万</td> "
			+ "<td>需开通银联无卡支付业务</td> " + "</tr> " + "<tr>" + "<td>工商银行</td> "
			+ "<td>单笔5万单日5万</td> " + "<td>暂不支持9558开头的银行卡、卡号为18位的卡</td> "
			+ "</tr> " + "<tr>" + "<td>农业银行</td> " + "<td>单笔单日2万</td> "
			+ "<td>暂不支持95595、95596、95597、95598、95599开头银行卡</td> " + "</tr> "
			+ "<tr>" + "<td>中国银行</td> " + "<td>单笔单日1万</td> "
			+ "<td>需开通银联无卡支付业务</td> " + "</tr> " + "</table>" + "</div>";
	$.blockUI(con);
}
// 发送验证码时添加cookie
function addCookie(name, value, expiresHours) {
	var cookieString = name + "=" + escape(value);
	// 判断是否设置过期时间,0代表关闭浏览器时失效
	if (expiresHours > 0) {
		var date = new Date();
		date.setTime(date.getTime() + expiresHours * 1000);
		cookieString = cookieString + ";expires=" + date.toUTCString();
	}
	document.cookie = cookieString;
}
// 修改cookie的值
function editCookie(name, value, expiresHours) {
	var cookieString = name + "=" + escape(value);
	if (expiresHours > 0) {
		var date = new Date();
		date.setTime(date.getTime() + expiresHours * 1000); // 单位是毫秒
		cookieString = cookieString + ";expires=" + date.toGMTString();
	}
	document.cookie = cookieString;
}
// 根据名字获取cookie的值
function getCookieValue(name) {
	var strCookie = document.cookie;
	var arrCookie = strCookie.split("; ");
	for (var i = 0; i < arrCookie.length; i++) {
		var arr = arrCookie[i].split("=");
		if (arr[0] == name) {
			return unescape(arr[1]);
			break;
		} else {
			continue;
		}
		if ((i + 1) == arrCookie.length) {
			return "";
			break;
		}
	}
}
/****************NEW JS AREA******************/
function loginCheck(p){
	if(p == '0'){
		if ($("#user-account").val().trim() == '') {
			$(".box_userName_tip").html("用户名不能为空");
			$("#user-account").siblings("i").attr("class","box_check_icon check_wrong");
			loginUserAccountStatus=false;
			return;
		}else{
			//phone
			if(phone_reg.test($("#user-account").val().trim())){
				$.post($.rootPath()+"user/validateMobile.do",{"tel":$("#user-account").val().trim()},function(result){
					if(result=='fail'){
						$("#user-account").siblings("i").attr("class","box_check_icon check_right");
						$(".box_userName_tip").html("");
						loginUserAccountStatus=true;
						return;
					}else {
						$(".box_userName_tip").html("用户名不存在！");
						$("#user-account").siblings("i").attr("class","box_check_icon check_wrong");
						loginUserAccountStatus=false;
						return;
					}
				});
			}
			//email
			if(email_reg.test($("#user-account").val().trim())){
				$.post($.rootPath()+"user/checkEmail.do",{"email":$("#user-account").val().trim()},function(result){
					if(result==false){
						$(".box_userName_tip").html("用户名不存在！");
						$("#user-account").siblings("i").attr("class","box_check_icon check_wrong");
						loginUserAccountStatus=false;
						return;
					}else {
						$("#user-account").siblings("i").attr("class","box_check_icon check_right");
						$(".box_userName_tip").html("");
						loginUserAccountStatus=true;
						return;
					}
				});
			}
		}
	}
	if (p == '1') {
		if ($("#user-password").val().trim() == '') {
			$(".box_loginpassword_tip").html("密码不能为空！");
			$("#user-password").siblings("i").attr("class","box_check_icon check_wrong");
			loginPasswordStatus=false;
			return;
		}else{
			$(".box_loginpassword_tip").html("");
			$("#user-password").siblings("i").attr("class","box_check_icon check_right");
			loginPasswordStatus=true;
			return;
		}
	}
	if (p == '2') {
		if ($("#user-verifyword").val().trim() == '') {
			$(".box_loginverifycode_tip").html("验证码不能为空");
			$("#user-verifyword").siblings("i").attr("class","box_check_icon check_wrong");
			loginVerifyStatus=false;
			return;
		}
	}
}
function changBankTeslStatus(){
	tel_remark=$("input[name=phoneatt]").val();
	$("input[name=phoneatt]").removeAttr("readonly").removeAttr("style").attr("onblur","checkTel(this.value)");
}
function queryString(key){
    var regex_str = "^.+\\?.*?\\b"+ key +"=(.*?)(?:(?=&)|$|#)";
    var regex = new RegExp(regex_str,"i");
    var url = window.location.toString();
    if(regex.test(url)) return RegExp.$1;
    return undefined;
}
function tologin() {
	$("#login_tologin").attr("class", "login_tologing");
	$("#login_tologin").html("\u6b63\u5728\u767b\u5f55");
	var data = {};
	var type = 0;
	if (type == 0) {
		data.useraccount = $("#user-account").val();
		data.password =$("#user-password").val();
	} else if (type == 1) {
		data.userphone = $("").val();
	}
	data.loginType = type;
	data.autologin = $("#checkbox1").val();
	data.captcha = $("#user-verifyword").val();
	var url = $.rootPath()+"\u0075\u0073\u0065\u0072\u002f\u006c\u006f\u0067\u0069\u006e\u002e\u0064\u006f";
	$.post(url, data, function(result) {
		if (result.success == 'yes') {
			$.session.set('userAccount', result.userAccount);
			$.session.set('userId', result.userId);
			$.session.set('useableMoney', result.useableMoney);
			$.session.set('totalMoney', result.totalMoney);
			$.session.set('totalCaculus', result.totalCaculus);
			$.session.set('inviteCode', result.inviteCode);
			// 直接用sessionStorage，因为有的页面并没有引用jQuerysessoin.js
			if (window.sessionStorage) {
				window.sessionStorage.setItem('js/js/userLevel', result.js/js/userLevel);
			}
			alertAgree(result);
			$.post($.rootPath()+"userlv/updateLevel.do",data,function(result){});
			try{
				var returnUrl=queryString("returnUrl");
			 	var RegUrl = new RegExp();  
			    var backUrl=unescape(returnUrl);
			    var t=(backUrl != undefined);
			    var r=(backUrl != "undefined");
				if(backUrl != undefined && backUrl != "undefined"){
					// location.href=$.rootPath()+unescape(returnUrl);
					location.href=unescape(returnUrl);
				}else{
					location.href = $.rootPath() + "index.html";
				}
			}catch (e){
				console.warn("returnUrl exception："+e);
			}
		} else {
			$("#login_tologin").attr("class", "login_tologin");
			$("#login_tologin").html("\u7acb\u5373\u767b\u5f55");
			if(result.errorCode == "2"){
				$(".box_loginverifycode_tip").html(result.errormsg);
				$("#user-verifyword").siblings("i").attr("class","box_check_icon check_wrong");
			}else if(result.errorCode == "1"){
				$(".box_loginpassword_tip").html(result.errormsg);
				$("#user-password").siblings("i").attr("class","box_check_icon check_wrong");
				$(".box_loginverifycode_tip").html("\u8bf7\u91cd\u65b0\u8f93\u5165\u9a8c\u8bc1\u7801\uff01");
				$("#user-verifyword").siblings("i").attr("class","box_check_icon check_wrong");
			}
			loginPasswordStatus=false;
			getVerifyWord();
		}
	});
}
function alertAgree(s) {
	$.ajax({
		url : $.rootPath() + "nccbank/getUserMove.html",
		dataType : "",
		async : false,
		success : function(result) {
			if (result.msgCode == '0') {
				location.href = $.rootPath() + "index.html";
			} else {
				agreement();
			}
		}
	});
}
function agree() {
	$.ajax({
		url : $.rootPath() + "nccbank/userAgree.html",
		dataType : "",
		async : false,
		success : function(result) {
			location.href = $.rootPath() + "index.html";
		}
	});
}
function disagree(){$.post($.rootPath()+"user/logout.do",function(w){window.location = $.rootPath()+"user/login.html";});}function updateAutoLogin(){var status=$("#checkbox1").data("status");if (status == '0') {$("#checkbox1").attr("data-status","1");$("#checkbox1").val("yes");}else{$("#checkbox1").attr("data-status","0");$("#checkbox1").val("no");}}function queryString(key){var regex_str = "^.+\\?.*?\\b"+ key +"=(.*?)(?:(?=&)|$|#)";var regex = new RegExp(regex_str,"i");var url = window.location.toString();if(regex.test(url)) return RegExp.$1;return undefined;}document.onkeydown = function(event) {e = event ? event : (window.event ? window.event : null);if (e.keyCode == 13) {tologin();}};
