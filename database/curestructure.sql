-- MySQL dump 10.13  Distrib 5.1.57, for apple-darwin10.3.0 (i386)
--
-- Host: localhost    Database: cure
-- ------------------------------------------------------
-- Server version	5.1.57

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
-- Table structure for table `board`
--

DROP TABLE IF EXISTS `board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `phenotype` varchar(30) DEFAULT NULL,
  `entrez_ids` varchar(300) DEFAULT NULL,
  `gene_symbols` varchar(500) DEFAULT NULL,
  `n_players` int(11) DEFAULT NULL,
  `n_wins` int(11) DEFAULT NULL,
  `average_score` float DEFAULT NULL,
  `max_score` float DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `attribute_names` text,
  `base_score` float DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1077 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `card`
--

DROP TABLE IF EXISTS `card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `card` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `username` varchar(25) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `phenotype` varchar(50) DEFAULT NULL,
  `board_id` int(11) DEFAULT NULL,
  `att_index` int(11) DEFAULT NULL,
  `att_name` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `geneid` varchar(50) DEFAULT NULL,
  `power` float DEFAULT NULL,
  `display_loc` int(11) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1567 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hand`
--

DROP TABLE IF EXISTS `hand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hand` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `player_name` varchar(50) DEFAULT NULL,
  `ip` varchar(25) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `cv_accuracy` int(11) DEFAULT NULL,
  `features` varchar(100) DEFAULT NULL,
  `board_id` int(11) DEFAULT NULL,
  `phenotype` varchar(25) DEFAULT NULL,
  `feature_names` varchar(500) DEFAULT NULL,
  `training_accuracy` int(11) DEFAULT NULL,
  `game_type` varchar(50) DEFAULT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `win` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1205 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `ip` varchar(25) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `top_score` int(11) DEFAULT NULL,
  `games_played` int(11) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `barney_levels` varchar(50) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `degree` varchar(12) DEFAULT NULL,
  `cancer` varchar(5) DEFAULT NULL,
  `biologist` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `a1` (`name`,`password`)
) ENGINE=MyISAM AUTO_INCREMENT=74 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


-- Dump completed on 2012-09-10  9:30:23
