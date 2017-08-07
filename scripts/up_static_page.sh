#!/bin/sh

. /etc/init.d/functions
. /etc/profile

#############################

#ACTS=management-web
__project=/opt/project
__base=management.zip
__date=`date +%F-%H-%M`
__old_static=/opt/tmp
__backup=/opt/backup
__tomcat=/data/tomcat-management/webapps
__static=/data/tomcat-management/webapps/management
############################

if [ $# != 1 ] ; then
echo "USAGE: $0 project_path " 
echo " e.g.: $0 2017-03-08-17-56-52 " 
exit 1;
fi


rsync -avzP 10.3.201.6:/opt/action-online/$1/${__base}  ${__project} || exit 2

cd ${__tomcat}

tar zcf ${__backup}/management-${__date} ${__old_static}/management 

mv ${__static} ${__old_static}

cd  ${__project}

unzip ${__base} -d ${__tomcat}


action " update static_page sucessful" /bin/true


