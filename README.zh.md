# IoTDB-UI

IoTDB-UI是一个可以深度管理IoTDB的管理系统，它提供了桌面软件级别的精确管理， 希望它能对您使用 IoTDB 有所帮助。

#### 需求

Java 1.8 or above

Maven 3.3.0 or above

Nginx

#### 部署步骤

1.在项目根目录执行 `mvn clean package` 以编译

2.确保 sqlite 文件 `iotdbui.db` 存在于项目根目录内。如果您使用其它数据库，需要修改 `src/main/resources/application.yml` 文件

3.使用 docker-compose 部署（需安装 docker 及 docker-compose）：

- 在项目根目录执行 `docker-compose up`
  
4.使用传统方式部署：

- 在项目根目录执行 `java -jar target/iotdb-ui-0.12.3-SNAPSHOT` 以启动，默认使用 8080 端口
  
- 使用 nginx 映射 `/front/dist` 中的内容，或者映射在iotdb-ui前端项目中手动构建的内容。 例如，下面的配置将前端映射到了 8040 端口，同时将后端转发到 8080 端口:

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
