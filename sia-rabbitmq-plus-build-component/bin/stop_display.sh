#!/bin/sh
echo ==========try to stop sia-rabbitmq-plus-display===============
process=`ps -ef | grep 'sia-rabbitmq-plus-display' |  grep -v grep | awk '{print $2}'`
echo $process
kill -9 $process
echo ==========stop sia-rabbitmq-plus-display OK=====================
