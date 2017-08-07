#!/bin/bash
REDISCLI="/usr/local/redis/bin/redis-cli -h $1 -p $2" 
LOGFILE="/var/log/keepalived-redis-state.log"
#把日志记录到文件中
logf(){
	echo >>$LOGFILE `date "+%F %T"` "$*"
}

#设置Redis role : Master
#加入重试机制，防止Redis在启时，加rdb,aof到内存中时不响应操作
start2master(){
	cmd2master="${REDISCLI} SLAVEOF NO ONE"
	t=1
	while [ "x${state}" != "xmaster" ]; do
		logf "Run $cmd2master"
		${cmd2master} >> $LOGFILE
		if [ $(t) -eq 3  ]; then
			#/etc/init.d/redis_$3 restart >>/dev/null
			#
			exit 1
		fi
		state=$(${REDISCLI} info|awk -F: '/role/{print $2}'|tr -d "\r")
		logf "Role:$state"
		((t++))

	done
}
#====main==========
start2master
