# IoTDB-UI

IoTDB-UI is a management system that can deeply manage IoTDB. It provides precise management at the desktop software level. I hope it will be helpful for you when using IoTDB.

#### Latest version

`0.12.4`

#### Required

Java 1.8 or above

Maven 3.3.0 or above

Nginx

#### Deployment steps

1.In the project root directory execute `mvn clean install` to compile, then you can find the iotdb-ui-0.12.4.jar file in the target folder

2.Make sure the sqlite file `iotdbui.db` exists in the project root directory. If you use another database, you need to modify the `src/main/resources/application.yml` file

<font color='red'>3. The sending mail service has been added since `0.12.4`. If you need to use this function, you need to have a mailbox with the smtp service enabled, and then add the relevant configuration to application.yml in the project root directory, for example, in the demo The configuration on the server is as follows:</font>

```
iotdbui:
  frontend: 115.28.134.232:8800
  email:
    port: 465
    host: 'smtp.xxx.com.'
    username: 'xxx@xxx.com'
    password: 'xxxxxx'
```

<font color='red'>The reason why modifying application.yml in the root directory will take effect is because the command in the Dockerfile points to this file. If you use a configuration file in another location, just add the above configuration to the file you use</font>

4.Deploy using docker-compose (require docker and docker-compose):

- In the project root directory execute `docker-compose up -d`

5.Deploy using traditional way:

- In the project root directory execute `java -jar target/iotdb-ui-0.12.4.jar --spring.config.location=application.xml` to startup, 8080 port is used by default

- Map the contents in `/front/dist` or the front-end project compiled by yourself to a port through nginx. For example, the following configuration maps the application to port 8040:

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

- If you enable the websocket feature on nginx, you can get a better user experience. If you do not enable it, it will not affect the use at all;

6.Now use your browser to open the port you previously set (for example http://localhost:8040/ ）, start to enjoy iotdb!
