#!/bin/bash
export JAVA_HOME=/usr/zulu-8
export PATH=$PATH:$JAVA_HOME/bin:$JAVA_HOME/jre/bin
/usr/sbin/nginx -c /etc/nginx/nginx.conf -g 'daemon on;'
java -jar -Diotdbui.frontend='127.0.0.1:7776' /root/iotdb-ui.jar
