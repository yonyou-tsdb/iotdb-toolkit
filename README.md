# IoTDB-UI

IoTDB-UI is a management system that can deeply manage IoTDB. It provides precise management at the desktop software level. I hope it will be helpful for you when using IoTDB.

#### Latest version

`0.12.4`

#### Required

docker

#### Deployment steps

1. Execute `docker pull limeng32/iotdbui` or `docker pull limeng32/iotdbui:0.12.4` to get the latest image

2. Start using it immediately: execute `docker run -p 80:80 -it --rm limeng32/iotdbui`, you can start it directly without setting any environment variables (the `80` port is exposed inside the image). In this case, most of the functions of iotdbui can be used, but the account management function cannot be used, and the data will not be saved after the container is closed

3. Add the function of saving data: download <a href="https://github.com/limeng32/iotdbui-back/blob/docker/iotdbui.db">https://github.com/limeng32/iotdbui-back/blob/docker/iotdbui.db</a> or follow
 <a href="https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql">https://github.com/limeng32/iotdbui-back/blob/master/src/test/resources/db/schema.iotdb-ui.dev.sql</a> to build the structure in the iotdbui.db file, If its path is `/foo/bar/iotdbui.db`, ​​execute
 `docker run -p 80:80 -v /foo/bar/iotdbui.db:/root/iotdbui.db -it --rm limeng32/iotdbui`, then the data can be saved in `/foo/bar/iotdbui.db`.
 
 4. Use the account management function: In order to make this function available, you need to have a mail server with the smtp service enabled, and add the following environment variables at startup

| Name | Meaning | Example |
|:-------|:-------:|-------:|
| `iotdbui_frontend` | IP and port where iotdbui is deployed | `127.0.0.1:80` |
| `iotdbui_email_port` | The port of the mail server | `465` |
| `iotdbui_email_host` | The address of the mail server | `smtp.xxx.com.` |
| `iotdbui_email_username` | Email server username | `postmaster@foo.bar` |
| `iotdbui_email_password` | Email server password | `xxxxxxxx` |

For example, execute `docker run -p 80:80 -v /foo/bar/iotdbui.db:/root/iotdbui.db -e iotdbui_frontend="127.0.0.1:80" -e iotdbui_email_port=465 -e iotdbui_email_host="smtp.xxx.com." -e iotdbui_email_username="postmaster@foo.bar" -e iotdbui_email_password="xxxxxxxx" -it --rm limeng32/iotdbui`, then you can register your account by email