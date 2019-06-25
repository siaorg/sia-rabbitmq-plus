#!/bin/sh
echo ==========try to stop skytrain-supervise================= 
kill -9 $(ps -ef | grep 'skytrain-supervise' |  grep -v "grep" | awk '{print $2}')
echo ==========stop skytrain-supervise OK=====================