#!/bin/sh
echo ==========try to stop skytrain-display================= 
kill -9 $(ps -ef | grep 'skytrain-display' |  grep -v "grep" | awk '{print $2}')
echo ==========stop skytrain-display OK=====================