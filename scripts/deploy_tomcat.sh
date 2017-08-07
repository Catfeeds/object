#!/bin/sh

. /etc/init.d/functions
. /etc/profile

#############################

#ACTS=management-web
BASE=card-$2-web
DATE=`date +%F-%H-%M`
OLD_PROJECT=/opt/tmp
BACKUP=/opt/backup
NEW_PROJECT=/opt/project
FILE=`ls ${NEW_PROJECT}`
BACK_NAME=$2
TOMCAT=/data/tomcat-service/webapps
TOMCAT_ADMIN=/data/tomcat-netty/webapps
TOMCAT_START=/data/tomcat-service
TOMCAT_ADMIN_START=/data/tomcat-netty
############################

if [ $# != 2 ] ; then
echo "USAGE: $0 project_path project_name " 
echo " e.g.: $0 2017-03-08-17-56-52 base" 
exit 1;
fi

#if [ ! -d "/data/$BASE" ]; then
#  action 'please check project path and servser path !!!' /bin/false
# exit 2
#fi

rsync -avzP 10.3.201.6:/opt/card-online/$1/${BASE}*  /opt/project/ || exit 2

if [ ${BASE} = card-service-web ]
then
    tomcat_pid() {
    	echo `ps -ef | grep tomcat-service | grep -v grep | tr -s " "|cut -d" " -f2`
       }

	pid=$(tomcat_pid)
	  if [ -n "$pid" ];then
	    kill $pid
		sleep 30
	    action  'tomcat is stoped!!!' /bin/true
	  else
	    action  'tomcat is not running!!!' /bin/true
	  fi

	 cd ${OLD_PROJECT}
	      tar zcvf ${BACKUP}/${BACK_NAME}-${DATE}.tar.gz  ./${BASE}
	      rm -fr /opt/tmp/${BASE}
	      action 'backup successful' /bin/true
	      mv  ${TOMCAT}/${BASE} /opt/tmp/
	      cd ${NEW_PROJECT}/
	      unzip ${BASE}* -d ${TOMCAT}/${BASE}
	      action 'Extract the complete' /bin/true


	${TOMCAT_START}/bin/startup.sh && tailf ${TOMCAT_START}/logs/catalina.out
else
   tomcat_pid() {
        echo `ps -ef | grep tomcat-netty | grep -v grep | tr -s " "|cut -d" " -f2`
       }

        pid=$(tomcat_pid)
          if [ -n "$pid" ];then
            kill $pid
		sleep 15
            action  'tomcat is stoped!!!' /bin/true
          else
            action  'tomcat is not running!!!' /bin/true
          fi

         cd ${OLD_PROJECT}
              tar zcvf ${BACKUP}/${BACK_NAME}-${DATE}.tar.gz  ./${BASE}
              rm -fr /opt/tmp/${BASE}
              action 'backup successful' /bin/true
              mv  ${TOMCAT_ADMIN}/${BASE} /opt/tmp/
              cd ${NEW_PROJECT}/
              unzip ${BASE}* -d ${TOMCAT_ADMIN}/${BASE}
              action 'Extract the complete' /bin/true


        ${TOMCAT_ADMIN_START}/bin/startup.sh && tailf ${TOMCAT_ADMIN_START}/logs/catalina.out

fi
