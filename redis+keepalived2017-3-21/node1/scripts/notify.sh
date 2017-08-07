#/bin/bash
log(){
	logger "From notify: $*"
}
log $@
#$1 = "GROUP|INSTANCE"
#$2 =  name of group instace
#$3 =  instances state : MASTER|BACKUP|FAULT
#$4 =  priority
echo $3:$4 > /var/run/keepalived.$2.state
