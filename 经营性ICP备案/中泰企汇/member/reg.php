<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-compatible" content="IE=edge,chrome=1" />
		<title>注册_北京中泰企汇金融服务外包有限公司</title>
	
		<link rel="stylesheet" href="css/user.css" />
		<script type="text/javascript" src="js/jquery-1.8.3.js"></script>
		<script type="text/javascript" src="js/jQuerysession.js"></script>
		<script type="text/javascript" src="js/alert.js"></script>
		<script type="text/javascript" src="js/user.js"></script>
		<script type="text/javascript" src="js/md5.js"></script>
		<script type="text/javascript" src="js/jquery.rotate.min.js"></script>
	</head>
	<body id="register">
		<div id="top">
			<div class="center" >
				<div class="left">
					<div class="logo">
						<a href="/"><img src="images/pay_newlogo.png" style="margin: 15px auto;"/><!-- <img src="images/slgen.png" /> --></a>
					</div>
				</div>
				<!-- <div class="middle">
					<span></span>
				</div> -->
				<div class="right">
					<a href="/" class="my">返回首页</a>
					<!-- <a href="" class="app">APP下载</a> -->
				</div>
			</div>
		</div>
		<div id="middle">
			<div class="img">
			
			
			
			
			
			
			
			
			
				<input type="hidden" name="thd_view_type" id="thd_view_type" value="register"/>
				<input type="hidden" name="cloleId" id="cloleId" value="1">
				<div class="center">
					<i class="left_wallet"></i>
					<i class="right_licai"></i>
					<div class="input_box_area">
						<div class="left">
							<div class="back" style="display: block;" id="step1">
						
								<ul class="input_ul">
									<li class="title">
										<div class="title_left">注&nbsp;&nbsp;册</div>
									</li>
									  <?php IF($_GET['t']=='error'){echo "<span style='color:red'>请正确填写注册信息！</span>";}else{echo "";}?>
									
									
									
									
									
<form id="form" name="form" method="post" action="do.php" enctype="application/x-www-form-urlencoded">
<input type="hidden" name="form" value="form" />
<input type="hidden" name="action" value="reg" id="action"/>


	
									<li class="li1">
										<div class="input_box">
											<label class="box_title">请输入用户名</label>
											<input class="box_input1" type="text" name="nickName"/>
										</div>
									</li>
									<li class="li1">
										<div class="input_box">
											<label class="box_title">请输入手机号码</label>
											<input class="box_input1" type="text" maxlength="11" name="username"/>
										</div>
									</li>
									<li class="li1">
										<div class="input_box">
											<label class="box_title">请设置登录密码</label>
											<input class="box_input1" type="text" name="password"/>
										</div>
									</li>
									<li class="li1">
										<div class="input_box">
											<label class="box_title">确认登陆密码</label>
											<input class="box_input1" type="text" name="passworda"/>
										</div>
									</li>
						
										<input type="submit" class="register" value="注册" id="login_btn" style=" padding:10px; padding-left:30px; padding-right:30px; font-family:微软雅黑; font-size:16px;"/>
				</form>
									
									
					
					</div>
					</div>
					</div>
				</div>
			</div>
		</div>
		<!-- <div id="partner">
			<span></span>
				<i></i>
		</div> -->
		<div id="foot">
			<div class="center">
			  <div class="bottom">
					<ul>
						<li>版权所有:北京中泰企汇金融服务外包有限公司&nbsp;&nbsp;地址：北京市西城区西直门外大街甲143号1-5-4002室&nbsp;&nbsp;电话：010-68368590&nbsp;&nbsp;备案号：<a href="http://www.miitbeian.gov.cn" target="_blank">京ICP备17066303号</a> </li>
					</ul>
				
				</div>
			</div>
		</div>
	</body>
</html>