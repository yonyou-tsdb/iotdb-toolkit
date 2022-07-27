# IoTDB-UI

IoTDB-UI是一个可以深度管理IoTDB的管理系统，它提供了桌面软件级别的精确管理， 希望它能对您使用 IoTDB 有所帮助。

#### 最新版本

`0.12.5`

#### 需求

Java 1.8 or above

Maven 3.3.0 or above

Nginx

#### Docker部署（推荐）

请见 https://github.com/limeng32/iotdbui-back/blob/docker/README.zh.md

#### 传统方式部署

1.在项目根目录执行 `mvn clean install` 以编译，之后在 target 文件夹下可找到 iotdb-ui-0.12.5.jar 文件

2.确保 sqlite 文件 `iotdbui.db` 存在于项目根目录内。如果您使用其它数据库，需要修改 `src/main/resources/application.yml` 文件

3.从 `0.12.4` 开始增加了发送邮件服务，如果您需要使用此功能，需要有一个开启了 smtp 服务的邮箱，然后在项目根目录下新增  application.yml 文件并在其中加入邮箱配置，以及 IoTDB-UI 的前端（即 IP 和端口）frontend。例如演示服务器上的配置如下：

```
iotdbui:
  frontend: 115.28.134.232:8800
  email:
    port: 465
    host: 'smtp.xxx.com.'
    username: 'xxx@xxx.com'
    password: 'xxxxxx'
```

如果不在配置文件加入这些内容也不影响使用默认账号登录系统，但无法使用基于邮箱的注册账号、找回密码等功能

4.启动后端和前端服务：

- 在项目根目录执行 `java -jar target/iotdb-ui-0.12.5.jar --spring.config.location=application.yml` 以启动，默认使用 8080 端口
  
- 使用 nginx 映射 `/front/dist` 中的内容，或者映射在 iotdb-ui 前端项目中手动构建的内容。 例如，下面的配置将前端映射到了 8040 端口，同时将后端转发到 8080 端口:

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

- 如果在 nginx 上启用 websocket 功能，可以获得更好的用户体验。如果不启用 websocket 功能，也不会影响使用

5.现在在浏览器打开之前设置的端口（如 http://localhost:8040/ ），开始享受 iotdb 的魅力吧！
