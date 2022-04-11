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

3. The sending mail service has been added since `0.12.4`. If you need to use this function, you need to have a mailbox with the smtp service enabled, then add the relevant configuration to application.yml in the project root directory, and the frontend of IoTDB-UI (i.e. IP and port). for example, in the demo The configuration on the server is as follows:

```
iotdbui:
  frontend: 115.28.134.232:8800
  email:
    port: 465
    host: 'smtp.xxx.com.'
    username: 'xxx@xxx.com'
    password: 'xxxxxx'
```

The reason why modifying application.yml in the root directory will take effect is because the command in the Dockerfile points to this file, you can also add it to the configuration file in /src/main/resources/application.yml or other location you use . If you do not add these contents to the configuration file, it will not affect the use of the default account to log in to the system, but you cannot use the email-based account registration, password retrieval and other functions

After this update, the default account is changed from admin to user. Due to stricter security requirements, the admin account is no longer available. Also add a new table tb_email_log (<a href="https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql">all related table information</a>), if you need to keep the data in the local iotdbui.db, you can manually add the tb_email_log table, and then copy the default account information in the tb_user table in the iotdbui.db under the project root directory to the database you use

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
