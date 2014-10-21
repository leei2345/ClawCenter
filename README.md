ClawCenter
==========

house data claw

first create databases and tables

CREATE TABLE `tb_clawer_mornitor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `identidy` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `date` varchar(8) COLLATE utf8_unicode_ci NOT NULL,
  `start_time` varchar(6) COLLATE utf8_unicode_ci NOT NULL,
  `avg_time` bigint(16) NOT NULL,
  `succ_percent` float(4,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `identidy_date` (`identidy`,`date`)
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `tb_plot` (
  `id` int(12) NOT NULL AUTO_INCREMENT,
  `plot` varchar(36) COLLATE utf8_unicode_ci NOT NULL COMMENT 'å°åŒºå',
  `area` varchar(8) COLLATE utf8_unicode_ci NOT NULL COMMENT 'åœ°åŒºå',
  `district` varchar(36) COLLATE utf8_unicode_ci NOT NULL COMMENT 'å•†åœˆ',
  `yx` varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'ç»çº¬åº¦',
  PRIMARY KEY (`id`),
  UNIQUE KEY `plot` (`plot`)
) ENGINE=InnoDB AUTO_INCREMENT=10380 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `tb_plot_unmatch` (
  `id` int(12) NOT NULL AUTO_INCREMENT,
  `plot` varchar(48) COLLATE utf8_unicode_ci NOT NULL COMMENT 'å°åŒºå',
  `area` varchar(8) COLLATE utf8_unicode_ci NOT NULL COMMENT 'åœ°åŒºå',
  `district` varchar(12) COLLATE utf8_unicode_ci NOT NULL COMMENT 'å•†åœˆ',
  `yx` varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'ç»çº¬åº¦',
  `status` tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼Œ0æœªå®¡æ ¸ï¼Œ1ä»¥å®¡æ ¸',
  `source_url` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `plot` (`plot`)
) ENGINE=InnoDB AUTO_INCREMENT=5423 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `tb_proxy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `host` varchar(15) COLLATE utf8_unicode_ci NOT NULL,
  `port` int(5) NOT NULL,
  `web_wuba` int(1) unsigned zerofill DEFAULT '0',
  `web_anjuke` int(1) DEFAULT NULL,
  `web_soufang` int(1) DEFAULT NULL,
  `avail` int(5) unsigned zerofill DEFAULT '00000',
  `unavail` int(5) unsigned zerofill DEFAULT '00000',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `host_port` (`host`,`port`)
) ENGINE=InnoDB AUTO_INCREMENT=1376 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `tb_scheduled_conf` (
  `identidy` varchar(12) COLLATE utf8_unicode_ci NOT NULL,
  `clazz` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `conf` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `status` tinyint(4) unsigned zerofill NOT NULL,
  PRIMARY KEY (`identidy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

