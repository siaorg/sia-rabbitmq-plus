#!/bin/sh
version=$("java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
jdk_home="no"
if [[ "$version" > "1.8" ]]; then
       echo default jdk version is ok,continue
       jdk_home=${JAVA_HOME}
else
      echo "default jdk is not ok, please input the path where you want to search, / is for all path" 
      read jdk_path
      while [ ! -d "$jdk_path"  ]
      do
           echo "Installer: path not exist, please input again"
           read jdk_path
      done
      echo "Installer: begin to look correct jdk path...."       
      for path in `find $jdk_path -name jmap`
      do
         _java=${path%/*}/java
         version=$("$_java" -version 2>&1 | awk -F '"' '{print $2}')
         if [[ "$version" > "1.8" ]]; then            	
		 		jdk_home=${_java%/bin*}
		 		echo "Installer: find out correct jdk,jdk home is $jdk_home"
		 		break
         fi
       done
fi

if [ "$jdk_home" == "no" ] ;then
	echo "Installer: no correct jdk was found,which is required jdk1.8"
	exit 0 
fi

cd ..

JAVA_HOME=$jdk_home 
CLASSPATH=.:$JAVA_HOME/lib:$JAVA_HOME/jre/lib
export PATH=$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH

echo $(java -version)
echo $(pwd)

nohup java -server -Xms128m -Xmx256m -Xss256k -jar $(pwd)/lib/skytrain-display-1.0-SNAPSHOT.jar  1>/dev/null 2>&1 &

echo '======================ALL DONE=============================='
