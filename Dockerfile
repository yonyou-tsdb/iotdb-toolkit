FROM nginx:1.21.0

COPY start.sh /root/start.sh

COPY zulu-8/ /usr/zulu-8/

COPY iotdb-ui-0.12.5.jar /root/iotdb-ui.jar

COPY iotdbui.db /root/iotdbui.db

COPY application.yml /root/application.yml

COPY nginx.conf /etc/nginx/nginx.conf

COPY dist-0.12.5/ /usr/share/nginx/html

RUN chmod -R +x /usr/zulu-8/

RUN sed -i 's/\r$//' /root/start.sh  && chmod +x /root/start.sh

ENTRYPOINT /root/start.sh