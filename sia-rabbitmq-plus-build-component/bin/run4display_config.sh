#!/bin/sh

# First, find suitable JDK
version=$("java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
jdk_home="no"
if [[ "$version" > "1.8" ]]; then
       jdk_home=${JAVA_HOME}
       echo "default JDK version is OK, JDK home is $jdk_home"
else
      jdk_path=/opt
      echo "begin to find suitable JDK...."
      for path in `find $jdk_path -name jmap`
      do
         _java=${path%/*}/java
         version=$("$_java" -version 2>&1 | awk -F '"' '{print $2}')
         if [[ "$version" > "1.8" ]]; then
             jdk_home=${_java%/bin*}
             echo "find out suitable JDK, JDK home is $jdk_home"
             break
         fi
      done
fi

if [ "$jdk_home" == "no" ] ;then
  echo "no suitable JDK was found, which is required jdk1.8, exit"
  exit 0
fi

JAVA_HOME=$jdk_home
CLASSPATH=.:$JAVA_HOME/lib:$JAVA_HOME/jre/lib
export PATH=$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH
echo "-------------------------java info-------------------------"
echo $(java -version)
echo "-------------------------pwd-------------------------"
echo $(pwd)

vi
# Second, choose profile
display_yml="display_application.yml"
working_directory=$(pwd)
echo "using workspace $working_directory"

javaOpts="-server -Xms128m -Xmx256m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection -XX:-CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=0 -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=."
java $javaOpts -XX:OnOutOfMemoryError='kill -9 %p'  -Dspring.config.location=../config/$display_yml  -jar  $working_directory/$2 &


