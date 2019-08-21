#!/bin/sh
echo ==========try to stop sia-rabbitmq-plus-heartbeat===============
process=`ps -ef | grep 'sia-rabbitmq-plus-heartbeat' |  grep -v grep | awk '{print $2}'`
echo $process
kill -9 $process
echo ==========stop sia-rabbitmq-plus-heartbeat OK=====================

