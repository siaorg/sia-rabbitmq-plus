#!/bin/sh
echo ==========try to stop sia-rabbitmq-plus-display=================
kill -9 $(ps -ef | grep 'sia-rabbitmq-plus-display' |  grep -v "grep" | awk '{print $2}')
echo ==========stop sia-rabbitmq-plus-display OK=====================