-- MySQL dump 10.13  Distrib 5.1.57, for apple-darwin10.3.0 (i386)
--
-- Host: localhost    Database: cure_public
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
-- Table structure for table `annotation`
--

DROP TABLE IF EXISTS `annotation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `annotation` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `feature_id` int(11) NOT NULL,
  `vocabulary` varchar(50) DEFAULT NULL,
  `accession` varchar(50) DEFAULT NULL,
  `term` varchar(250) DEFAULT NULL,
  `evidence_type` varchar(100) DEFAULT NULL,
  `source` varchar(200) DEFAULT NULL,
  `created` date DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `f1` (`feature_id`,`accession`,`evidence_type`,`source`)
) ENGINE=MyISAM AUTO_INCREMENT=189692 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attribute`
--

DROP TABLE IF EXISTS `attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `col_index` int(11) NOT NULL,
  `name` varchar(30) DEFAULT NULL,
  `dataset` varchar(50) DEFAULT NULL,
  `reliefF` float DEFAULT NULL,
  `created` date DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `feature_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dataset_index_name` (`col_index`,`name`,`dataset`)
) ENGINE=MyISAM AUTO_INCREMENT=15521 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `board`
--

DROP TABLE IF EXISTS `board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `dataset` varchar(50) DEFAULT NULL,
  `room` varchar(25) DEFAULT NULL,
  `n_players` int(11) DEFAULT NULL,
  `n_wins` int(11) DEFAULT NULL,
  `avg_score` float DEFAULT NULL,
  `max_score` float DEFAULT NULL,
  `base_score` float DEFAULT NULL,
  `created` date DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1555 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `board_feature`
--

DROP TABLE IF EXISTS `board_feature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board_feature` (
  `board_id` int(11) DEFAULT NULL,
  `feature_id` int(11) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `card`
--

DROP TABLE IF EXISTS `card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `card` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `board_id` int(11) DEFAULT NULL,
  `unique_id` varchar(50) DEFAULT NULL,
  `display_loc` int(11) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=31522 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feature`
--

DROP TABLE IF EXISTS `feature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feature` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(50) NOT NULL,
  `short_name` varchar(30) DEFAULT NULL,
  `long_name` varchar(250) DEFAULT NULL,
  `description` text,
  `created` date DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=43191 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `board_id` int(11) DEFAULT NULL,
  `ip` varchar(30) DEFAULT NULL,
  `player1_id` int(11) DEFAULT NULL,
  `player2_id` int(11) DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `game_started` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `game_finished` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `p1_score` int(11) DEFAULT NULL,
  `p2_score` int(11) DEFAULT NULL,
  `win` int(11) DEFAULT NULL,
  `created` date DEFAULT NULL,
  `search_term` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `g1` (`board_id`,`player1_id`,`game_finished`)
) ENGINE=InnoDB AUTO_INCREMENT=7991 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_card_ux`
--

DROP TABLE IF EXISTS `game_card_ux`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_card_ux` (
  `game_id` int(11) DEFAULT NULL,
  `feature_id` int(11) DEFAULT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `panel` varchar(50) DEFAULT NULL,
  `board_hover` tinyint(1) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_mouse_action`
--

DROP TABLE IF EXISTS `game_mouse_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_mouse_action` (
  `game_id` int(11) DEFAULT NULL,
  `x` int(11) DEFAULT NULL,
  `y` int(11) DEFAULT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_player_feature`
--

DROP TABLE IF EXISTS `game_player_feature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_player_feature` (
  `game_id` int(11) DEFAULT NULL,
  `player_id` int(11) DEFAULT NULL,
  `feature_id` int(11) DEFAULT NULL,
  UNIQUE KEY `gpf1` (`game_id`,`player_id`,`feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
  `password` varchar(50) DEFAULT NULL,
  `ip` varchar(25) DEFAULT NULL,
  `top_score` int(11) DEFAULT NULL,
  `games_played` int(11) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `created` date DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `degree` varchar(12) DEFAULT NULL,
  `cancer` varchar(5) DEFAULT NULL,
  `biologist` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `a1` (`name`,`password`)
) ENGINE=MyISAM AUTO_INCREMENT=410 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `text_annotation`
--

DROP TABLE IF EXISTS `text_annotation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `text_annotation` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `feature_id` int(11) NOT NULL,
  `source` varchar(50) DEFAULT NULL,
  `pubmed_id` int(11) DEFAULT NULL,
  `anno_text` text,
  `created` date DEFAULT NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `f_index` (`feature_id`)
) ENGINE=MyISAM AUTO_INCREMENT=375603 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-02-19  9:53:14
