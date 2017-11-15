var _widht = document.documentElement.clientWidth; //屏幕宽
var _height = document.documentElement.clientHeight; //屏幕高  默认 谷歌浏览器内核

var userAgent=window.navigator.userAgent.toLowerCase();//获取浏览器内核
if(userAgent.indexOf("firefox")>=1){
	_height = window.screen.height;//firefox浏览器内核
}else {
    var name=navigator.appName;
    if(name=="Microsoft Internet Explorer"){
    	_height = document.documentElement.clientHeight;
    }
}
(function($) {
	$.extend({
		alert: function(msg) {
			GenerateHtml("alert", msg);
			btnOk(); //alert只是弹出消息，因此没必要用到回调函数callback
			btnNo();
		},
		alertReload: function(msg,callback) {
			GeneratereloadHtml("confirm", msg);
			btnOk(callback); 
			//btnNo();
		},
		confirm: function(msg, callback) {
			GenerateHtml("confirm", msg);
			btnOk(callback);
			btnNo();
		},
		confirmWithBoth: function(msg,callback1, callback2) {
			GenerateHtml("confirm", msg);
			btnOk(callback1);
			btnOkWithBoth(callback2);
		},
		blockUI: function(msg){
			GenerateHtml("blockUI", msg);
			clickOn();
		},
		blockAgreeUI: function(msg){
			blockAgreeHtml(msg);
			clickOn();
		},
		blockIndexUI: function(msg){
			blockIndexHtml(msg);
			clickOn();
		},
		blockAdvUI: function(msg){
			blockAdvHtml(msg);
			clickOn();
		},
		alertConfirm: function(msg,callback) {
			GenerateHtml("alert", msg);
			btnOk(callback);
			btnNo();
		},
		blockCreatCreditUI:function(msg){
			blockCreatCreditHtml(msg);
		},
		blockInfoUI:function(title,msg){
			blockInfoHtml(title,msg);
			btnNo();
		},
		blockNewUI:function(msg,title){
			blockNewHtml(msg,title);
		},
	});
	var blockNewHtml=function(msg,title){
		var _html = "";
		_html += '<div id="mb_box"></div><div id="mb_con_info_acct" style=\"display:none;\"><span id="mb_tit" style= "text-align:left;">'+title+'</span><br>';
		_html += '<div style="font-size: 14px; padding: 0px 20px 20px 20px; line-height: 50px; height: auto; font-family: 微软雅黑;">' + msg + '</div></div>';
		$("body").append(_html);
		$("#mb_con_info_acct").dragCommonAlert();
		infoCssAcct();
	};
	var blockInfoHtml=function(title,msg){
		var _html = "";
			_html += '<div id="mb_box"></div><div id="mb_con_info"><span id="mb_tit" style= "position: relative;text-align:center;">'+title+
			'<a id="mb_ico" style="font-size: 22px; position: absolute; right: 3px; top: 1px; color: #f00; cursor: pointer;">x</a></span>';
			_html += '<div id="mb_con" style="font-size: 14px; padding: 0px 20px 20px 20px; height: auto; font-family: 微软雅黑;">' + msg + '</div></div>';
		$("body").append(_html);
		$("#mb_con_info").dragCommonAlert();
		infoCss();
	};
	var blockCreatCreditHtml=function(msg){
		var _html = "";
			_html += '<div id="mb_box"></div><div id="mb_con_CreatCredit"><span id="mb_tit" style= "text-align:center;">确认转让及协议</span><br>';
			_html += '<div style="font-size: 14px; padding: 0px 20px 20px 20px; line-height: 50px; height: 200px; font-family: 微软雅黑;">' + msg + '</div></div>';
		$("body").append(_html);
		$("#mb_con_CreatCredit").dragCommonAlert();
		creditCss();
	};
	var blockIndexHtml=function(msg){
		var _html = "";
			_html += '<div id="mb_box"></div><div id="mb_con"><span id="mb_tit" style= "text-align:center; " >关于江西银行版本存管系统域名切换的公告</span><br>';
			_html+='<span style= "text-align:right; float: right; margin-right: 30px; font-size: 16px;font-weight: bold;" >——手机APP、WAP同步更新上线</span>';
			_html += '<div   style="font-size: 16px; padding: 20px; line-height: 22px; height: 300px; font-family: 微软雅黑;">' + msg + '</div></div>';
		$("body").append(_html);
		$("#newmb_con").dragCommonAlert();
		advCss();
	};
	var blockAdvHtml=function(msg){
		var _html = "";
			_html += '<div id="mb_box"></div><div id="mb_con"><span id="mb_tit" style= "text-align:center; " >关于元宝365接入江西银行资金存管系统的公告</span>';
			_html += '<div id="mb_msg">' + msg + '</div></div>';
		$("body").append(_html);
		$("#newmb_con").dragCommonAlert();
		advCss();
	};
		
	var blockAgreeHtml=function(msg){
		var _html = "";
			_html += '<div id="mb_box"></div><div id="mb_con"><span id="mb_tit">用户协议</span>';
			_html += '<div id="mb_msg">' + msg + '</div><div id="mb_btnbox">';
			_html += '</div></div>';
		$("body").append(_html);
		$("#newmb_con").dragCommonAlert();
		agreeCss();
		}
		//生成Html
	var GeneratereloadHtml = function(type, msg) {
		var _html = "";
		
		if(type=="blockUI"){
			_html += '<div id="mb_box"></div><div id="newmb_con"><span id="mb"></span>'+msg+'</div>';
		}else{
			_html += '<div id="mb_box"></div><div id="mb_con"><span id="mb_tit">温馨提示</span>';
			_html += '<a id="mb_ico">x</a><div id="mb_msg">' + msg + '</div><div id="mb_btnbox">';
			if (type == "alert") {
				_html += '<input id="mb_btn_ok" type="button" value="确定" />';
			}
			if (type == "confirm") {
				_html += '<input id="mb_btn_ok" type="button" value="确定" />';
				
			}
			_html += '</div></div>';
		}
		//必须先将_html添加到body，再设置Css样式
		$("body").append(_html);
		$("#mb_con").dragCommonAlert();
		$("#newmb_con").dragCommonAlert();
		GenerateCss();
	};
	
	//生成Html
	var GenerateHtml = function(type, msg) {
		var _html = "";
		
		if(type=="blockUI"){
			_html += '<div id="mb_box"></div><div id="newmb_con"><span id="mb"></span><a id=\"mb_ico\">x</a>'+msg+'</div>';
		}else{
			_html += '<div id="mb_box"></div><div id="mb_con"><span id="mb_tit">温馨提示</span>';
			_html += '<a id="mb_ico">x</a><div id="mb_msg">' + msg + '</div><div id="mb_btnbox">';
			if (type == "alert") {
				_html += '<input id="mb_btn_ok" type="button" value="确定" />';
			}
			if (type == "confirm") {
				_html += '<input id="mb_btn_ok" type="button" value="确定" />';
				_html += '<input id="mb_btn_no" type="button" value="取消" />';
			}
			_html += '</div></div>';
		}
		//必须先将_html添加到body，再设置Css样式
		$("body").append(_html);
		$("#mb_con").dragCommonAlert();
		$("#newmb_con").dragCommonAlert();
		$("#mb_con_CreatCredit").dragCommonAlert();
		GenerateCss();
	};
//生成Css
	var GenerateCss = function() {
		
		$("#newmb_con").css({
			zIndex: '999999',
			position: 'fixed'
		});
		$("#mb_box").css({
			width: '100%',
			height: '100%',
			zIndex: '99999',
			position: 'fixed',
			filter: 'Alpha(opacity=60)',
			backgroundColor: 'black',
			top: '0',
			left: '0',
			opacity: '0.6'
		});
		
		$("#mb").css({
			  display:'block',
			  height: '40px',
			  position: 'absolute',
			  width: '100%',
			  cursor: 'move'
		});
		
		$("#mb_con").css({
			zIndex: '999999',
			width: '320px',
			position: 'fixed',
			backgroundColor: 'White',
			borderRadius: '9px'
		});
		$("#mb_tit").css({
			display: 'block',
			fontSize: '14px',
			color: '#444',
			padding: '10px 15px',
			backgroundColor: '#F5F5F5',
			borderRadius: '9px 9px 0 0',
			borderBottom: '3px solid #E43448',
			fontWeight: 'bold',
			letterSpacing: '0.1em',
			cursor:'move'
		});
		$("#mb_msg").css({
			padding: '20px',
			lineHeight: '22px',
			fontFamily: '微软雅黑',
			borderBottom: '1px dashed #DDD',
			fontSize: '13px'
		});
		$("#mb_ico").css({
			display: 'block',
			position: 'absolute',
			right: '10px',
			top: '9px',
			border: '1px solid Gray',
			width: '18px',
			height: '18px',
			textAlign: 'center',
			lineHeight: '16px',
			cursor: 'pointer',
			borderRadius: '12px',
			fontFamily: '微软雅黑'
		});
		$("#mb_btnbox").css({
			margin: '6px 0 6px 0',
			textAlign: 'right',
			marginRight: '9px',
			height: '33px'
		});
		$("#mb_btn_ok,#mb_btn_no").css({
			width: '72px',
			height: '30px',
			color: 'black',
			fontFamily: '微软雅黑',
			letterSpacing: '0.2em',
			lineHeight: '14px',
			backgroundColor: '#fff',
			border: '1px solid lightgray',
			boxShadow: '0 0 6px 0 lightgray',
			borderRadius: '11px',
			cursor: 'pointer'
		});
		$("#mb_btn_ok").hover(function(){
			$(this).css({
				color: '#ffffff',
				backgroundColor: '#e43448'
			});
		},function(){
			$(this).css({
				color: 'black',
				backgroundColor: '#ffffff'
			});
		});
		$("#mb_btn_no").hover(function(){
			$(this).css({
				backgroundColor: '#bcbcbc'
			});
		},function(){
			$(this).css({
				backgroundColor: '#ffffff'
			});
		});
		$("#mb_btn_ok").css({
		});
		$("#mb_btn_no").css({
			marginLeft: '18px'
		});
		//右上角关闭按钮hover样式
		$("#mb_ico").hover(function() {
			$(this).css({
				backgroundColor: 'Red',
				color: 'White'
			});
		}, function() {
			$(this).css({
				backgroundColor: '#F5F5F5',
				color: 'black'
			});
		});
		var boxWidth = $("#mb_con").width();
		var boxHeight = $("#mb_con").height();
		//让提示框居中
		$("#mb_con").css({
			top: (_height - boxHeight) / 2.5 + "px",
			left: (_widht - boxWidth) / 2 + "px"
		});
		
		var newboxWidth = $("#newmb_con").width();
		var newboxHeight = $("#newmb_con").height();
		//让提示框居中
		$("#newmb_con").css({
			top: (_height - newboxHeight) / 2 + "px",
			left: (_widht - newboxWidth) / 2 + "px"
		});
	};
	var advCss = function() {
		
		$("#newmb_con").css({
			zIndex: '999999',
			position: 'fixed'
		});
		$("#mb_box").css({
			width: '100%',
			height: '100%',
			zIndex: '99999',
			position: 'fixed',
			filter: 'Alpha(opacity=60)',
			backgroundColor: 'black',
			top: '0',
			left: '0',
			opacity: '0.6'
		});
		
		$("#mb").css({
			  display:'block',
			  height: '40px',
			  position: 'absolute',
			  width: '100%',
			  cursor: 'move'
		});
		
		$("#mb_con").css({
			zIndex: '999999',
			width: '1100px',
			position: 'fixed',
			backgroundColor: 'White',
			borderRadius: '9px'
		});
		$("#mb_tit").css({
			display: 'block',
			fontSize: '18px',
			color: '#444',
			padding: '10px 15px',
			backgroundColor: '#F5F5F5',
			borderRadius: '9px 9px 0 0',
			borderBottom: '3px solid #E43448',
			fontWeight: 'bold',
			letterSpacing: '0.1em',
			cursor:'move'
		});
		$("#mb_msg").css({
			padding: '20px',
			lineHeight: '22px',
			height: '30px',
			fontFamily: '微软雅黑',
		
			fontSize: '13px',
			height:'300px'
		});
		$("#mb_btnbox").css({
			margin: '6px 0 6px 0',
			textAlign: 'right',
			marginRight: '9px',
			height: '40px'
		});
		var boxWidth = $("#mb_con").width();
		var boxHeight = $("#mb_con").height();
		//让提示框居中
		$("#mb_con").css({
			top: (_height - boxHeight) / 2.5 + "px",
			left: (_widht - boxWidth) / 2 + "px"
		});
		
		var newboxWidth = $("#newmb_con").width();
		var newboxHeight = $("#newmb_con").height();
		//让提示框居中
		$("#newmb_con").css({
			top: (_height - newboxHeight) / 2 + "px",
			left: (_widht - newboxWidth) / 2 + "px"
		});
	};
	var agreeCss = function() {
		
		$("#newmb_con").css({
			zIndex: '999999',
			position: 'fixed'
		});
		$("#mb_box").css({
			width: '100%',
			height: '100%',
			zIndex: '99999',
			position: 'fixed',
			filter: 'Alpha(opacity=60)',
			backgroundColor: 'black',
			top: '0',
			left: '0',
			opacity: '0.6'
		});
		
		$("#mb").css({
			  display:'block',
			  height: '40px',
			  position: 'absolute',
			  width: '100%',
			  cursor: 'move'
		});
		
		$("#mb_con").css({
			zIndex: '999999',
			width: '1100px',
			position: 'fixed',
			backgroundColor: 'White',
			borderRadius: '9px'
		});
		$("#mb_tit").css({
			display: 'block',
			fontSize: '14px',
			color: '#444',
			padding: '10px 15px',
			backgroundColor: '#F5F5F5',
			borderRadius: '9px 9px 0 0',
			borderBottom: '3px solid #E43448',
			fontWeight: 'bold',
			letterSpacing: '0.1em',
			cursor:'move'
		});
		$("#mb_msg").css({
			padding: '20px',
			lineHeight: '22px',
			height: '30px',
			fontFamily: '微软雅黑',
			borderBottom: '1px dashed #DDD',
			fontSize: '13px',
			height:'300px'
		});
		$("#mb_btnbox").css({
			margin: '6px 0 6px 0',
			textAlign: 'right',
			marginRight: '9px',
			height: '40px'
		});
		var boxWidth = $("#mb_con").width();
		var boxHeight = $("#mb_con").height();
		//让提示框居中
		$("#mb_con").css({
			top: (_height - boxHeight) / 2.5 + "px",
			left: (_widht - boxWidth) / 2 + "px"
		});
		
		var newboxWidth = $("#newmb_con").width();
		var newboxHeight = $("#newmb_con").height();
		//让提示框居中
		$("#newmb_con").css({
			top: (_height - newboxHeight) / 2 + "px",
			left: (_widht - newboxWidth) / 2 + "px"
		});
	};
        var creditCss = function() {
		$("#mb_con_CreatCredit").css({
			zIndex: '999999',
			position: 'fixed'
		});
		
		$("#mb_box").css({
			width: '100%',
			height: '100%',
			zIndex: '99999',
			position: 'fixed',
			filter: 'Alpha(opacity=60)',
			backgroundColor: 'black',
			top: '0',
			left: '0',
			opacity: '0.6'
		});
		
		$("#mb").css({
			  display:'block',
			  height: '40px',
			  position: 'absolute',
			  width: '100%',
			  cursor: 'move'
		});
		
		$("#mb_con_CreatCredit").css({
			zIndex: '999999',
			width: '660px',
			position: 'fixed',
			backgroundColor: 'White'
		});
		$("#mb_tit").css({
			display: 'block',
			fontSize: '18px',
			color: '#444',
			padding: '10px 15px',
			backgroundColor: '#F5F5F5',
			borderRadius: '9px 9px 0 0',
			borderBottom: '3px solid #E43448',
			fontWeight: 'bold',
			letterSpacing: '0.1em',
			cursor:'move'
		});
		$("#mb_btnbox").css({
			margin: '6px 0 6px 0',
			textAlign: 'right',
			marginRight: '9px',
			height: '40px'
		});
		
		var boxWidth = $("#mb_con_CreatCredit").width();
		var boxHeight = $("#mb_con_CreatCredit").height();
		//让提示框居中
		$("#mb_con_CreatCredit").css({
			top: (_height - boxHeight) / 2 + "px",
			left: (_widht - boxWidth) / 2 + "px"
		});
		
		//点击事件
		var clickSub = function(){
			$("#mb_box").remove("div");
			$("#mb_con_CreatCredit").remove("div");
		};
		var clickBack = function(){
			
		};
	};
	var infoCss = function() {
		$("#mb_box").css({
			width: '100%',
			height: '100%',
			zIndex: '99999',
			position: 'fixed',
			filter: 'Alpha(opacity=60)',
			backgroundColor: 'black',
			top: '0',
			left: '0',
			opacity: '0.6'
		});
		
		$("#mb").css({
			  display:'block',
			  height: '40px',
			  position: 'absolute',
			  width: '100%',
			  cursor: 'move'
		});
		
		$("#mb_con_info").css({
			zIndex: '999999',
			width: 'auto',
			position: 'fixed',
			backgroundColor: '#ffffff',
			borderRadius: '13px'
		});
		$("#mb_tit").css({
			display: 'block',
			fontSize: '16px',
			color: '#444',
			padding: '10px 15px',
			backgroundColor: '#F5F5F5',
			borderRadius: '9px 9px 0 0',
			borderBottom: '3px solid #E43448',
			fontWeight: 'bold',
			letterSpacing: '0.1em',
			cursor:'move'
		});
		$("#mb_btnbox").css({
			margin: '6px 0 6px 0',
			textAlign: 'right',
			marginRight: '9px',
			height: '40px'
		});
		
		var boxWidth = $("#mb_con_info").width();
		var boxHeight = $("#mb_con_info").height();
		//让提示框居中
		$("#mb_con_info").css({
			top: (_height - boxHeight) / 2 + "px",
			left: (_widht - boxWidth) / 2 + "px"
		});
		
		//点击事件
		var clickSub = function(){
			$("#mb_box").remove("div");
			$("#mb_con_info").remove("div");
		};
	};
	
	var infoCssAcct = function() {
		$("#mb_box").css({
			width: '100%',
			height: '100%',
			zIndex: '99999',
			position: 'fixed',
			filter: 'Alpha(opacity=60)',
			backgroundColor: 'black',
			top: '0',
			left: '0',
			opacity: '0.6'
		});
			
		$("#mb").css({
			  display:'block',
			  height: '40px',
			  position: 'absolute',
			  width: '100%',
			  cursor: 'move'
		});
		
		$("#mb_con_info_acct").css({
			zIndex: '999999',
			width: '840px',
			height:'583px',
			position: 'fixed',
			backgroundColor: '#ffffff'
		});
		$("#mb_tit").css({
			display: 'block',
			fontSize: '16px',
			color: '#444',
			padding: '10px 15px',
			backgroundColor: '#F5F5F5',
			borderRadius: '9px 9px 0 0',
			borderBottom: '3px solid #E43448',
			fontWeight: 'bold',
			letterSpacing: '0.1em',
			cursor:'move'
		});
		$("#mb_btnbox").css({
			margin: '6px 0 6px 0',
			textAlign: 'right',
			marginRight: '9px',
			height: '40px'
		});
		
		var boxWidth = $("#mb_con_info_acct").width();
		var boxHeight = $("#mb_con_info_acct").height();
		//让提示框居中
		$("#mb_con_info_acct").css({
			top: (_height - boxHeight) / 2 + "px",
			left: (_widht - boxWidth) / 2 + "px"
		});
		//点击事件
		var clickSub = function(){
			$("#mb_box").remove("div");
			$("#mb_con_info_acct").remove("div");
		};
	};
	//确定按钮事件
	var btnOk = function(callback) {
		$("#mb_btn_ok").click(function() {
			$("#mb_box,#mb_con").remove();
			if (typeof(callback) == 'function') {
				callback();
			}
		});
	};
	//确定按钮事件
	var btnOkWithBoth = function(callback) {
		$("#mb_btn_no").click(function() {
			$("#mb_box,#mb_con").remove();
			if (typeof(callback) == 'function') {
				callback();
			}
		});
	};
	//取消按钮事件
	var btnNo = function() {
		$("#mb_btn_no,#mb_ico").click(function() {
			$("#mb_box,#mb_con,#mb_con_info").remove();
		});
	};
	
	//点击事件
	var clickOn = function(){
		$("#mb_ico").click(function(){
			$("#mb_box,#newmb_con,#mb_con_CreatCredit").remove();
		});
	};
})(jQuery);
//拖拽功能 
$(function() { 
	$.fn.extend({
		dragCommonAlert:function(){
			var $tar = $(this); 
			return $(this).mousedown(function(e){
				if(e.target.tagName =="SPAN"){
					var diffX = e.clientX - $tar.offset().left; 
					//var diffY = e.clientY - $tar.offset().top; 
					$(document).mousemove(function(e){
						var left = e.clientX - diffX; 
						var top = e.clientY - 20; 
					if (left < 0){
						left = 0;
					}else if (left <= $(window).scrollLeft()){ 
						left = $(window).scrollLeft(); 
					}else if (left > $(window).width() +$(window).scrollLeft() - $tar.width()){ 
						left = $(window).width() +$(window).scrollLeft() -$tar.width(); 
					}
					
					/*if (top < 0){ 
						top = 0; 
					}else if (top <= $(window).scrollTop()){ 
						top = $(window).scrollTop(); 
					}else if (top > $(window).height() +$(window).scrollTop() - $tar.height()){ 
						top = $(window).height() +$(window).scrollTop() - $tar.height(); 
					}*/
					$tar.css("left",left + 'px').css("top",top + 'px'); 
					}); 
				} 
				$(document).mouseup(function(){
					$(this).unbind("mousemove"); 
					$(this).unbind("mouseup");
				}); 
			}); 
		} 
	}); 
}); 