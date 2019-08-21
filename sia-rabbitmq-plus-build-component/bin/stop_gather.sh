#!/bin/sh
echo ==========try to stop sia-rabbitmq-plus-gather===============
process=`ps -ef | grep 'sia-rabbitmq-plus-gather' |  grep -v grep | awk '{print $2}'`
echo $process
kill -9 $process
echo ==========stop sia-rabbitmq-plus-gather OK=====================