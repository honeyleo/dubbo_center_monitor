CREATE TABLE `DUBBO_APP_MINUTE` (
  `ID_` bigint(20) NOT NULL AUTO_INCREMENT,
  `APP_NAME` varchar(100) NOT NULL,
  `TIME` datetime NOT NULL,
  `SUCCESS_TIMES` bigint(20) NOT NULL,
  `FAIL_TIMES` bigint(20) NOT NULL,
  `ELAPSED_AVG` bigint(20) NOT NULL,
  `ELAPSED_MAX` int(11) NOT NULL,
  PRIMARY KEY (`ID_`),
  KEY `DUBBO_APP_MINUTE_IDX1` (`APP_NAME`,`TIME`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=790323 DEFAULT CHARSET=utf8;

CREATE TABLE `T_ALT_ALERT` (
  `ID_` bigint(20) NOT NULL AUTO_INCREMENT,
  `TARGET` varchar(255) DEFAULT NULL,
  `TARGET_EXCLUDE` varchar(255) DEFAULT NULL,
  `CONDITION_IDS` varchar(255) DEFAULT NULL,
  `CONDITION_PARAM` varchar(255) DEFAULT NULL,
  `ACTION_IDS` varchar(255) DEFAULT NULL,
  `ACTION_PARAM` varchar(255) DEFAULT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `ENABLED` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE `T_ALT_CONDITION` (
  `ID_` bigint(20) NOT NULL AUTO_INCREMENT,
  `CONTENT_TYPE` varchar(255) DEFAULT NULL,
  `CONTENT` varchar(255) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `ENABLED` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE `T_ALT_ACTION` (
  `ID_` bigint(20) NOT NULL AUTO_INCREMENT,
  `CONTENT_TYPE` varchar(255) DEFAULT NULL,
  `CONTENT` varchar(255) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `ENABLED` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE `T_ALT_RECORD` (
  `ID_` bigint(20) NOT NULL AUTO_INCREMENT,
  `APP_NAME` varchar(255) DEFAULT NULL,
  `SERVICE` varchar(255) DEFAULT NULL,
  `METHOD` varchar(255) DEFAULT NULL,
  `CONSUMER_IP` varchar(255) DEFAULT NULL,
  `PROVIDER_IP` varchar(255) DEFAULT NULL,
  `ALERT_MSG` text,
  `INSERT_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
