#!/bin/bash
#
# This script will configure the automatic launch of the agent when then
# raspberry pi is initialized, using the service script agentd.sh
#
#
PATH_DIR=`echo $PWD/$0 | sed 's/configure.sh//g'`
echo $PATH_DIR

sed -i "s+PATH_DIR+$PATH_DIR+g" agentd.sh
sudo cp ./agentd.sh /etc/init.d/
sudo update-rc.d agentd.sh defaults
