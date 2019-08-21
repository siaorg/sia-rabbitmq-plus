DROP TABLE IF EXISTS `sia_queue_message_info_history`;
CREATE TABLE `sia_queue_message_info_history` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `queue_name` varchar(100) NOT NULL,
  `un_consume_message_num` int(11) DEFAULT NULL,
  `publish_message_num` int(11) DEFAULT NULL,
  `deliver_message_num` int(11) DEFAULT NULL,
  `worktime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `index3` (`queue_name`),
  KEY `index4` (`worktime`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;