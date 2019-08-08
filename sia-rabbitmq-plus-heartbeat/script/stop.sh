#!/bin/sh
echo ==========try to stop skytrain-supervise================= 
kill -9 $(ps -ef | grep 'sia-rabbitmq-plus-heartbeat' |  grep -v "grep" | awk '{print $2}')
echo ==========stop sia-rabbitmq-plus-heartbeat OK=====================