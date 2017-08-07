server {
        listen                  80;
        #use "default" parameter to directive this is default when using
        #name based virtual hosts.
        #listen                  88   default;
        server_name              mgame.zuanliantech.com i0-game.zuanliantech.com ci0-game.zuanliantech.com i1-game.zuanliantech.com ci1-game.zuanliantech.com;
        #server_name              cms.diamond.hncae.net;
        #charset                 utf-8;
        gzip                     on;
        #gzip_types              text/html text/css text/js application/x-javascript  application/javascript text/javascript;
        gzip_types               *;
        #gzip_comp_level         3;
        #gzip_min_length         10000;
        #gzip_disable     "MSIE [1-6]\.";
        root                     /data/www/game.zuanliantech.com/ ;
	#expires		epoch ;
	sub_filter_once off;
        error_page      502  /502.html;


        #location ~* \.(?:ico|css|js|gif|jpe?g|png)$ {
        #        access_log              off;
        #        log_not_found           off;
        #        expires                 10m;
        #        }
        #error_page      404     /index.php ;
        #error_page      403     /index.html ;

        index  index.html index.htm index.php;
        access_log              logs/access.game.zuanliantech.com.log main;
        #access_log              off;
        log_not_found           off;

    location / {
	proxy_pass http://game-zuanliantech ;
        #for http version 1.1
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        #or
        #for http version 1.0
        #proxy_set_header Connection "Keep-Alive";
        #proxy_pass $scheme://$host$request_uri;
        #proxy_redirect http:// https:// ;
        #health_check interval=2 fails=1 passes=2  match=welcome;  #for commercial version
        include    proxy.conf ;
 

    }

    location ~ ^/admins {
	include auth.conf ;
        proxy_pass http://game-zuanliantech ;
        #for http version 1.1
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        #or
        #for http version 1.0
        #proxy_set_header Connection "Keep-Alive";
        #proxy_pass $scheme://$host$request_uri;
        #proxy_redirect http:// https:// ;
        #health_check interval=2 fails=1 passes=2  match=welcome;  #for commercial version
        include    proxy.conf ;


    }
    location ~ ^/sidekiq {
	auth_basic	"Auth required" ;
        auth_basic_user_file  vhosts/sidekiq_pass ;
        proxy_pass http://game-zuanliantech ;
        #for http version 1.1
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        #or
        #for http version 1.0
        #proxy_set_header Connection "Keep-Alive";
        #proxy_pass $scheme://$host$request_uri;
        #proxy_redirect http:// https:// ;
        #health_check interval=2 fails=1 passes=2  match=welcome;  #for commercial version
        include    proxy.conf ;


    }

    location ~ (^/javascripts|^/stylesheets) {
        proxy_pass http://t3-ihaveu ;
        #for http version 1.1
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        #or
        #for http version 1.0
        #proxy_set_header Connection "Keep-Alive";
        #proxy_pass $scheme://$host$request_uri;
        #proxy_redirect http:// https:// ;
        #health_check interval=2 fails=1 passes=2  match=welcome;  #for commercial version
        include    proxy.conf ;
	sub_filter '优众'  '';
        proxy_set_header Host  t3.ihaveu.net ;
	sub_filter 't3.ihaveu.net' 'game.zuanliantech.com' ;
	proxy_set_header User-Agent "(iPhone; CPU iPhone OS 10_2_1 like Mac OS X) AppleWebKit/602.4.6 (KHTML, like Gecko) Version/10.0 Safari/602.1" ;


    }




    location ~ (^/categories|^/products|^/carts|^/brands|^/utilities) {
	# if ($http_user_agent !~* 'iPhone|iPad|Android') {
	#	return 403 ;
        #  }
        proxy_pass http://touch-ihaveu ;
        #for http version 1.1
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        #or
        #for http version 1.0
        #proxy_set_header Connection "Keep-Alive";
        #proxy_pass $scheme://$host$request_uri;
        #proxy_redirect http:// https:// ;
        #health_check interval=2 fails=1 passes=2  match=welcome;  #for commercial version
        #include    proxy.conf ;
	proxy_set_header User-Agent "(iPhone; CPU iPhone OS 10_2_1 like Mac OS X) AppleWebKit/602.4.6 (KHTML, like Gecko) Version/10.0 Safari/602.1" ;
	#proxy_redirect http://www.ihaveu.com/  /  ;
	proxy_set_header Host  touch.ihaveu.com ;
	proxy_pass_request_headers off;
	sub_filter 'https:' 'http:' ;
	sub_filter 'windows' 'windows2' ;
	sub_filter 'linux' 'linux2' ;
	sub_filter 'osx' 'osx2' ;
	sub_filter 't3.ihaveu.net' 'game.zuanliantech.com' ;
	sub_filter 'www.ihaveu.com' 'game.zuanliantech.com' ;
	sub_filter '优众'  '';
	sub_filter '<div class="app_tip hide" id="app">' '' ;
	sub_filter '<div class="app_del left" ><span class="sprites left"></span></div>' '' ;
	sub_filter '<span class="app_logo sprites"></span>' '' ;
	sub_filter '<span class="app_name">优众手机APP <br>购物更方便</span>' '' ;
	sub_filter '<div class="download_app right btn" data-href="http://itunes.apple.com/cn/app/you-zhong/id506299184?mt=8">下载</div>
    </div>' '' ;
	sub_filter '下载' '' ;

    }

   location /ecshop/ {
        proxy_pass http://appserver ;
        #for http version 1.1
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        #or
        #for http version 1.0
        #proxy_set_header Connection "Keep-Alive";
        #proxy_pass $scheme://$host$request_uri;
        #proxy_redirect http:// https:// ;
        #health_check interval=2 fails=1 passes=2  match=welcome;  #for commercial version
        include    proxy.conf ;
        #proxy_set_header User-Agent "(iPhone; CPU iPhone OS 10_2_1 like Mac OS X) AppleWebKit/602.4.6 (KHTML, like Gecko) Version/10.0 Safari/602.1" ;
        #proxy_redirect http://game.zuanliantech.com/test111/  /  ;
        #proxy_set_header Host  touch.ihaveu.com ;
        #proxy_pass_request_headers off;
    }


}
