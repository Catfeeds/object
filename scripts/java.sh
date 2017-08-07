#!/bin/sh

. /etc/init.d/functions
. /etc/profile

#############################

#ACTS=acts-service
BASE=card-$2-service
DATE=`date +%F-%H-%M`
OLD_PROJECT=/opt/tmp
BACKUP=/opt/backup
NEW_PROJECT=/opt/project
BACK_NAME=$2
############################


#######  check project  #######

if [ $# != 2 ] ; then 
echo "USAGE: $0 project_path project_name " 
echo " e.g.: $0 2017-03-08-17-56-52 base" 
exit 1; 
fi 

if [ ! -d "/data/$BASE" ]; then
  action 'please check project path and servser path !!!' /bin/false
 exit 2
fi
rsync -avzP 10.3.201.6:/opt/card-online/$1/${BASE}*  /opt/project/ || exit 3


#######  kill pid  #######

	java_pid() {
	    echo `ps -ef | grep ${BASE} | grep -v grep | tr -s " "|cut -d" " -f2`
	}
pid=$(java_pid)
  if [ -n "$pid" ];then
      kill  $pid
      cd ${OLD_PROJECT}
      tar zcf ${BACKUP}/${BACK_NAME}-${DATE}.tar.gz  ./${BASE}
      rm -fr /opt/tmp/${BASE}
      action 'backup successful' /bin/true
      mv /data/${BASE} ${OLD_PROJECT}
      tar xf ${NEW_PROJECT}/${BASE}* -C /data/
      action 'Extract the complete' /bin/true 
  else
      cd ${OLD_PROJECT}
      tar zcf ${BACKUP}/${BACK_NAME}-${DATE}.tar.gz  ./${BASE}
      rm -fr /opt/tmp/${BASE}
      action 'backup successful' /bin/true
      mv /data/${BASE} ${OLD_PROJECT}/
      tar xf ${NEW_PROJECT}/${BASE}* -C /data/
      action 'Extract the complete' /bin/true
  fi


#######  Start the java process  #######

cd /data/${BASE} && ./bin/start.sh

sleep 2

start_pid=`ps -ef |grep ${BASE}|grep -v grep |wc -l`
   if [ ${start_pid} = 1 ]
       then
   	action ' service server started!' /bin/true
   else
	action ' service start failed' /bin/false
   fi

