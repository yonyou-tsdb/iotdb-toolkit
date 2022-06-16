# IoTDB-UI

IoTDB-UI是一个可以深度管理IoTDB的管理系统，它提供了桌面软件级别的精确管理， 希望它能对您使用 IoTDB 有所帮助。

#### 最新版本

`0.12.4`

#### 需求

docker

#### 部署方式

1.执行 `docker pull limeng32/iotdbui` 或 `docker pull limeng32/iotdbui:0.12.4` 以获取最新镜像

2.立刻开始使用：执行 `docker run -p 80:80 -it --rm limeng32/iotdbui`，可以不设置任何环境变量就直接启动（镜像内部暴露 `80` 端口）。这种情况下可以使用 iotdbui 大部分功能，但无法使用账号管理功能，且容器关闭后数据不保存

3.增加保存数据功能：下载 <a href="https://github.com/limeng32/iotdbui-back/blob/docker/iotdbui.db">https://github.com/limeng32/iotdbui-back/blob/docker/iotdbui.db</a> 或按 <a href="https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql">https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql</a> 中的结构建立 iotdbui.db 文件，若其路径为 `/foo/bar/iotdbui.db`，则执行 `docker run -p 80:80 -v /foo/bar/iotdbui.db:/root/iotdbui.db -it --rm limeng32/iotdbui`，这样数据可以保存在 `/foo/bar/iotdbui.db` 中。

4.使用账号管理功能：为了使此功能可用需要您有一个开启了 smtp 服务的邮件服务器，并在启动时增加以下环境变量

| 名称 | 含义  |  范例  |
|:--------|:-------:|-------:|
| `iotdbui_frontend` | 部署iotdbui的 IP和端口  | `127.0.0.1:80` |
| `iotdbui_email_port` | 邮件服务器的端口  | `465` |
| `iotdbui_email_host` | 邮件服务器的地址  | `smtp.xxx.com.` |
| `iotdbui_email_username` | 邮件服务器的用户名  | `postmaster@foo.bar` |
| `iotdbui_email_password` | 邮件服务器的密码  | `xxxxxxxx` |

例如执行 `docker run -p 80:80 -v /foo/bar/iotdbui.db:/root/iotdbui.db -e iotdbui_frontend="127.0.0.1:80" -e iotdbui_email_port=465 -e iotdbui_email_host="smtp.xxx.com." -e iotdbui_email_username="postmaster@foo.bar" -e iotdbui_email_password="xxxxxxxx" -it --rm limeng32/iotdbui`，这样您就可以通过邮箱注册自己的账号
