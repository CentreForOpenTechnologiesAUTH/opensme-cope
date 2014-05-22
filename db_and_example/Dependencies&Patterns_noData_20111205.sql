-- MySQL dump 10.13  Distrib 5.1.54, for debian-linux-gnu (i686)
--
-- Host: localhost    Database: dependencies
-- ------------------------------------------------------
-- Server version	5.1.54-1ubuntu4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `dependencies`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `dependencies` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `dependencies`;

--
-- Table structure for table `classes`
--

DROP TABLE IF EXISTS `classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `classes` (
  `classid` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `sources` varchar(200) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `innerclass` tinyint(1) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `usedby` bigint(20) unsigned DEFAULT NULL,
  `usesinternal` bigint(20) unsigned DEFAULT NULL,
  `usesexternal` bigint(20) unsigned DEFAULT NULL,
  `layer` bigint(20) unsigned DEFAULT NULL,
  `WMC` int(11) DEFAULT NULL,
  `DIT` int(11) DEFAULT NULL,
  `NOC` int(11) DEFAULT NULL,
  `CBO` int(11) DEFAULT NULL,
  `RFC` int(11) DEFAULT NULL,
  `LCOM` int(11) DEFAULT NULL,
  `Ca` int(11) DEFAULT NULL,
  `NPM` int(11) DEFAULT NULL,
  `projectid` bigint(20) NOT NULL,
  `packageid` bigint(20) DEFAULT NULL,
  `ClusterSize` int(11) DEFAULT NULL,
  PRIMARY KEY (`classid`),
  KEY `projectid` (`projectid`),
  KEY `classname` (`name`),
  KEY `projectidfk` (`projectid`),
  KEY `packagesid` (`packageid`),
  CONSTRAINT `packagesid` FOREIGN KEY (`packageid`) REFERENCES `packages` (`packageid`),
  CONSTRAINT `projectidfk` FOREIGN KEY (`projectid`) REFERENCES `projects` (`projectid`)
) ENGINE=InnoDB AUTO_INCREMENT=6961 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `classinternaldependencies`
--

DROP TABLE IF EXISTS `classinternaldependencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `classinternaldependencies` (
  `dependee` bigint(20) NOT NULL,
  `dependency` bigint(20) NOT NULL,
  PRIMARY KEY (`dependee`,`dependency`),
  KEY `classid` (`dependee`,`dependency`),
  KEY `classidfk` (`dependee`,`dependency`),
  KEY `classidfk1` (`dependency`),
  CONSTRAINT `classidfk` FOREIGN KEY (`dependee`) REFERENCES `classes` (`classid`),
  CONSTRAINT `classidfk1` FOREIGN KEY (`dependency`) REFERENCES `classes` (`classid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dithierarchy`
--

DROP TABLE IF EXISTS `dithierarchy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dithierarchy` (
  `superclass` varchar(200) NOT NULL,
  `subclass` varchar(200) NOT NULL,
  `projectid` bigint(20) NOT NULL,
  KEY `projectid` (`projectid`),
  CONSTRAINT `projectid` FOREIGN KEY (`projectid`) REFERENCES `projects` (`projectid`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `frequencies`
--

DROP TABLE IF EXISTS `frequencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frequencies` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dependee` bigint(20) DEFAULT NULL,
  `dependend` bigint(20) DEFAULT NULL,
  `frequency` int(11) DEFAULT NULL,
  `dependeeLayer` bigint(20) DEFAULT NULL,
  `dependendLayer` bigint(20) DEFAULT NULL,
  `dependendPackageId` bigint(20) DEFAULT NULL,
  `dependeePackageId` bigint(20) DEFAULT NULL,
  `projectId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logentries`
--

DROP TABLE IF EXISTS `logentries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logentries` (
  `logentryid` bigint(20) NOT NULL AUTO_INCREMENT,
  `author` varchar(100) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `revision` bigint(20) DEFAULT NULL,
  `message` text,
  `logid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`logentryid`),
  KEY `logid` (`logid`),
  KEY `logidfk` (`logid`),
  KEY `logidfk1` (`logid`),
  CONSTRAINT `logidfk1` FOREIGN KEY (`logid`) REFERENCES `logs` (`logid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logs`
--

DROP TABLE IF EXISTS `logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logs` (
  `logid` bigint(20) NOT NULL AUTO_INCREMENT,
  `projectid` bigint(20) NOT NULL,
  PRIMARY KEY (`logid`),
  KEY `projectid` (`projectid`),
  KEY `projectidfk` (`projectid`),
  KEY `projectidfk1` (`projectid`),
  CONSTRAINT `projectidfk1` FOREIGN KEY (`projectid`) REFERENCES `projects` (`projectid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `packageinternaldependencies`
--

DROP TABLE IF EXISTS `packageinternaldependencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `packageinternaldependencies` (
  `dependee` bigint(20) NOT NULL,
  `dependency` bigint(20) NOT NULL,
  PRIMARY KEY (`dependee`,`dependency`),
  KEY `dependeefk` (`dependee`),
  KEY `dependencyfk` (`dependency`),
  CONSTRAINT `dependeefk` FOREIGN KEY (`dependee`) REFERENCES `packages` (`packageid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `dependencyfk` FOREIGN KEY (`dependency`) REFERENCES `packages` (`packageid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `packages`
--

DROP TABLE IF EXISTS `packages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `packages` (
  `packageid` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `sources` varchar(200) DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `usedBy` int(11) DEFAULT NULL,
  `usesInternal` int(11) DEFAULT NULL,
  `usesExternal` int(11) DEFAULT NULL,
  `layer` int(11) DEFAULT NULL,
  `projectid` bigint(20) NOT NULL,
  PRIMARY KEY (`packageid`),
  KEY `packageprojectidfk` (`projectid`),
  CONSTRAINT `packageprojectidfk` FOREIGN KEY (`projectid`) REFERENCES `projects` (`projectid`)
) ENGINE=InnoDB AUTO_INCREMENT=536 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paths`
--

DROP TABLE IF EXISTS `paths`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paths` (
  `pathid` bigint(20) NOT NULL AUTO_INCREMENT,
  `kind` varchar(100) DEFAULT NULL,
  `action` varchar(5) DEFAULT NULL,
  `path` varchar(500) DEFAULT NULL,
  `logentryid` bigint(20) DEFAULT NULL,
  `isPathAMonitoredClass` tinyint(1) DEFAULT NULL,
  `classid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`pathid`),
  KEY `logentryid` (`logentryid`),
  KEY `classid` (`classid`),
  KEY `logentryidfk` (`logentryid`),
  KEY `logentryidfk1` (`logentryid`),
  KEY `classidfk2` (`classid`),
  CONSTRAINT `classidfk2` FOREIGN KEY (`classid`) REFERENCES `classes` (`classid`),
  CONSTRAINT `logentryidfk1` FOREIGN KEY (`logentryid`) REFERENCES `logentries` (`logentryid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patterninstance`
--

DROP TABLE IF EXISTS `patterninstance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patterninstance` (
  `patternId` int(11) NOT NULL,
  `patternName` varchar(100) NOT NULL,
  `patternInstanceID` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`patternInstanceID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patternlist`
--

DROP TABLE IF EXISTS `patternlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patternlist` (
  `projectID` int(11) NOT NULL,
  `patternID` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`patternID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patternparticipant`
--

DROP TABLE IF EXISTS `patternparticipant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patternparticipant` (
  `patternParticiapntId` int(11) NOT NULL,
  `role` varchar(100) NOT NULL,
  `class` varchar(100) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projects` (
  `projectid` bigint(20) NOT NULL AUTO_INCREMENT,
  `projecttitle` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`projectid`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-10-30 20:50:03
