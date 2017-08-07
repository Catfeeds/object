#!/bin/bash

# 脚本的日志文件
LOGFILE="/data/zabbix/logs/sms.log"
:>"$LOGFILE"
exec 1>"$LOGFILE"
exec 2>&1


MOBILE_NUMBER=$1		    ## 手机号码
MESSAGE_UTF8=$2                    # 短信内容
XXD="/usr/bin/xxd"
CURL="/usr/bin/curl"
TIMEOUT=5

# 短信内容要经过URL编码处理，除了下面这种方法，也可以用curl的--data-urlencode选项实现。
MESSAGE_ENCODE=$(echo "$MESSAGE_UTF8" | ${XXD} -ps | sed 's/\(..\)/%\1/g' | tr -d '\n')

# SMS API 接口用户名和秘钥
ACCOUNT="J02039"
PASS="289292"

#URL="http://222.73.117.158/msg/HttpBatchSendSM?account=${ACCOUNT}&pswd=${PASS}&mobile=${MOBILE_NUMBER}&msg=${MESSAGE_ENCODE}&needstatus=true"

URL="http://112.91.147.37:9003/MWGate/wmgw.asmx/MongateCsSpSendSmsNew?userId=${ACCOUNT}&password=${PASS}&iMobiCount=1&pszMobis=${MOBILE_NUMBER}&pszMsg=${MESSAGE_ENCODE}"

# Send it
set -x
${CURL} -s --connect-timeout ${TIMEOUT} "${URL}"
