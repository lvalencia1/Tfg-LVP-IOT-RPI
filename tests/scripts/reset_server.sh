#!/bin/bash
#This script is just a workaround to clean the server and unzip it 
#again to allow new testing.
#
#Usage: [route_to_server] [route_to_ZIP]
#
#
if [ "$#" -ne 2 ]; then
	echo "Usage: $0 [route_to_server] [route_to_ZIP]" >&2
	exit 1
fi
if ! [ -d "$1" ]; then
	echo "[ERROR:] $1 directory does not exist" >&2
	exit 1
fi
if ! [ -e "$2" ]; then
	echo "[ERROR:] $2 zip file does not exist" >&2
	exit 1
fi

PID_LIST=`ps aux | grep wso2 | grep java | awk '{print $2}'`
echo $PID_LIST
for PID in $PID_LIST; do
	echo "[INFO:] Killing process $PID"
	kill $PID
	sleep 1
done

while ! [ -z "$PID_LIST" ]; do
	PID_LIST=`ps aux | grep wso2 | grep java | awk '{print $2}'`
	echo "[INFO:] Waiting server process to exit, sleeping 2 seconds"
	sleep 2
done
cd $1
cd ..
rm -rf $1 && unzip $2 &>/dev/null
