# sia-rabbitmq-plus部署指南

## 一. mysql初始化
```sql
DROP TABLE IF EXISTS `skytrain_queue_message_info_history`;
CREATE TABLE `skytrain_queue_message_info_history` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `queue_name` varchar(100) NOT NULL, #队列名称
  `un_consume_message_num` int(11) DEFAULT NULL,#残留消息数量
  `publish_message_num` int(11) DEFAULT NULL,#发送消息数量
  `deliver_message_num` int(11) DEFAULT NULL,# 消费消息数量
  `worktime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `index3` (`queue_name`),
  KEY `index4` (`worktime`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

```
## 二. redis安装
reids的安装和配置详见官方文档,单机或集群模式都行

## 三. rabbitMQ 安装
rabbitMQ的安装和配置详见官方文档,单机或集群都行

##项目部署

1.环境要求 
系统：64bit OS，Linux/Mac/Windows 
IDE：推荐使用IntelliJ IDEA 或 Eclipse 
JDK：JDK1.6+

2.从SIA-RABBITMQ-PLUS工程下获取源代码打包，执行rabbitmq-plus-build-component目录的mvn命令即可。

* 在~/sia-task/sia-task-build-component目录下，执行如下命令打包：mvn clean install 。

* 打包成功后，会在~/sia-rabbitmq-plus/rabbitmq-plus-build-component 目录下出现target文件，target文件中的.zip文件即为项目安装包。

* 打开安装包所在文件夹，将安装包解压，得到task目录，其中包括四个子目录：


3.配置文件修改

将config文件夹下的sia-task-config工程的配置文件task_config_open.yml，以及sia-task-scheduler工程下的配置文件task_scheduler_open.yml中的zookeeper和Mysql的链接修改为自己的地址。

4.启动sia-rabbitmq-plus-heartbeat工程

sh sia-rabbitmq-plus-heartbeat.sh

5.启动sia-rabbitmq-plus-gather工程
sh sia-rabbitmq-plus-gather.sh

6.启动sia-rabbitmq-plus-display工程
sh sia-rabbitmq-plus-display.sh

7.启动sia-rabbitmq-plus-dome工程
sh sia-rabbitmq-plus-dome.sh

