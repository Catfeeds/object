#!/bin/bash 
REDISCLI="/usr/local/redis/bin/redis-cli -h $1 -p $3" 
MHOST=$2
PORT=$3
LOGFILE="/var/log/keepalived-redis-state.log" 
flag="/var/keepalived/$3.master"
echo $flag
log(){
	logger "$@"
}
#把日志记录到文件中
logf(){
	echo >>$LOGFILE `date "+%F %T"` "$*"
}
get_flag(){
	if [ -e $flag ]; then
		rm -f $flag
		echo "0"
		return 0
	fi
		echo "1"
		return 1
		
}
start2slave(){
	cmd2slave="${REDISCLI} SLAVEOF $MHOST $PORT"
	t=1
	state=''
	while [ "x${state}" != "xslave" ]; do
		logf "Run $cmd2slave"
		${cmd2slave} >> $LOGFILE
		#if [ $(t) -eq 3 ]; then
		#	/etc/init.d/redis_$3 restart >>/dev/null
		#	[ $? -eq 0 ]&& msg="restart redis-server on $3 sucessed";${cmd2slave} >/dev/null|| msg="tried restart redis-server on $3 faild"
		#	logf "Promoting redis-server to MASTER on ${port} Faild 3 times, ${msg}"
		#fi
		state=$(${REDISCLI} info|awk -F':' '/role/{print $2}'|tr -d "\r")
		logf "Role:$state"
		((t++))
	done
}
#====main==========
st=$(get_flag)
if [ ${st} ]; then
	start2slave
fi
