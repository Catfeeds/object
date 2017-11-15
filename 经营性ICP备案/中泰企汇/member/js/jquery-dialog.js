//common
var SW = document.documentElement.clientWidth;
var SH = document.documentElement.clientHeight;
//common
//login
var hr = window.location.href;
var flag = false;//防止多次点击事件
//login
var userAgent = window.navigator.userAgent.toLowerCase();
if (userAgent.indexOf("firefox") >= 1) {
	SH = window.screen.height;
} else {
	var name = navigator.appName;
	if (name == "Microsoft Internet Explorer") {
		_1 = document.documentElement.clientHeight
	}
}(function($) {
	$("link").eq(0).after("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + $.rootPath() + "css/common/dialog-common.css\"/>");
	$("script").eq(0).before("<script type=\"text/javascript\" src=\"" + $.rootPath() + "js/jquery.easing.min.js\"></script>");
	$.extend({
		dialog: function(msg, btn) {
			var _0 = '<div id="dialog-shade"><div id="dialog-full"></div>';
			_0 += '<div id="dialog-container">';
			_0 += '	<div id="dialog-info">';
			_0 += '		<div id="dialog-info-title"><span id="title-span">温馨提示</span></div>';
			_0 += '		<div id="dialog-info-info" style="max-width: 350px;">' + msg + '</div>';
			_0 += '		<div id="dialog-info-btn">';
			if (btn != undefined && btn != null && btn.trim() != "") {
				_0 += '			<i id="dialog-submit">' + btn + '</i>'
			} else {
				_0 += '			<i id="dialog-submit">确定</i>'
			}
			_0 += '		</div>';
			_0 += '	</div>';
			_0 += '</div></div>';
			$("body").append(_0);
			$("#dialog-full").addClass("dialog-full");
			$("#dialog-container").addClass("dialog-container");
			$("#dialog-info").addClass("dialog-info");
			$("#dialog-info #dialog-info-title").addClass("dialog-info-title");
			$("#dialog-info #dialog-info-title #title-span").addClass("title-span");
			$("#dialog-info #dialog-info-info").addClass("dialog-info-info");
			$("#dialog-info #dialog-info-btn").addClass("dialog-info-btn");
			$("#dialog-info #dialog-submit").addClass("dialog-submit");
			$("#dialog-full").show();
			$("#dialog-container").css({
				"top": "50%",
				"margin-left": "-" + $("#dialog-container").width() / 2 + "px",
				"margin-top": "-" + $("#dialog-container").height() / 2 + "px",
			});
			$("#dialog-submit").click(function() {
				$("#dialog-container").css({
					"top": "0",
				});
				setTimeout(function() {
					$("#dialog-full").hide()
				}, 150);
				setTimeout(function() {
					$("#dialog-shade").remove("div")
				}, 200)
			})
		},
		dialogConfirm: function(msg, title2, even2, title1, even1) {
			var _0 = '<div id="dialog-shade"><div id="dialog-full"></div>';
			_0 += '<div id="dialog-container">';
			_0 += '	<div id="dialog-info">';
			_0 += '		<div id="dialog-info-title"><span id="title-span">温馨提示</span></div>';
			_0 += '		<div id="dialog-info-info" style="max-width: 350px;">' + msg + '</div>';
			_0 += '		<div id="dialog-info-btn">';
			if (title1 != undefined && title1 != null && title1.trim() != "") {
				_0 += '			<i id="dialog-cancle">' + title1 + '</i>'
			} else {
				_0 += '			<i id="dialog-cancle">取消</i>'
			}
			if (title2 != undefined && title2 != null && title2.trim() != "") {
				_0 += '			<i id="dialog-submit">' + title2 + '</i>'
			} else {
				_0 += '			<i id="dialog-submit">确定</i>'
			}
			_0 += '		</div>';
			_0 += '	</div>';
			_0 += '</div></div>';
			$("body").append(_0);
			$("#dialog-full").addClass("dialog-full");
			$("#dialog-container").addClass("dialog-container");
			$("#dialog-info").addClass("dialog-info");
			$("#dialog-info #dialog-info-title").addClass("dialog-info-title");
			$("#dialog-info #dialog-info-title #title-span").addClass("title-span");
			$("#dialog-info #dialog-info-info").addClass("dialog-info-info");
			$("#dialog-info #dialog-info-btn").addClass("dialog-info-btn");
			$("#dialog-info #dialog-submit").addClass("dialog-submit");
			$("#dialog-info #dialog-cancle").addClass("dialog-cancle");
			$("#dialog-full").show();
			$("#dialog-container").css({
				"top": "50%",
				"margin-left": "-" + $("#dialog-container").width() / 2 + "px",
				"margin-top": "-" + $("#dialog-container").height() / 2 + "px",
			});
			$("#dialog-submit").click(function() {
				if (typeof(even2) == 'function') {
					even2();
				}
				$("#dialog-container").css({
					"top": "0",
				});
				setTimeout(function() {
					$("#dialog-full").hide()
				}, 150);
				setTimeout(function() {
					$("#dialog-shade").remove("div")
				}, 200)
			});
			$("#dialog-cancle").click(function() {
				if (typeof(even1) == 'function') {
					even1();
				}				
				$("#dialog-container").css({
					"top": "0",
				});
				setTimeout(function() {
					$("#dialog-full").hide()
				}, 150);
				setTimeout(function() {
					$("#dialog-shade").remove("div")
				}, 200)
			})
		},
		dialogCommon: function(title, content) {
			var _0 = '<div id="dialog-shade"><div id="dialog-full"></div>';
			_0 += '<div id="dialog-container">';
			_0 += '	<div id="dialog-info">';
			_0 += '		<div id="dialog-info-title"><span id="title-span">' + title + '</span><span id="close-span"></span></div>';
			_0 += '		<div id="dialog-info-info">' + content + '</div>';
//			_0 += '		<div id="dialog-info-btn">';
//			_0 += '		</div>';
			_0 += '	</div>';
			_0 += '</div></div>';
			$("body").append(_0);
			$("#dialog-full").addClass("dialog-full");
			$("#dialog-container").addClass("dialog-container");
			$("#dialog-info").addClass("dialog-info");
			$("#dialog-info #dialog-info-title").addClass("dialog-info-title");
			$("#dialog-info #dialog-info-title #title-span").addClass("title-span");
			$("#dialog-info #dialog-info-title #close-span").addClass("close-span").css({"background":"url("+$.rootPath()+"images/common/icons.png) no-repeat -220px -315px"});
			$("#dialog-info #dialog-info-info").addClass("dialog-info-info");
			$("#dialog-info #dialog-info-btn").addClass("dialog-info-btn");
			$("#dialog-info #dialog-submit").addClass("dialog-submit");
			$("#dialog-full").show();
			$("#dialog-container").css({
				"top": "50%",
				"margin-left": "-" + $("#dialog-container").width() / 2 + "px",
				"margin-top": "-" + $("#dialog-container").height() / 2 + "px",
			});
			$("#close-span").click(function() {
				$("#dialog-container").css({
					"top": "0",
				});
				setTimeout(function() {
					$("#dialog-full").hide();
				}, 150);
				setTimeout(function() {
					$("#dialog-shade").remove("div");
				}, 200);
			});
		},
		login: function() {
			var _0 = '<div id="dialog-shade"><div id="dialog-full"></div>';
			_0 += '<div id="dialog-container">';
			_0 += '	<div id="dialog-info">';
			_0 += '		<div id="dialog-info-title"><span id="title-span">登录</span><span id="close-span"></span></div>';
			_0 += '			<div id="dialog-info-info">';
			_0 += '				<ul class="normalul">';
			_0 += '				<li class="loginJs_li2"> ';
			_0 += '					<div class="logindiv"> ';
			_0 += '						<!--<label></label>--> ';
			_0 += '						<span class="username"></span> ';
			_0 += '						<input type="text" value="" placeholder="请输入用户名/手机号/邮箱" id="user-account" autocomplete="off" onblur=""/>';
			_0 += '					</div> ';
			_0 += '				</li>';
			_0 += '				<li class="loginJs_li3"> ';
			_0 += '						<div class="logindiv"> ';
			_0 += '						<!--<label></label>--> ';
			_0 += '						<span class="password"></span> ';
			_0 += '						<input type="password" value="" placeholder="请输入密码" id="user-password" autocomplete="off"/>';
			_0 += '					</div> ';
			_0 += '				</li>';
			_0 += '				<li class="loginJs_li4"> ';
			_0 += '					<div class="verifywrod"> ';
			_0 += '						<div class="logindiv_verifywrod">';
			_0 += '							<!--<label></label>--> ';
			_0 += '							<span class="verifywrod"></span> ';
			_0 += '							<input type="text" value="" placeholder="请输入验证码" id="user-verifyword" autocomplete="off"/>';
			_0 += '						</div> ';
			_0 += '						<div class="img_verifywrod"> ';
			_0 += '							<span> ';
			_0 += '								<img style="width:100px; height:34px;padding-left:5px;" src="' + $.rootPath() + 'captcha.svl" valign="bottom" id="verify_code"/>';
			_0 += '							</span>';
			_0 += '							<a href="javascript:getVerifyWord();" data-url-verifyword="' + $.rootPath() + 'captcha.svl" id="verifyA">看不清，换一个</a>';
			_0 += '						</div> ';
			_0 += '					</div> ';
			_0 += '				</li>';
			_0 += '				<li class="loginJs_li5"> ';
			_0 += '					<div class="login_toremember"> ';
			_0 += '						<ul class="remember_ul"> ';
			_0 += '							<li class="u1">下次自动登录<i class="checked" data-status="1" id="autologin" onclick="updateAutoLogin()"></i>';
			_0 += '								<input type="hidden" id="checkbox1" value="yes"/>';
			_0 += '							</li>';
			_0 += '							<li class="u2"><a href="' + $.rootPath() + 'user/resetpwd.html">忘记密码</a></li> ';
			_0 += '						</ul>';
			_0 += '					</div> ';
			_0 += '				</li>';
			_0 += '				<li class="loginJs_li6"> ';
			_0 += '					<div class="login_tologin" id="login_tologin"> ';
			_0 += '						<a href="javascript:tologin();" id="toLogin">立即登录</a> ';
			_0 += '					</div> ';
			_0 += '				</li>';
			_0 += '				<li class="loginJs_li7"> ';
			_0 += '					<div class="login_toregister">没有帐号？';
			_0 += '						<a href="' + $.rootPath() + 'user/register.html">免费注册</a>';
			_0 += '					</div> ';
			_0 += '				</li>';
			_0 += '			</ul>';
			_0 += '			<div class=\"login_info\"></div>';
			_0 += '		</div>';
			/*_0 += '		<div id="dialog-info-btn">';
			_0 += '		</div>';*/
			_0 += '	</div>';
			_0 += '</div></div>';
			$("body").append(_0);
			$("#dialog-full").addClass("dialog-full");
			$("#dialog-container").addClass("dialog-container");
			$("#dialog-info").addClass("dialog-info");
			$("#dialog-info #dialog-info-title").addClass("dialog-info-title");
			$("#dialog-info #dialog-info-title #title-span").addClass("title-span");
			$("#dialog-info #dialog-info-title #close-span").addClass("close-span").css({"background":"url("+$.rootPath()+"images/common/icons.png) no-repeat -220px -315px"});
			$("#dialog-info #dialog-info-info").addClass("dialog-info-info");
			$("#dialog-info #dialog-info-btn").addClass("dialog-info-btn");
			$("#dialog-info #dialog-submit").addClass("dialog-submit");
			$(".login_type").css({
				width: '420px',
				height: '50px',
				lineHeight: '50px',
				fontSize: '14px',
				color: '#7C7C7C',
				textAlign: 'center'
			});
			$("#loginJsId ul").css({
				width : '350px',
				height : '320px',
				marginRight : 'auto',
				marginLeft : 'auto'
			});
			$("#loginJsId ul .loginJs_li2, .loginJs_li3, .loginJs_li4, .loginJs_li5, .loginJs_li6, .loginJs_li7, .loginJs_li8 ").css({
				width : '350px',
				height : '50px'
			});
			$(".loginJs_li2,.loginJs_li3,.loginJs_li4 ").css({
				paddingTop : '20px'
			});
			$(".logindiv ").css({
				border : '#D8D8D8 1px solid',
				width : '350px',
				height : '50px',
				lineHeight : '50px',
				backgroundColor : '#EEEEF0'
			});
			$(".username ").css({
				width : '22px',
				height : '22px',
				display : 'block',
				float : 'left',
				background : 'url('+$.rootPath()+'images/user/login_input_useraccount.png) no-repeat',
				margin : '14px 14px 0px 14px'
			});
			$(".logindiv input ").css({
				font : '14px "微软雅黑"',
				color : '#7C7C7C',
				padding : '15px 0px 15px 20px',
				width : '278px',
				float : 'left'
			});
			$(".password ").css({
				width : '22px',
				height : '22px',
				display : 'block',
				float : 'left',
				background : 'url('+$.rootPath()+'images/user/login_input_userpassword.png) no-repeat',
				margin : '14px 14px 0px 14px'
			});
			$(".logindiv_verifywrod ").css({
				border : '#D8D8D8 1px solid',
				width : '200px',
				height : '50px',
				lineHeight : '50px',
				backgroundColor : '#EEEEF0',
				float : 'left'
			});
			$(".logindiv_verifywrod .verifywrod ").css({
				width : '22px',
				height : '22px',
				display : 'block',
				float : 'left',
				background : 'url('+$.rootPath()+'images/user/login_input_userverifywrod.png) no-repeat',
				margin : '14px 14px 0px 14px'
			});
			$(".logindiv_verifywrod input ").css({
				font : '14px "微软雅黑"',
				color : '#7C7C7C',
				padding : '15px 0px 15px 20px',
				width : '128px',
				float : 'left'
			});
			$(".img_verifywrod ").css({
				width : '148px',
				height : '50px',
				float : 'left'
			});
			$(".img_verifywrod span ").css({
				width : '100px',
				height : '34px',
				display : 'block',
				float : 'left',
				border : '#D8D8D8 1px solid',
				marginLeft : '20px'
			});
			$(".img_verifywrod a ").css({
				float : 'left',
				height : '13px',
				lineHeight : '13px',
				fontSize : '12px',
				display : 'block',
				textDecoration : 'underline',
				color : '#B5B5B5',
				paddingTop : '3px',
				marginLeft : '20px'
			});
			$(".login_toremember ").css({
				margin : '0px'
			});
			$(".remember_ul ").css({
				width : '350px',
				height : '25px',
				marginTop : '20px'
			});
			$(".remember_ul li ").css({
				width : '175px',
				height : '25px',
				float : 'left',
				fontSize : '14px',
				color : '#7C7C7C'
			});
			$(".remember_ul .u1 ").css({
				height : '25px',
				lineHeight : '25px'
			});
			$(".checked ").css({
				backgroundImage : 'url('+$.rootPath()+'images/common/checkBox_checked.png)',
				backgroundRepeat : 'no-repeat'
			});
			$(".remember_ul i ").css({
				cursor : 'pointer',
				width : '26px',
				height : '25px',
				display : 'block',
				float : 'left',
				marginRight : '5px'
			});
			$(".remember_ul a ").css({
				color : '#E43448',
				marginLeft : '100px'
			});
			$(".login_tologin a ").css({
				width : '120px',
				height : '40px',
				display : 'block',
				border : '#E43448 2px solid',
				backgroundColor : '#FFFFFF',
				margin : '3px auto',
				lineHeight : '40px',
				color : '#E43448',
				textAlign : 'center'
			});
			$(".login_tologin a ").hover(function(){
				$(this).css({
					color: '#FFFFFF',
					backgroundColor: '#E43448',
					border: '#FFFFFF 2px solid'
				});
			},function(){
				$(this).css({
					border : '#E43448 2px solid',
					backgroundColor : '#FFFFFF',
					color : '#E43448'
				});
			});
			$(".login_toregister ").css({
				textAlign : 'center',
				color : '#7C7C7C',
				lineHeight : '50px'
			});
			$(".login_toregister a ").css({
				color : '#E43448'
			});
			$("#loginJsId").css({
				width : '420px',
				height : '100%',
				backgroundColor : '#FFFFFF',
				marginRight : 'auto',
				marginLeft : 'auto',
				borderRadius: '9px'
			});
			$(".login_info").css({
				  width: '350px',
				  height: '50px',
				  lineHeight: '50px',
				  color: '#E43448',
				  margin: '0px auto'
			});
			$("#dialog-full").show();
			$("#dialog-container").css({
				"top": "50%",
				"margin-left": "-" + $("#dialog-container").width() / 2 + "px",
				"margin-top": "-" + $("#dialog-container").height() / 2 + "px",
			});
			$("#close-span").click(function() {
				$("#dialog-container").css({
					"top": "0",
				});
				setTimeout(function() {
					$("#dialog-full").hide()
				}, 150);
				setTimeout(function() {
					$("#dialog-shade").remove("div")
				}, 200)
			})
		},
		register: function() {
		}
	})
})(jQuery);
function getVerifyWord(){
	var url=$("#verifyA").data("url-verifyword")+"?d="+new Date().getTime();
	$("#verify_code").attr("src",url);
}
function tologin(){
	if(!flag){
		flag = true;
		$("#login_tologin a").attr("style","margin:3px auto;line-height:40px;text-align:center;width:120px;height:40px;display:block;border: #EEEEF0 2px solid;background-color: #EEEEF0;color: #7C7C7C;");
		$("#login_tologin").children("a").html("正在登录");
		$("#login_tologin a").unbind('mouseenter').unbind('mouseleave');
		var data={};
		var type=0;
		if (type==0) {
			data.useraccount=$("#user-account").val();
			data.password =$("#user-password").val();
			/*data.password = hex_md5($("#user-password").val());*/
		}else if(type==1){
			data.userphone=$("").val();
		}
		data.loginType=type;
		data.autologin=$("#checkbox1").val();
		data.captcha=$("#user-verifyword").val();
		var url=$.rootPath()+"user/login.do";
		$.post(url,data,function(result){
			if(result.success == 'yes'){
				$.session.set('userAccount', result.userAccount);
				$.session.set('userId', result.userId);
				$.session.set('useableMoney', result.useableMoney);
				$.session.set('totalMoney', result.totalMoney);
				$.session.set('totalCaculus', result.totalCaculus);
				$.session.set('inviteCode', result.inviteCode);
				location.href=hr;
			}else{
				$(".login_info").html(result.msg);
				$("#login_tologin a").attr("style","margin:3px auto;line-height:40px;text-align:center;width:120px;height:40px;display:block;border: #E43448 2px solid;background-color: #fff;color: #e43448;");
				$("#login_tologin").children("a").html("立即登录");
				$(".login_tologin a ").hover(function(){
					$(this).css({
						color: '#FFFFFF',
						backgroundColor: '#E43448',
						border: '#FFFFFF 2px solid'
					});
				},function(){
					$(this).css({
						border : '#E43448 2px solid',
						backgroundColor : '#FFFFFF',
						color : '#E43448'
					});
				});
				getVerifyWord();
				flag = false;
			}
		});
	}
}
function updateAutoLogin(){
	var status=$("#checkbox1").val();
	if (status == 'no') {
		$("#autologin").attr("data-status","1");
		$("#checkbox1").val('yes');
		$("#autologin").attr('style','cursor: pointer;width: 26px;height: 25px;display: block;float: left;margin-right: 5px;background-image: url('+$.rootPath()+'images/common/checkBox_checked.png); background-repeat: no-repeat;');
	}else{
		$("#checkbox1").val('no');
		$("#autologin").attr('style','cursor: pointer;width: 26px;height: 25px;display: block;float: left;margin-right: 5px;background-image: url('+$.rootPath()+'images/common/checkBox_disabled_unchecked.png); background-repeat: no-repeat;');
		$("#autologin").attr("data-status","0");
	}
}
document.onkeydown = function(event) {
	e = event ? event : (window.event ? window.event : null);
	if (e.keyCode == 13) {
		tologin();
	}
};