# IoTDB-UI

IoTDB-UI is a management system that can deeply manage IoTDB. It provides precise management at the desktop software level. I hope it will be helpful for you when using IoTDB.

#### Required

Java 1.8 or above

Maven 3.3.0 or above

Nginx

#### Deployment steps

1. In the project root directory execute `mvn clean package` to compile
2. In the project root directory execute `java -jar target/iotdb-ui-0.12.3-SNAPSHOT` to startup, 8080 port is used by default
3. Map the contents in `/front/dist` or the front-end project compiled by yourself to a port through nginx. For example, the following configuration maps the application to port 8040:
```
server {
	listen		8040;
	server_name	localhost;
	location / {
		root	iotdbui-back/front/dist;
		index	index.html;
	}
	location /api/ {
           proxy_pass    http://localhost:8080/api/;
        }
}
```
4. If you enable the websocket feature on nginx, you can get a better user experience. If you do not enable it, it will not affect the use at all;
5. Now use your browser to open the port you previously set (for example http://localhost:8040/ ）, start to enjoy iotdb!
