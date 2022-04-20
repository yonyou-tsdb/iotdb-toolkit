# IoTDB-UI

IoTDB-UI是一个可以深度管理IoTDB的管理系统，它提供了桌面软件级别的精确管理， 希望它能对您使用 IoTDB 有所帮助。

#### 最新版本

`0.12.4`

#### 需求

Java 1.8 or above

Maven 3.3.0 or above

Nginx

#### Docker 方式部署（需安装 docker）

1.执行 `docker pull limeng32/iotdbui` 或 `docker pull limeng32/iotdbui:0.12.4` 以获取最新镜像

2.立刻开始使用：执行 `docker run -p 80:80 -it limeng32/iotdbui`。镜像内部暴露 `80` 端口，可以不设置任何环境变量就直接启动，这种情况下可以使用 iotdbui 大部分功能，但无法使用邮箱功能，容器关闭后数据也不会保存

3.指定卷以让 iotdbui.db 保存数据：下载 <a href="https://github.com/limeng32/iotdbui-back/blob/docker/iotdbui.db">https://github.com/limeng32/iotdbui-back/blob/docker/iotdbui.db</a> 或按 <a href="https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql">https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql</a> 中的结构建立 iotdbui.db 文件，然后执行 `docker run -p 80:80 -v iotdbui.db:/iotdbui.db -it limeng32/iotdbui`。

#### 直接部署

1.在项目根目录执行 `mvn clean install` 以编译，之后在 target 文件夹下可找到 iotdb-ui-0.12.4.jar 文件

2.确保 sqlite 文件 `iotdbui.db` 存在于项目根目录内。如果您使用其它数据库，需要修改 `src/main/resources/application.yml` 文件

3.从 `0.12.4` 开始增加了发送邮件服务，如果您需要使用此功能，需要有一个开启了 smtp 服务的邮箱，然后在项目根目录下的 application.yml 中加入邮箱配置，以及 IoTDB-UI 的前端（即 IP 和端口）frontend。例如演示服务器上的配置如下：

```
iotdbui:
  frontend: 115.28.134.232:8800
  email:
    port: 465
    host: 'smtp.xxx.com.'
    username: 'xxx@xxx.com'
    password: 'xxxxxx'
```

之所以在根目录下修改 application.yml 会生效，是因为 Dockerfile 中的命令指向了这个文件，您也可以把它加入到 /src/main/resources/application.yml 或您使用的其他位置的配置文件。如果不在配置文件加入这些内容也不影响使用默认账号登录系统，但无法使用基于邮箱的注册账号、找回密码等功能

本次更新后默认账号由 admin 变为 user，因安全要求变得更加严格 admin 账号不再可用。同时加入一张新表 tb_email_log （<a href="https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql">所有相关表信息</a>），如果您需要保留本地 iotdbui.db 中的数据，可以手动新增 tb_email_log 表，再把项目根目录下 iotdbui.db 中 tb_user 表中默认账号的信息拷贝到自己使用的数据库

4.使用 docker-compose 部署（需安装 docker 及 docker-compose）：

- 在项目根目录执行 `docker-compose up -d`
  
5.使用传统方式部署：

- 在项目根目录执行 `java -jar target/iotdb-ui-0.12.4.jar --spring.config.location=application.xml` 以启动，默认使用 8080 端口
  
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

6.现在在浏览器打开之前设置的端口（如 http://localhost:8040/ ），开始享受 iotdb 的魅力吧！
