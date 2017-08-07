#!/bin/bash 
REDISCLI="/usr/local/bin/redis-cli -a mNhsCbxDi9dGRxOq"
LOGFILE="/var/log/keepalived-redis-check.log" 

#state file
kpstate="/var/run/keepalived.$2.state"
#echo $kpstate
#ping 3 times avg: 2s-3s
ping_times=3

get_ping_gw(){
	gwip=$(/sbin/ip route|awk '/default/{ print $3}')
	count=$(ping -c $ping_times $gwip|awk -F, '/received/{print $2*1}')
	if [ $count lt "$ping_times" ]; then
		echo "Failed:Ping $gwip lost package , count: $count" >> $LOGFILE 2>&1
		exit 1
	fi
}

logf(){
	echo >>$LOGFILE `date "+%F %T"` "$*"	
}

log(){
	logger "From Redis Check: $*"
}

get_kp_state(){
	log $kpstate
	if [  -e "$kpstate" ]; then
		state=$(cat $kpstate|head -n 1|awk -F: '{print $1}')
#		log $state
		if [ "x${state}" == "xBACKUP" ]; then
		 	echo "1"
			return 1
		else
			echo "2"
			return 2
		fi
	fi
	log "Instance $2 is first Startup!!!"	
	echo "0"
	return 0
}

st=$(get_kp_state)
#log "${st}"
if [ ${st} eq "1"]; then
	get_ping_gw
fi

ALIVE=$($REDISCLI -h $1 -p $2 PING 2>/dev/null) 
#log $ALIVE
if [ "x${ALIVE}" == "xPONG" ]; then : 
   logf "Success: redis-cli -h $1 -p $2 PING $ALIVE"  2>&1
   exit 0 
else 
   logf "Failed:redis-cli -h $1 -p $2 PING $ALIVE" 2>&1
   exit 1 
fi 
