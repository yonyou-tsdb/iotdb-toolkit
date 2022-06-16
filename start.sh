#!/bin/bash
export JAVA_HOME=/usr/zulu-8
export PATH=$PATH:$JAVA_HOME/bin:$JAVA_HOME/jre/bin

if [ -z $iotdbui_db_name ]
then
   sed -i "s/_iotdbui_db_name_/iotdbui\.db/" /root/application.yml
else
   sed -i "s/_iotdbui_db_name_/$iotdbui_db_name/" /root/application.yml
fi

if [ -z $iotdbui_frontend ]
then
   sed -i "s/_iotdbui_frontend_/127\.0\.0\.1:80/" /root/application.yml
else
   sed -i "s/_iotdbui_frontend_/$iotdbui_frontend/" /root/application.yml
fi

if [ -z $iotdbui_email_port ]
then
   sed -i "s/_iotdbui_email_port_/465/" /root/application.yml
else
   sed -i "s/_iotdbui_email_port_/$iotdbui_email_port/" /root/application.yml
fi

if [ -z $iotdbui_email_host ]
then
   sed -i "s/_iotdbui_email_host_//" /root/application.yml
else
   sed -i "s/_iotdbui_email_host_/$iotdbui_email_host/" /root/application.yml
fi

if [ -z $iotdbui_email_username ]
then
   sed -i "s/_iotdbui_email_username_//" /root/application.yml
else
   sed -i "s/_iotdbui_email_username_/$iotdbui_email_username/" /root/application.yml
fi

if [ -z $iotdbui_email_password ]
then
   sed -i "s/_iotdbui_email_password_//" /root/application.yml
else
   sed -i "s/_iotdbui_email_password_/$iotdbui_email_password/" /root/application.yml
fi

/usr/sbin/nginx -c /etc/nginx/nginx.conf -g 'daemon on;'
java -jar /root/iotdb-ui.jar --spring.config.location=/root/application.yml
