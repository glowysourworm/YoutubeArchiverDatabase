-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               12.0.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.11.0.7065
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for YoutubeArchiver
DROP DATABASE IF EXISTS `YoutubeArchiver`;
CREATE DATABASE IF NOT EXISTS `youtubearchiver` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci */;
USE `YoutubeArchiver`;

-- Dumping structure for table YoutubeArchiver.localcomment
DROP TABLE IF EXISTS `localcomment`;
CREATE TABLE IF NOT EXISTS `localcomment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` varchar(100) DEFAULT NULL,
  `channelId` varchar(100) DEFAULT NULL,
  `videoId` varchar(100) DEFAULT NULL,
  `commentId` varchar(100) DEFAULT NULL COMMENT 'This is implicitly a youtube comment id. The local comment is not, itself, a comment tree.',
  `dateCreated` datetime NOT NULL,
  `dateModified` datetime NOT NULL,
  `suspicious` bit(1) NOT NULL,
  `malicious` bit(1) NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `text` varchar(10000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `User_FK` (`userId`),
  KEY `Channel_FK` (`channelId`),
  KEY `Video_FK` (`videoId`),
  KEY `Comment_FK` (`commentId`),
  CONSTRAINT `Channel_FK` FOREIGN KEY (`channelId`) REFERENCES `youtubechannel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Comment_FK` FOREIGN KEY (`commentId`) REFERENCES `youtubecomment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `User_FK` FOREIGN KEY (`userId`) REFERENCES `youtubeuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Video_FK` FOREIGN KEY (`videoId`) REFERENCES `youtubevideo` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='Comment that relates to any of the associated youtube entities; and allows offline data to be stored for reference.';

-- Data exporting was unselected.

-- Dumping structure for table YoutubeArchiver.youtubechannel
DROP TABLE IF EXISTS `youtubechannel`;
CREATE TABLE IF NOT EXISTS `youtubechannel` (
  `id` varchar(100) NOT NULL,
  `name` varchar(500) NOT NULL,
  `imageURL` varchar(3000) DEFAULT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `hashValue` varchar(1000) NOT NULL COMMENT 'etag or computed hash value',
  `apiCreated` bit(1) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deletedOnline` bit(1) NOT NULL,
  `dateCreated` datetime NOT NULL,
  `dateModified` datetime NOT NULL,
  `dateDeletedOnline` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table YoutubeArchiver.youtubecomment
DROP TABLE IF EXISTS `youtubecomment`;
CREATE TABLE IF NOT EXISTS `youtubecomment` (
  `id` varchar(100) NOT NULL DEFAULT '',
  `parentId` varchar(100) DEFAULT NULL,
  `userId` varchar(100) NOT NULL,
  `channelId` varchar(100) NOT NULL,
  `videoId` varchar(100) NOT NULL DEFAULT '',
  `dateCreated` datetime NOT NULL,
  `dateModified` datetime NOT NULL,
  `dateDeletedOnline` datetime DEFAULT NULL,
  `apiCreated` bit(1) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deletedOnline` bit(1) NOT NULL,
  `hashValue` varchar(1000) NOT NULL,
  `text` varchar(10000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Comment_Comment_FK` (`parentId`),
  KEY `Comment_User_FK` (`userId`),
  KEY `Comment_Channel_FK` (`channelId`),
  KEY `Comment_Video_FK` (`videoId`),
  CONSTRAINT `Comment_Channel_FK` FOREIGN KEY (`channelId`) REFERENCES `youtubechannel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Comment_Comment_FK` FOREIGN KEY (`parentId`) REFERENCES `youtubecomment` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Comment_User_FK` FOREIGN KEY (`userId`) REFERENCES `youtubeuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `Comment_Video_FK` FOREIGN KEY (`videoId`) REFERENCES `youtubevideo` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='This table is a combination of youtube comment / comment thread fields; and data to be used locally. The youtube ETag field is a youtube fingerprint that also shows if the comment was modified. The rest of the foreign key relationships are for local tables. API created comments imply that the associated user was parsed out of the comment data from the browser; and the created date corresponds to the date that the comment was parsed.';

-- Data exporting was unselected.

-- Dumping structure for table YoutubeArchiver.youtubecommenthistorical
DROP TABLE IF EXISTS `youtubecommenthistorical`;
CREATE TABLE IF NOT EXISTS `youtubecommenthistorical` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commentId` varchar(100) NOT NULL,
  `dateCreated` datetime NOT NULL,
  `apiCreated` bit(1) NOT NULL,
  `hashValue` varchar(1000) NOT NULL COMMENT 'etag or computed hash value',
  `text` varchar(10000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table YoutubeArchiver.youtubeuser
DROP TABLE IF EXISTS `youtubeuser`;
CREATE TABLE IF NOT EXISTS `youtubeuser` (
  `id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `userIconURL` varchar(3000) DEFAULT NULL,
  `apiCreated` bit(1) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deletedOnline` bit(1) NOT NULL,
  `dateCreated` datetime NOT NULL,
  `dateModified` datetime NOT NULL,
  `dateDeletedOnline` datetime DEFAULT NULL,
  `hashValue` varchar(1000) NOT NULL DEFAULT '' COMMENT 'etag or computed application value',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci COMMENT='This is a local collection of user data based on return values from the youtube google api.';

-- Data exporting was unselected.

-- Dumping structure for table YoutubeArchiver.youtubeuserchannelmap
DROP TABLE IF EXISTS `youtubeuserchannelmap`;
CREATE TABLE IF NOT EXISTS `youtubeuserchannelmap` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` varchar(100) NOT NULL,
  `channelId` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `MAP_USER_FK` (`userId`),
  KEY `MAP_CHANNEL_FK` (`channelId`),
  CONSTRAINT `MAP_CHANNEL_FK` FOREIGN KEY (`channelId`) REFERENCES `youtubechannel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `MAP_USER_FK` FOREIGN KEY (`userId`) REFERENCES `youtubeuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table YoutubeArchiver.youtubevideo
DROP TABLE IF EXISTS `youtubevideo`;
CREATE TABLE IF NOT EXISTS `youtubevideo` (
  `id` varchar(100) NOT NULL,
  `channelId` varchar(100) NOT NULL,
  `title` varchar(1000) DEFAULT NULL,
  `imageURL` varchar(3000) DEFAULT NULL,
  `hashValue` varchar(1000) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `apiCreated` bit(1) NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deletedOnline` bit(1) NOT NULL,
  `dateCreated` datetime NOT NULL,
  `dateModified` datetime NOT NULL,
  `dateDeletedOnline` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `VIDEO_CHANNEL_FK` (`channelId`),
  CONSTRAINT `VIDEO_CHANNEL_FK` FOREIGN KEY (`channelId`) REFERENCES `youtubechannel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Data exporting was unselected.

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
