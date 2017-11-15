<?php
if($_SESSION['name']!=""){
header("Location:pay.php"); 
exit();
}
?><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-compatible" content="IE=edge,chrome=1" />
   <title>登陆_北京中泰企汇金融服务外包有限公司</title>
	
		<link rel="stylesheet" href="css/user.css" />
		<script type="text/javascript" src="js/jquery-1.8.3.js"></script>
		<script type="text/javascript" src="js/jQuerysession.js"></script>
		<script type="text/javascript" src="js/alert.js"></script>
		<script type="text/javascript" src="js/user.js"></script>
		<script type="text/javascript" src="js/md5.js"></script>
		<script type="text/javascript" src="js/jquery.rotate.min.js"></script>
	</head>
	<body id="login">
		<div id="top">
			<div class="center">
				<div class="left">
					<div class="logo">
						<a href="/"><img src="images/pay_newlogo.png" style="margin: 15px auto;"/><!-- <img src="images/slgen.png" /> --></a>
					</div>
				</div>
				<div class="right">
					<a href="/" class="my">返回首页</a>
					<!-- <a href="" class="app">APP下载</a> -->
				</div>
			</div>
		</div>
		<div id="middle">
			<div class="img">
				<div class="center">
					<div class="left">
						<div class="back">
							<ul>
								<li class="title">登&nbsp;&nbsp;录</li>
								 <?php IF($_GET['t']=='error'){echo "<span style='color:red'>用户名或密码错误！</span>";}else{echo "";}?>
								<form action="do.php" accept-charset="UTF-8" method="post">
		<input type="hidden" name="action" value="login" id="action"/>
								<li class="li1">
									<div class="input_box">
										<label class="box_title">请输入手机号</label>
										<input class="box_input1" type="text" name="j_username" />
										
									</div>
								</li>
								<li class="li2">
									<div class="input_box">
										<label class="box_title">请输入登录密码</label>
										<input class="box_input1" type="password"  name="password" />
									
									</div>
								</li>
						
							
								<li class="li5">
								<input type="submit" class="register" value="登录" id="login_btn" style=" padding:10px; padding-left:30px; padding-right:30px; font-family:微软雅黑; font-size:16px;"/>
								
								
								
								</li>
								
								</form>
								
								<li class="li6">
									<div><a href="reg.php"  style="font-size:16px; color:#FFFFFF; text-decoration:underline">没有账号，立即注册！</a></div>
								</li>
							</ul>
						</div>
					</div>
					<div class="right">
						<div class="right_top">
							
						</div>
						<!-- 猫 -->
						<div class="right_bottom"><i class="right_bottom"></i></div>
					</div>
				</div>
			</div>
		</div>
		<div id="foot">
			<div class="center">
				
				<div class="bottom">
					<ul>
						<li>版权所有:北京中泰企汇金融服务外包有限公司&nbsp;&nbsp;地址：北京市西城区西直门外大街甲143号1-5-4002室&nbsp;&nbsp;电话：010-68368590&nbsp;&nbsp;备案号：<a href="http://www.miitbeian.gov.cn" target="_blank"></a> </li>
					</ul>
					
				</div>
			</div>
		</div>
		
	</body>
</html>