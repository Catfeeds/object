#!/bin/bash
# check memory script
# chuzhuo 2011.5.27
# Total memory
TOTAL=`free -m | head -2 |tail -1 |gawk '{print $2}'`
# Free mem
FREE=`free -m | awk 'NR==3 {print $4}'`
#used mem percent
USED=`expr $TOTAL - $FREE`
USEDTMP=`expr $USED \* 100`
PERCENT=`expr $USEDTMP / $TOTAL`

if [ $PERCENT -lt $1 ]
then
    echo "OK -used mem is $PERCENT%"
    exit 0

fi
if [ $PERCENT -gt $1 -a $PERCENT -lt $2 ]
then
    echo "Warning -used mem is $PERCENT%"
    exit 1
fi
if [ $PERCENT -gt $2 ]
then
    echo "Critical -used mem is $PERCENT%"
    exit 2
fi
