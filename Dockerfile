FROM nginx

COPY start.sh /root/start.sh

COPY zulu-8/ /usr/zulu-8/

COPY iotdb-ui-0.12.4.jar /root/iotdb-ui.jar

COPY application.yml /root/application.yml

COPY nginx.conf /etc/nginx/nginx.conf

COPY dist-0.12.4/ /usr/share/nginx/html

RUN chmod 777 /root/start.sh

RUN chmod -R 777 /usr/zulu-8/

ENTRYPOINT /root/start.sh
