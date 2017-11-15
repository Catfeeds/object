var banks = ["中国邮政储蓄银行", "中国工商银行", "中国光大银行", "中国建设银行", "中国民生银行", "中国农业银行", "平安银行", "兴业银行", "中国银行", "中信银行"];
var bankStatus = false;
var bankSelectStatus = false;
var bankNo = 0;
var bankRemark = "";
var bankName = "";
var bankInfo = "";
function showOrHid(para) {
    bankStrr();
}
function bankStrr() {
    var contant = "<table class=\"bank_header\" style=\"margin: 0px 12px;\" border=\"1\" style=\"border-collapse:collapse;\">";
    contant += "	<tr style=\"line-height:30px;color:#7c7c7c\"><td colspan=\"4\">请选择您要绑定银行卡的开户行：</td></tr>";
    contant += "	<tr style=\"margin:5px 0px;\">";
    contant += "		<td onclick=\"addValue(5);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li5 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/bank-15.png\"></td> ";
    contant += "		<td onclick=\"addValue(10);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li10 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/user/bank-110.png\"></td> ";
    contant += "		<td onclick=\"addValue(2);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li2 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/user/bank-12.png\"></td> ";
    contant += "		<td onclick=\"addValue(3);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li3 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/user/bank-13.png\"></td> ";
    contant += "	</tr>";
    contant += "	<tr style=\"margin:5px 0px;\">";
    contant += "		<td onclick=\"addValue(6);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li6 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/user/bank-16.png\"></td> ";
    contant += "		<td onclick=\"addValue(8);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li8 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/user/bank-18.png\"></td> ";
    contant += "		<td onclick=\"addValue(9);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li9 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/user/bank-19.png\"></td> ";
    contant += "		<td onclick=\"addValue(4);\" style=\"margin:5px 5px;\"><img class=\"bankIcon\" style=\"cursor:pointer\" id=li4 width=\"151px\" height=\"41px\" src=\"" + $.rootPath() + "images/user/bank-14.png\"></td> ";
    contant += "	</tr>";
    contant += "	<tr style=\"line-height:30px;color:#7c7c7c\">";
    contant += "		<td colspan=\"4\">推荐使用民生银行、中信银行</td>";
    contant += "	</tr>";
    contant += "</table>";
    if (bankNo == 0) {
        contant += "<div style = 'margin: 20px 0px;color:#7c7c7c;' id='bankmsg'></div>";
    } else {
        contant += "<div style = 'margin: 20px 0px;color:#7c7c7c;' id='bankmsg'><span style = 'color:#e43448;margin-left:20px;font-weight:bold;font-size:13px;'>温馨提示:</span><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1、此发卡行的充值、提现限额为&nbsp;<span style=\"text-decoration:underline;font-weight:bold;font-size:16px;\" id=\"xianed\">" + bankRemark + "</span>" + bankInfo + "</div>";
    }
    contant += "<div style=\"wdith:100%;height:30px;\">" +
        "<a href=\"javascript:queding();\" style=\"display:block;float:left;width:70px;height:30px;background-color:#e43448;color:#ffffff;line-height:30px;text-align:center;margin:0px 0px 0px 245px;\">确定</a>" +
        "<a href=\"javascript:close();\" style=\"display:block;float:left;width:60px;height:30px;color:#7c7c7c;line-height:30px;text-align:center;margin:0px 0px 0px 35px;\">关闭</a>" +
        "</div>";
    $.dialogCommon("银行列表", contant);
    if (bankNo != 0) {
        $("#li" + bankNo).attr("src", $.rootPath() + "images/user/bank-2" + bankNo + ".png");
    }
}
function queding() {
    if (bankNo != 0) {
        bankStatus = true;
        $(".selectBankk").html("<img src=\"" + $.rootPath() + "images/user/bank-3" + bankNo + ".png\" style=\"float:left;\"><span id=\"selectBankTip\" style=\"float:left;margin-left:10px;\" onclick=\"showOrHid();\">更改其他</span>");
        $(".box_smsverifycode_tip").html("充值、提现限额为" + bankRemark);
        $("#box_bankName_input").val(banks[bankNo - 1]);
        $("#box_bankName_input").attr("readonly", "readonly");
        $("#box_bankName_input").siblings("label").html("");
        $(".msg_verifycode").html("更换其它银行");
        $("#bankCardsNo").siblings("label").html("请输入&nbsp;<b>" + banks[bankNo - 1] + "借记卡</b>&nbsp;号");
        $("#bankCardsNo").siblings("label").bind("click", function () {
            var label = $(this);
            var inputObj = $(this).siblings("input[class=box_input1]");
            label.hide();
        });
        $("#bankCardsNo").removeAttr("disabled");
    }
    //区分我的账户页面和注册页面关闭事件
    if ($("#thd_view_type") != null && $("#thd_view_type") != undefined) {
        if ($("#thd_view_type").val() == "acct") {
            //$("#mb_con_info").remove("div");
        	$("#dialog-container").css({
    			"top": "0",
    		});
        	setTimeout(function() {
				$("#dialog-shade").remove("div")
			}, 200)
        } else if ($("#thd_view_type").val() == "register") {
        	$("#dialog-container").css({
    			"top": "0",
    		});
    		setTimeout(function() {
    			$("#dialog-full").hide()
    		}, 150);
    		setTimeout(function() {
				$("#dialog-shade").remove("div")
			}, 200)
        }
    } else {
		$("#dialog-container").css({
			"top": "0",
		});
		setTimeout(function() {
			$("#dialog-full").hide()
		}, 150);
		setTimeout(function() {
			$("#dialog-shade").remove("div")
		}, 200)
    }
}
function close() {
    if (!bankStatus) {
        $(".selectBankk").html("");
        $(".selectBankk").html("<span id=\"selectBankTip\" onclick=\"showOrHid();\">请先单击此项，选择您的开户行</span>");
        $(".banktip").html("");
        bankNo = 0;
    }
    //区分我的账户页面和注册页面关闭事件
    if ($("#thd_view_type") != null && $("#thd_view_type") != undefined) {
        if ($("#thd_view_type").val() == "acct") {
            //$("#mb_con_info").remove("div");
        	$("#dialog-container").css({
    			"top": "0",
    		});
        	setTimeout(function() {
				$("#dialog-shade").remove("div")
			}, 200)
        } else if ($("#thd_view_type").val() == "register") {
        	$("#dialog-container").css({
    			"top": "0",
    		});
    		setTimeout(function() {
    			$("#dialog-full").hide()
    		}, 150);
    		setTimeout(function() {
				$("#dialog-shade").remove("div")
			}, 200)
        }
    } else {
		$("#dialog-container").css({
			"top": "0",
		});
		setTimeout(function() {
			$("#dialog-full").hide()
		}, 150);
		setTimeout(function() {
			$("#dialog-shade").remove("div")
		}, 200)
    }
}
function addValue(arg) {
    /*
     * 图标编号对应银行：
     * 	{1:中国邮政银行,2:中国工商银行,3:中国光大银行,4:中国建设银行,5:中国民生,6:中国农业,7:平安银行,8:兴业银行,9:中国银行,10:中信银行}
     * */
    if (bankNo == 0) {
        $("#mb_con_info").css({"top": "160px"});
    }
    bankNo = arg;
    bankInfo = "";
    var count = document.getElementsByClassName("bankIcon");
    for (var i = 1; i <= 10; i++) {
        if (i == arg) {
            $("#li" + arg).attr("src", $.rootPath() + "images/user/bank-2" + arg + ".png");
        } else {
            $("#li" + i).attr("src", $.rootPath() + "images/user/bank-1" + i + ".png");
        }
    }
    if (arg == '1') {
        bankRemark = "单笔50万单日50万";
    } else if (arg == '2') {
        bankRemark = "单笔5万单日5万";
        bankInfo = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2、暂不支持9558开头的银行卡、卡号为18位的卡";
    } else if (arg == '3') {
        bankRemark = "单笔10万单日10万";
        bankInfo = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2、在光大银行柜面或个人网银开通电子支付功能";
    } else if (arg == '4') {
        bankRemark = "单笔10万单日10万";
        bankInfo = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2、在建设银行柜面或个人网银开通电子支付功能";
    } else if (arg == '5') {
        bankRemark = "单笔10万单日10万";
    } else if (arg == '6') {
        bankRemark = "单笔5万单日5万";
        bankInfo = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2、暂不支持95595、95596、95597、95598、95599开头银行卡";
    } else if (arg == '7') {
        bankRemark = "单笔50万单日50万";
        bankInfo = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2、需开通银联无卡支付业务";
    } else if (arg == '8') {
        bankRemark = "单笔5万单日5万";
    } else if (arg == '9') {
        bankRemark = "单笔1万单日1万";
        bankInfo = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2、需开通银联无卡支付业务";
    } else if (arg == '10') {
        bankRemark = "单笔10万单日10万";
    }
    bankName = banks[arg - 1];
    $("#bankmsg").html("<span style = 'color:#e43448;margin-left:20px;font-weight:bold;font-size:13px;'>温馨提示:</span><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1、此发卡行的充值、提现限额为&nbsp;<span style=\"text-decoration:underline;font-weight:bold;font-size:16px;\" id=\"xianed\">" + bankRemark + "</span>" + bankInfo + "");
    $("#xiane").html(bankRemark);
    $("#xianed").html(bankRemark);
}