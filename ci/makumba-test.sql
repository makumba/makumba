-- MySQL dump 10.13  Distrib 5.7.24, for Linux (x86_64)
--
-- Host: localhost    Database: makumba
-- ------------------------------------------------------
-- Server version	5.7.24-0ubuntu0.16.04.1

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
-- Table structure for table `org_makumba_db_makumba_Catalog_`
--

DROP TABLE IF EXISTS `org_makumba_db_makumba_Catalog_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `org_makumba_db_makumba_Catalog_` (
  `Catalog_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Catalog_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `name_` (`name_`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_makumba_db_makumba_Catalog_`
--

LOCK TABLES `org_makumba_db_makumba_Catalog_` WRITE;
/*!40000 ALTER TABLE `org_makumba_db_makumba_Catalog_` DISABLE KEYS */;
/*!40000 ALTER TABLE `org_makumba_db_makumba_Catalog_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_makumba_db_makumba_Lock_`
--

DROP TABLE IF EXISTS `org_makumba_db_makumba_Lock_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `org_makumba_db_makumba_Lock_` (
  `Lock_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Lock_`),
  UNIQUE KEY `name_` (`name_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_makumba_db_makumba_Lock_`
--

LOCK TABLES `org_makumba_db_makumba_Lock_` WRITE;
/*!40000 ALTER TABLE `org_makumba_db_makumba_Lock_` DISABLE KEYS */;
/*!40000 ALTER TABLE `org_makumba_db_makumba_Lock_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Individual_`
--

DROP TABLE IF EXISTS `test_Individual_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Individual_` (
  `Individual_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `surname_` varchar(40) DEFAULT NULL,
  `name_` varchar(40) DEFAULT NULL,
  `person_` int(11) DEFAULT NULL,
  `someDate_` datetime DEFAULT NULL,
  `someusername_` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`Individual_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `surname_` (`surname_`),
  KEY `name_` (`name_`),
  KEY `person_` (`person_`),
  KEY `someDate_` (`someDate_`),
  KEY `someusername_` (`someusername_`),
  CONSTRAINT `test_Individual__ibfk_1` FOREIGN KEY (`person_`) REFERENCES `test_Person_` (`Person_`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Individual_`
--

LOCK TABLES `test_Individual_` WRITE;
/*!40000 ALTER TABLE `test_Individual_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Individual_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Language_`
--

DROP TABLE IF EXISTS `test_Language_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Language_` (
  `Language_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name_` varchar(10) DEFAULT NULL,
  `isoCode_` varchar(2) DEFAULT NULL,
  `family_` int(11) DEFAULT NULL,
  PRIMARY KEY (`Language_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `name_` (`name_`),
  KEY `isoCode_` (`isoCode_`),
  KEY `family_` (`family_`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Language_`
--

LOCK TABLES `test_Language_` WRITE;
/*!40000 ALTER TABLE `test_Language_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Language_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person_`
--

DROP TABLE IF EXISTS `test_Person_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person_` (
  `Person_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `indiv_` int(11) DEFAULT NULL,
  `birthdate_` datetime DEFAULT NULL,
  `firstSex_` datetime DEFAULT NULL,
  `myapp_username_` varchar(80) DEFAULT NULL,
  `password_` varchar(80) DEFAULT NULL,
  `beginDate_` datetime DEFAULT NULL,
  `gender_` int(11) DEFAULT NULL,
  `all_` varchar(25) DEFAULT NULL,
  `field_` varchar(80) DEFAULT NULL,
  `militarySucksAndEverybodyKnowsItButDoesNotSpeakOutLoudAboutIt_` int(11) DEFAULT NULL,
  `driver_` int(11) DEFAULT NULL,
  `age_` int(11) DEFAULT NULL,
  `makumbaTillDeath_` int(11) DEFAULT NULL,
  `designer_` int(11) DEFAULT NULL,
  `weight_` double DEFAULT NULL,
  `length_` int(11) DEFAULT NULL,
  `max_` int(11) DEFAULT NULL,
  `hobbies_` longtext,
  `comment_` longtext,
  `picture_` longblob,
  `someAttachment_` int(11) DEFAULT NULL,
  `brother_` int(11) DEFAULT NULL,
  `extraData_` int(11) DEFAULT NULL,
  `uniqInt_` int(11) DEFAULT NULL,
  `uniqChar_` varchar(33) DEFAULT NULL,
  `uniqDate_` datetime DEFAULT NULL,
  `uniqPtr_` int(11) DEFAULT NULL,
  `email_` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`Person_`),
  UNIQUE KEY `indiv_` (`indiv_`),
  UNIQUE KEY `uniqInt_` (`uniqInt_`),
  UNIQUE KEY `uniqChar_` (`uniqChar_`),
  UNIQUE KEY `uniqDate_` (`uniqDate_`),
  UNIQUE KEY `uniqPtr_` (`uniqPtr_`),
  UNIQUE KEY `age_email` (`age_`,`email_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `birthdate_` (`birthdate_`),
  KEY `firstSex_` (`firstSex_`),
  KEY `myapp_username_` (`myapp_username_`),
  KEY `password_` (`password_`),
  KEY `beginDate_` (`beginDate_`),
  KEY `gender_` (`gender_`),
  KEY `all_` (`all_`),
  KEY `field_` (`field_`),
  KEY `militarySucksAndEverybodyKnowsItButDoesNotSpeakOutLoudAboutIt_` (`militarySucksAndEverybodyKnowsItButDoesNotSpeakOutLoudAboutIt_`),
  KEY `driver_` (`driver_`),
  KEY `age_` (`age_`),
  KEY `makumbaTillDeath_` (`makumbaTillDeath_`),
  KEY `designer_` (`designer_`),
  KEY `weight_` (`weight_`),
  KEY `length_` (`length_`),
  KEY `max_` (`max_`),
  KEY `someAttachment_` (`someAttachment_`),
  KEY `brother_` (`brother_`),
  KEY `extraData_` (`extraData_`),
  KEY `email_` (`email_`),
  CONSTRAINT `test_Person__ibfk_1` FOREIGN KEY (`indiv_`) REFERENCES `test_Individual_` (`Individual_`),
  CONSTRAINT `test_Person__ibfk_2` FOREIGN KEY (`someAttachment_`) REFERENCES `test_Person__someAttachment_` (`someAttachment_`),
  CONSTRAINT `test_Person__ibfk_3` FOREIGN KEY (`brother_`) REFERENCES `test_Person_` (`Person_`),
  CONSTRAINT `test_Person__ibfk_4` FOREIGN KEY (`extraData_`) REFERENCES `test_Person__extraData_` (`extraData_`),
  CONSTRAINT `test_Person__ibfk_5` FOREIGN KEY (`uniqPtr_`) REFERENCES `test_Language_` (`Language_`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person_`
--

LOCK TABLES `test_Person_` WRITE;
/*!40000 ALTER TABLE `test_Person_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__address_`
--

DROP TABLE IF EXISTS `test_Person__address_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__address_` (
  `address_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `description_` varchar(30) DEFAULT NULL,
  `streetno_` varchar(100) DEFAULT NULL,
  `zipcode_` varchar(10) DEFAULT NULL,
  `city_` varchar(40) DEFAULT NULL,
  `country_` varchar(20) DEFAULT NULL,
  `phone_` varchar(20) DEFAULT NULL,
  `fax_` varchar(20) DEFAULT NULL,
  `email_` varchar(40) DEFAULT NULL,
  `usagestart_` datetime DEFAULT NULL,
  `usageend_` datetime DEFAULT NULL,
  `homepage_` varchar(50) DEFAULT NULL,
  `sth_` int(11) DEFAULT NULL,
  `Person_` int(11) DEFAULT NULL,
  PRIMARY KEY (`address_`),
  UNIQUE KEY `streetno_zipcode_city_country` (`streetno_`,`zipcode_`,`city_`,`country_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `description_` (`description_`),
  KEY `streetno_` (`streetno_`),
  KEY `zipcode_` (`zipcode_`),
  KEY `city_` (`city_`),
  KEY `country_` (`country_`),
  KEY `phone_` (`phone_`),
  KEY `fax_` (`fax_`),
  KEY `email_` (`email_`),
  KEY `usagestart_` (`usagestart_`),
  KEY `usageend_` (`usageend_`),
  KEY `homepage_` (`homepage_`),
  KEY `sth_` (`sth_`),
  KEY `Person_` (`Person_`),
  CONSTRAINT `test_Person__address__ibfk_1` FOREIGN KEY (`sth_`) REFERENCES `test_Person__address__sth_` (`sth_`),
  CONSTRAINT `test_Person__address__ibfk_2` FOREIGN KEY (`Person_`) REFERENCES `test_Person_` (`Person_`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__address_`
--

LOCK TABLES `test_Person__address_` WRITE;
/*!40000 ALTER TABLE `test_Person__address_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__address_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__address__languages_`
--

DROP TABLE IF EXISTS `test_Person__address__languages_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__address__languages_` (
  `languages_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `address_` int(11) DEFAULT NULL,
  `Language_` int(11) DEFAULT NULL,
  PRIMARY KEY (`languages_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `address_` (`address_`),
  KEY `Language_` (`Language_`),
  CONSTRAINT `test_Person__address__languages__ibfk_1` FOREIGN KEY (`address_`) REFERENCES `test_Person__address_` (`address_`),
  CONSTRAINT `test_Person__address__languages__ibfk_2` FOREIGN KEY (`Language_`) REFERENCES `test_Language_` (`Language_`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__address__languages_`
--

LOCK TABLES `test_Person__address__languages_` WRITE;
/*!40000 ALTER TABLE `test_Person__address__languages_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__address__languages_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__address__sth_`
--

DROP TABLE IF EXISTS `test_Person__address__sth_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__address__sth_` (
  `sth_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `aaa_` varchar(20) DEFAULT NULL,
  `bbb_` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`sth_`),
  UNIQUE KEY `aaa_bbb` (`aaa_`,`bbb_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `aaa_` (`aaa_`),
  KEY `bbb_` (`bbb_`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__address__sth_`
--

LOCK TABLES `test_Person__address__sth_` WRITE;
/*!40000 ALTER TABLE `test_Person__address__sth_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__address__sth_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__charSet_`
--

DROP TABLE IF EXISTS `test_Person__charSet_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__charSet_` (
  `charSet_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `Person_` int(11) DEFAULT NULL,
  `enum_` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`charSet_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `Person_` (`Person_`),
  KEY `enum_` (`enum_`),
  CONSTRAINT `test_Person__charSet__ibfk_1` FOREIGN KEY (`Person_`) REFERENCES `test_Person_` (`Person_`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__charSet_`
--

LOCK TABLES `test_Person__charSet_` WRITE;
/*!40000 ALTER TABLE `test_Person__charSet_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__charSet_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__extraData_`
--

DROP TABLE IF EXISTS `test_Person__extraData_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__extraData_` (
  `extraData_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `something_` varchar(20) DEFAULT NULL,
  `what_` int(11) DEFAULT NULL,
  PRIMARY KEY (`extraData_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `something_` (`something_`),
  KEY `what_` (`what_`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__extraData_`
--

LOCK TABLES `test_Person__extraData_` WRITE;
/*!40000 ALTER TABLE `test_Person__extraData_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__extraData_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__groupMembers_`
--

DROP TABLE IF EXISTS `test_Person__groupMembers_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__groupMembers_` (
  `groupMembers_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `Person_` int(11) DEFAULT NULL,
  `Person__` int(11) DEFAULT NULL,
  PRIMARY KEY (`groupMembers_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `Person_` (`Person_`),
  KEY `Person__` (`Person__`),
  CONSTRAINT `test_Person__groupMembers__ibfk_1` FOREIGN KEY (`Person_`) REFERENCES `test_Person_` (`Person_`),
  CONSTRAINT `test_Person__groupMembers__ibfk_2` FOREIGN KEY (`Person__`) REFERENCES `test_Person_` (`Person_`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__groupMembers_`
--

LOCK TABLES `test_Person__groupMembers_` WRITE;
/*!40000 ALTER TABLE `test_Person__groupMembers_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__groupMembers_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__intSet_`
--

DROP TABLE IF EXISTS `test_Person__intSet_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__intSet_` (
  `intSet_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `Person_` int(11) DEFAULT NULL,
  `enum_` int(11) DEFAULT NULL,
  PRIMARY KEY (`intSet_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `Person_` (`Person_`),
  KEY `enum_` (`enum_`),
  CONSTRAINT `test_Person__intSet__ibfk_1` FOREIGN KEY (`Person_`) REFERENCES `test_Person_` (`Person_`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__intSet_`
--

LOCK TABLES `test_Person__intSet_` WRITE;
/*!40000 ALTER TABLE `test_Person__intSet_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__intSet_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__someAttachment_`
--

DROP TABLE IF EXISTS `test_Person__someAttachment_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__someAttachment_` (
  `someAttachment_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `content_` longblob,
  `contentLength_` int(11) DEFAULT NULL,
  `contentType_` varchar(255) DEFAULT NULL,
  `originalName_` varchar(255) DEFAULT NULL,
  `name_` varchar(255) DEFAULT NULL,
  `imageWidth_` int(11) DEFAULT NULL,
  `imageHeight_` int(11) DEFAULT NULL,
  PRIMARY KEY (`someAttachment_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `contentLength_` (`contentLength_`),
  KEY `contentType_` (`contentType_`),
  KEY `originalName_` (`originalName_`),
  KEY `name_` (`name_`),
  KEY `imageWidth_` (`imageWidth_`),
  KEY `imageHeight_` (`imageHeight_`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__someAttachment_`
--

LOCK TABLES `test_Person__someAttachment_` WRITE;
/*!40000 ALTER TABLE `test_Person__someAttachment_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__someAttachment_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__speaks_`
--

DROP TABLE IF EXISTS `test_Person__speaks_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__speaks_` (
  `speaks_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `Person_` int(11) DEFAULT NULL,
  `Language_` int(11) DEFAULT NULL,
  PRIMARY KEY (`speaks_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `Person_` (`Person_`),
  KEY `Language_` (`Language_`),
  CONSTRAINT `test_Person__speaks__ibfk_1` FOREIGN KEY (`Person_`) REFERENCES `test_Person_` (`Person_`),
  CONSTRAINT `test_Person__speaks__ibfk_2` FOREIGN KEY (`Language_`) REFERENCES `test_Language_` (`Language_`)
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__speaks_`
--

LOCK TABLES `test_Person__speaks_` WRITE;
/*!40000 ALTER TABLE `test_Person__speaks_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__speaks_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_Person__toys_`
--

DROP TABLE IF EXISTS `test_Person__toys_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_Person__toys_` (
  `toys_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name_` varchar(30) DEFAULT NULL,
  `Person_` int(11) DEFAULT NULL,
  PRIMARY KEY (`toys_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `name_` (`name_`),
  KEY `Person_` (`Person_`),
  CONSTRAINT `test_Person__toys__ibfk_1` FOREIGN KEY (`Person_`) REFERENCES `test_Person_` (`Person_`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_Person__toys_`
--

LOCK TABLES `test_Person__toys_` WRITE;
/*!40000 ALTER TABLE `test_Person__toys_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_Person__toys_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_validMdds_Int_`
--

DROP TABLE IF EXISTS `test_validMdds_Int_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_validMdds_Int_` (
  `Int_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `i_` int(11) DEFAULT NULL,
  PRIMARY KEY (`Int_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `i_` (`i_`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_validMdds_Int_`
--

LOCK TABLES `test_validMdds_Int_` WRITE;
/*!40000 ALTER TABLE `test_validMdds_Int_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_validMdds_Int_` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_validMdds_Real_`
--

DROP TABLE IF EXISTS `test_validMdds_Real_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_validMdds_Real_` (
  `Real_` int(11) NOT NULL AUTO_INCREMENT,
  `TS_modify_` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TS_create_` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `r_` double DEFAULT NULL,
  PRIMARY KEY (`Real_`),
  KEY `TS_modify_` (`TS_modify_`),
  KEY `TS_create_` (`TS_create_`),
  KEY `r_` (`r_`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_validMdds_Real_`
--

LOCK TABLES `test_validMdds_Real_` WRITE;
/*!40000 ALTER TABLE `test_validMdds_Real_` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_validMdds_Real_` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-12-15  3:01:11
