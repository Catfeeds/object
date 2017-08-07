#!/bin/sh
# --
# Check if all specified nfs mounts exist and if they are correct implemented.
# That means we check /etc/fstab, the mountpoints in the filesystem and if they
# are mounted. It is written for Linux, uses proc-Filesystem and was tested on
# Debian, OpenSuse 10.1 10.2 10.3 11.0, SLES 10.1 10.2
#
# @author: Daniel Werdermann / optivo GmbH / daniel.werdermann@optivo.de
# @version: 0.5
# @date: Mo 23. Okt 15:35:27 CEST 2008
# --


# --------------------------------------------------------------------
# configuration
# --------------------------------------------------------------------
PROGNAME=$(basename $0)
ERR_MESG=()
LOGGER="/bin/logger -i -p kern.warn -t"
MTAB=/proc/mounts
export PATH="/bin:/sbin:/usr/bin:/usr/sbin"
LIBEXEC="/usr/local/nagios/libexec"
. $LIBEXEC/utils.sh


#aliasname=`ls -l /opt/|grep ihaveu|grep " ->" |awk -F'-> ' '{print $2}'`
#aliasname=`ls -l  /|grep mnt|grep " ->" |awk -F'-> ' '{print $2}'`
#aliasname=`ls -l  /|grep mnt|grep " ->" |awk -F'-> ' '{print $2}'|awk -F'\/' '{print $2}'`
#aliasname=/mnt
aliasname=`df -h |grep production|awk '{print $5}'|sed -n '$p'`
#echo $aliasname

# --------------------------------------------------------------------


# --------------------------------------------------------------------
# functions
# --------------------------------------------------------------------
function log() {
	$LOGGER ${PROGNAME} "$@";
}

function usage() {
	echo "Usage: $PROGNAME [-m FILE] \$NFSmountpoint [\$NFSmountpoint2 ...]"
	echo "Usage: $PROGNAME -h,--help"
	echo "Options:"
	echo " -m FILE   Use this mtab instead (default is /proc/mounts)"
}

function print_help() {
	echo ""
	usage
	echo ""
	echo "Check if nfs mountpoints are correct implemented and mounted."
	echo ""
	echo "This plugin is NOT developped by the Nagios Plugin group."
	echo "Please do not e-mail them for support on this plugin, since"
	echo "they won't know what you're talking about."
	echo ""
	echo "For contact info, read the plugin itself..."
}

# --------------------------------------------------------------------
# startup checks
# --------------------------------------------------------------------

if [ $# -eq 0 ]; then
	usage
	exit $STATE_CRITICAL
fi

	case "$1" in
		--help) print_help; exit $STATE_OK;;
		-h) print_help; exit $STATE_OK;;
		-m) MTAB=$2; shift 2;;
		/*) MP=$aliasname;;
		*) usage; exit $STATE_UNKNOWN;;
	esac

if [ ! -f /proc/mounts ]; then
	log "WARN: /proc wasn't mounted!"
	mount -t proc proc /proc
	ERR_MESG[${#ERR_MESG[*]}]="WARN: mounted /proc $?"
fi

if [ ! -f ${MTAB} ]; then
	log "WARN: ${MTAB} don't exist!"
	echo "WARN: ${MTAB} don't exist!"
	exit $STATE_CRITICAL
fi

# --------------------------------------------------------------------
# now we check if the given parameters ...
#  1) ... exist in the /etc/fstab
#  2) ... are mounted
#  3) ... exist on the filesystem
#  4) ... df -k gives no stale
# --------------------------------------------------------------------
	awk '{if ($3=="nfs"){print $2}}' /proc/mounts | grep -q ${MP} &>/dev/null
	if [ $? -ne 0 ]; then
		log "WARN: ${MP} don't exists in /proc/mounts"
		ERR_MESG[${#ERR_MESG[*]}]="$1 don't exists in /proc/mounts"
	fi

	grep -q /proc/mounts -e " ${MP} nfs " &>/dev/null
	if [ $? -ne 0 ]; then
		log "WARN: ${MP} isn't mounted"
		ERR_MESG[${#ERR_MESG[*]}]="$1 isn't mounted"
	fi

	if [ ! -d ${MP} ]; then
		log "WARN: ${MP} don't exists in filesystem"
		ERR_MESG[${#ERR_MESG[*]}]="${MP} don't exists in filesystem"
	fi

	if [ `df -k ${MP} | grep "Stale NFS file handle" | wc -l` -gt 0 ]; then
		ERR_MESG[${#ERR_MESG[*]}]="${MP} is Stale NFS mount"
	fi

if [ ${#ERR_MESG[*]} -ne 0 ]; then
	echo -n "CRITICAL: "
	for element in "${ERR_MESG[@]}"; do
		echo -n ${element}" ; "
	done
	echo
	exit $STATE_CRITICAL
fi

echo "OK: all mounts were found"
exit $STATE_OK
