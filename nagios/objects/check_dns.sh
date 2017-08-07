#!/bin/bash

# -------------------------------------------------------------------------------
# Filename:    check_dns.sh
# Revision:    1.0
# Date:        2007/02/05
# Author:      Luigi Balzano
# Email:       info@sistemistica.it
# Website:     www.sistemistica.it
# Description: Plugin to monitor changes of the IP address stored in the A record
#              of a DNS resource.
# Notes:       This plugin uses the "dig" command
# -------------------------------------------------------------------------------
# Copyright:   2007 (c) Luigi Balzano
# License:     GPL
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty
# of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# you should have received a copy of the GNU General Public License
# along with this program (or with Nagios); if not, write to the
# Free Software Foundation, Inc., 59 Temple Place - Suite 330,
# Boston, MA 02111-1307, USA
# -------------------------------------------------------------------------------
# Credits go to Ethan Galstad for coding Nagios
# If any changes are made to this script, please mail me a copy of the changes
#

PROGNAME=`basename $0`
PROGPATH=`echo $0 | /bin/sed -e 's,[\\/][^\\/][^\\/]*$,,'`
REVISION=`echo '$Revision: 1.0 $' | sed -e 's/[^0-9.]//g'`

. $PROGPATH/utils.sh

print_usage() {
        echo "Usage: $PROGNAME -h"
        echo "Usage: $PROGNAME -v"
        echo "Usage: $PROGNAME -s STATUSFILE -l LOOKUPHOST"
        echo ""
        echo "Path of STATUSFILE must be relative to the program path"
        echo "(e.g. no path needed if status file is in the program dir)"
}

print_help() {
        print_revision $PROGNAME $REVISION
        echo ""
        print_usage
        echo ""
        echo "Plugin to monitor changes of the IP address stored in the A record"
        echo "of a DNS resource."
        echo ""
        support
}

# No command line arguments are supplied for this script. Print help

if [ "$#" -eq "0" ]; then
        print_usage
        exit $STATE_UNKNOWN
fi

# Print help with --help

if [[ ("$#" -eq "1") && ("$1" = "--help") ]]; then
        print_help
        exit $STATE_UNKNOWN
fi

unset STATUSFILE
unset LOOKUPHOST
FIRSTEXEC="FIRSTEXEC"

while getopts "vhs:l:" Option
do
  case $Option in
    h     ) print_help
            exit $STATE_UNKNOWN;;
    v     ) print_revision $PROGNAME $REVISION
            exit $STATE_UNKNOWN;;
    s     ) STATUSFILE=$OPTARG;;
    l     ) LOOKUPHOST=$OPTARG;;
    *     ) print_usage
            exit $STATE_UNKNOWN;;   # DEFAULT
  esac
done

if [[ (! -n "$STATUSFILE") || (! -n "$LOOKUPHOST") ]]
  then
        echo "Unspecified argument"
        print_usage
        exit $STATE_UNKNOWN
fi

if [ -f $PROGPATH/$STATUSFILE ]
  then
        . "$PROGPATH/$STATUSFILE"
  else
        LASTLOOKUP=$FIRSTEXEC
fi

NEWLOOKUP=`dig ${LOOKUPHOST} A +short | grep -v "^;;" | tail -1`

#### Update status ####

echo "LASTLOOKUP=${NEWLOOKUP}" > "$PROGPATH/$STATUSFILE"

if [ "$LASTLOOKUP" == "$FIRSTEXEC" ]
  then
    if [ -n "$NEWLOOKUP" ]
      then
        echo "OK - Dig for ${LOOKUPHOST} is \"${NEWLOOKUP}\" (first execution)"
        exit $STATE_OK
      else
        echo "CRITICAL - Dig for ${LOOKUPHOST} is \"${NEWLOOKUP}\" (first execution)"
        exit $STATE_CRITICAL
    fi
  else
    if [ "$NEWLOOKUP" != "$LASTLOOKUP" ]
      then
        if [ -n "$LASTLOOKUP" ]
          then
            echo "CRITICAL - Dig for ${LOOKUPHOST} changes from \"${LASTLOOKUP}\" to \"${NEWLOOKUP}\" since last execution"
            exit $STATE_CRITICAL
          else
            echo "OK - Dig for ${LOOKUPHOST} changes from \"${LASTLOOKUP}\" to \"${NEWLOOKUP}\" since last execution"
            exit $STATE_OK
        fi
      else
        if [ -n "$NEWLOOKUP" ]
          then
            echo "OK - Dig for ${LOOKUPHOST} is still \"${NEWLOOKUP}\" since last execution"
            exit $STATE_OK
          else
            echo "CRITICAL - Dig for ${LOOKUPHOST} is still \"${NEWLOOKUP}\" since last execution"
            exit $STATE_CRITICAL
        fi
    fi
fi
