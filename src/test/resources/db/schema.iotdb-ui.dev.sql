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

drop table if exists tb_alert;

drop table if exists tb_board;

drop table if exists tb_exporter;

drop table if exists tb_panel;

drop table if exists tb_trigger;

drop table if exists tb_task;

create table tb_connect
(
   id                   bigint not null comment '主键',
   host                 varchar(128) comment '地址',
   port                 integer comment '端口',
   username             varchar(20) comment '数据源用户名',
   password             varchar(20) comment '数据源密码',
   alias                varchar(100) comment '别名',
   create_time          datetime comment '创建时间',
   setting              varchar(4000) comment '参数设置',
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
   email           		varchar(100) comment '邮箱',
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

create table tb_alert
(
   id                   bigint not null comment '主键',
   code                 varchar(50) comment '来源相同的alert的code保持不变',
   version              int comment '版本，每次部署后再编辑时增加一条新的alert数据，其version自增，status由“已部署”变为“开发中”',
   origin               bigint comment '来源，创建alert时origin等于id，编辑新版本时origin保持不变',
   create_time          datetime comment '创建时间',
   update_time          datetime comment '修改时间',
   status               char(1) comment '状态（0开发中1已部署）',
   token                varchar(50),
   user_id              bigint,
   rule                 text comment '模型',
   primary key (id)
);

create table tb_board
(
   id                   bigint not null comment '主键',
   name                 varchar(20) comment '名称',
   setting              varchar(4000) comment '设置',
   token                char(100),
   create_time          datetime,
   update_time          datetime,
   user_id              bigint,
   primary key (id)
);

create table tb_exporter
(
   id                   bigint not null comment '主键',
   end_point            varchar(500) comment 'exporter端点',
   name                 varchar(100) comment '名称',
   code                 varchar(500) comment '业务编码',
   period               int comment '读取周期，单位秒',
   create_time          datetime,
   update_time          datetime,
   user_id              bigint,
   primary key (id)
);

create table tb_panel
(
   id                   bigint not null comment '主键',
   name                 varchar(100) comment '名称',
   query                varchar(10000) comment '查询sql',
   period				int comment '刷新周期，单位秒',
   display_order        int comment '显示顺序',
   setting              varchar(1000),
   create_time          datetime,
   update_time          datetime,
   board_id             bigint,
   user_id              bigint,
   primary key (id)
);

create table tb_trigger
(
   id                   bigint not null comment '主键',
   name                 varchar(200) comment '名称',
   timeseries           varchar(5000) comment '时间序列',
   status               char(1) comment '状态（0删除1启用2禁用）',
   create_time          datetime comment '建立时间',
   update_time          datetime,
   user_id              bigint,
   alert_id             bigint,
   primary key (id)
);

create table tb_task
(
   id                   bigint not null comment '主键',
   user_id              bigint,
   type                 char(1) comment '类型',
   setting              varchar(5000) comment '参数设置',
   start_window_from    datetime comment '时间窗口起始时间',
   priority             integer comment '优先级',
   status               char(1) comment '状态（0未开始1进行中2正常结束3异常结束4强制结束）',
   result_rows          bigint comment '结果行数',
   create_time          datetime,
   update_time          datetime,
   name                 varchar(100) comment '名称',
   start_time           datetime comment '任务开始时间',
   end_time             datetime comment '任务结束时间',
   time_cost            integer comment '任务用时（秒）',
   flag                 char(1) comment '长期（l）或一次性（o）',
   expression           varchar(100) comment '长期任务使用的cron表达式',
   long_term_task_id    bigint comment '当长期任务触发条件生成一次性任务时，记录长期任务的主键',
   primary key (id)
);