--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

drop table if exists tb_connect;

drop table if exists tb_query;

drop table if exists tb_user;

drop table if exists tb_email_log;

create table tb_connect
(
   id                   bigint not null comment '主键',
   host                 varchar(128) comment '地址',
   port                 integer comment '端口',
   username             varchar(20) comment '数据源用户名',
   password             varchar(20) comment '数据源密码',
   alias                varchar(100) comment '别名',
   create_time          datetime comment '创建时间',
   user_id              bigint comment '关联user表外键',
   primary key (id)
);

create table tb_query
(
   id                   bigint not null comment '主键',
   name                 varchar(100) comment '查询名',
   sqls                 varchar(10000) comment '相关sql语句',
   create_time          datetime comment '创建时间',
   connect_id           bigint comment '关联connect表外键',
   primary key (id)
);

create table tb_user
(
   id                   bigint not null comment '主键',
   name                 varchar(20) comment '用户名',
   password             varchar(200) comment '密码',
   setting             	varchar(4000) comment '设置',
   primary key (id)
);

create table tb_email_log
(
   id                   bigint not null comment '主键',
   email_time           datetime comment '激活请求时间',
   due_time             datetime comment '链接有效期到期时间',
   reset_time           datetime comment '激活发生时间',
   token                varchar(50) comment '此次重置密码的随机数token',
   available            boolean comment '链接是否可用。若用户使用此链接修改密码成功后便不再可用。',
   status               char comment '当为i时表示新增用户，当为u时表示更新用户；',
   temp_account         varchar(320) comment '用户账号，当此条记录为激活账号记录时才有需要',
   temp_password        varchar(64) comment '用户密码，当此条记录为激活账号记录时才有需要',
   account_id           bigint comment '外键，对应表user的主键；',
   primary key (id)
);