-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: nasi_bergizi_pajak
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE IF NOT EXISTS `nasi_bergizi_pajak` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `nasi_bergizi_pajak`;

-- Pre-create parent table so child tables with FK to user_account can be created.
CREATE TABLE IF NOT EXISTS `user_account` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `signup_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `profile_image_name` varchar(255) DEFAULT NULL,
  `tipe_admin` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_user_email` (`email`),
  CONSTRAINT `chk_user_active` CHECK ((`active` in (0,1))),
  CONSTRAINT `chk_user_tipe_admin` CHECK ((`tipe_admin` in (0,1)))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `budget`
--

DROP TABLE IF EXISTS `budget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `budget` (
  `budget_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'active',
  PRIMARY KEY (`budget_id`),
  KEY `idx_budget_user_id` (`user_id`),
  CONSTRAINT `fk_budget_user` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_budget_amount` CHECK ((`amount` >= 0)),
  CONSTRAINT `chk_budget_period` CHECK ((`period_end` >= `period_start`))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `budget`
--

LOCK TABLES `budget` WRITE;
/*!40000 ALTER TABLE `budget` DISABLE KEYS */;
INSERT INTO `budget` VALUES (1,1,'Monthly Budget 1',4319404.26,'2026-04-26','2026-05-26','active'),(2,2,'Monthly Budget 2',3174485.60,'2026-04-29','2026-05-29','inactive'),(3,3,'Monthly Budget 3',3022404.25,'2026-04-25','2026-05-25','inactive'),(4,4,'Monthly Budget 4',1969934.50,'2026-04-12','2026-05-12','inactive'),(5,5,'Monthly Budget 5',2692660.29,'2026-04-30','2026-05-30','active'),(6,6,'Monthly Budget 6',3433388.71,'2026-04-15','2026-05-15','active'),(7,7,'Monthly Budget 7',3440380.81,'2026-04-18','2026-05-18','inactive'),(8,8,'Monthly Budget 8',3866133.79,'2026-04-16','2026-05-16','inactive'),(9,9,'Monthly Budget 9',646605.65,'2026-04-29','2026-05-29','inactive'),(10,10,'Monthly Budget 10',874973.00,'2026-04-07','2026-05-07','active'),(11,1,'Zero Budget',0.00,'2026-05-05','2026-05-12','active');
/*!40000 ALTER TABLE `budget` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `family_member`
--

DROP TABLE IF EXISTS `family_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_member` (
  `member_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `birth_date` date DEFAULT NULL,
  `height` double DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `allergy` text,
  PRIMARY KEY (`member_id`),
  KEY `idx_family_member_user_id` (`user_id`),
  CONSTRAINT `fk_fm_user` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `family_member`
--

LOCK TABLES `family_member` WRITE;
/*!40000 ALTER TABLE `family_member` DISABLE KEYS */;
INSERT INTO `family_member` VALUES (1,1,'Prayitna','2008-05-31',90.31,35.59,'Egg'),(2,2,'Gawati','1977-04-24',141.74,88.76,'Egg'),(3,3,'Oskar','2023-12-11',104.25,74.82,NULL),(4,3,'Niyaga','1993-01-21',163.47,27.56,'Milk'),(5,3,'Yunita','2003-04-19',117.43,27.1,'Peanut'),(6,3,'Dwi','2009-09-12',91.24,51.79,'Peanut'),(7,4,'Dasa','1998-01-28',146.41,98.78,'Milk'),(8,4,'Widya','1982-03-26',138.99,117.04,'Milk'),(9,4,'Asmuni','2005-10-07',88.67,42.25,'Egg'),(10,5,'Darmana','1980-05-11',143.51,87.5,NULL),(11,5,'Icha','2022-05-09',152.74,95.04,NULL),(12,5,'Rusman','2008-01-20',174.08,105.31,'Milk'),(13,6,'Uli','1989-06-27',129.88,101.75,'Seafood'),(14,6,'Setya','1960-05-26',120.72,33.05,'Peanut'),(15,6,'Marsudi','2009-02-04',157.2,85.19,NULL),(16,7,'Prayogo','1961-11-29',138.76,36.93,'Milk'),(17,7,'Darimin','1966-12-04',121.74,118.85,'Egg'),(18,8,'Imam','1989-11-19',155.31,102.71,NULL),(19,8,'Jarwa','1991-11-06',105.2,13.53,'Peanut'),(20,9,'Mulyanto','1956-05-22',109.45,33.21,'Egg'),(21,9,'Jayadi','2013-12-25',176.4,44.61,'Milk'),(22,9,'Emil','1972-04-03',123.52,110.6,'Milk'),(23,9,'Kajen','1972-04-30',95.72,25.36,'Egg'),(24,10,'Jarwi','2011-08-18',162.17,57.13,'Egg'),(25,10,'Rangga','1965-04-17',123.93,34.13,'Seafood'),(26,10,'Dadi','1988-11-05',136.05,20,NULL),(27,1,'Baby','2026-05-05',50,3.5,'Milk');
/*!40000 ALTER TABLE `family_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingredient`
--

DROP TABLE IF EXISTS `ingredient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient` (
  `ingredient_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `unit` varchar(50) NOT NULL,
  PRIMARY KEY (`ingredient_id`),
  UNIQUE KEY `uq_ingredient_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingredient`
--

LOCK TABLES `ingredient` WRITE;
/*!40000 ALTER TABLE `ingredient` DISABLE KEYS */;
INSERT INTO `ingredient` VALUES (1,'Beras','ml'),(2,'Telur','kg'),(3,'Ayam','kg'),(4,'Bayam','liter'),(5,'Wortel','kg'),(6,'Bawang Merah','ml'),(7,'Bawang Putih','gram'),(8,'Minyak Goreng','gram'),(9,'Susu','liter'),(10,'Tempe','ml'),(11,'Tahu','gram'),(12,'Cabai','pcs'),(13,'Garam','ml'),(14,'Gula','ml'),(15,'Tomat','liter'),(16,'Air Kosong','ml');
/*!40000 ALTER TABLE `ingredient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingredient_nutrition`
--

DROP TABLE IF EXISTS `ingredient_nutrition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient_nutrition` (
  `nutrition_id` int NOT NULL AUTO_INCREMENT,
  `ingredient_id` int NOT NULL,
  `calories` decimal(10,2) NOT NULL DEFAULT '0.00',
  `protein` decimal(10,2) NOT NULL DEFAULT '0.00',
  `carbohydrate` decimal(10,2) NOT NULL DEFAULT '0.00',
  `fat` decimal(10,2) NOT NULL DEFAULT '0.00',
  `fibre` decimal(10,2) NOT NULL DEFAULT '0.00',
  `unit` varchar(50) NOT NULL,
  PRIMARY KEY (`nutrition_id`),
  UNIQUE KEY `uq_nutrition_ingredient` (`ingredient_id`),
  CONSTRAINT `fk_in_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`ingredient_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_in_calories` CHECK ((`calories` >= 0)),
  CONSTRAINT `chk_in_carbohydrate` CHECK ((`carbohydrate` >= 0)),
  CONSTRAINT `chk_in_fat` CHECK ((`fat` >= 0)),
  CONSTRAINT `chk_in_fibre` CHECK ((`fibre` >= 0)),
  CONSTRAINT `chk_in_protein` CHECK ((`protein` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingredient_nutrition`
--

LOCK TABLES `ingredient_nutrition` WRITE;
/*!40000 ALTER TABLE `ingredient_nutrition` DISABLE KEYS */;
INSERT INTO `ingredient_nutrition` VALUES (1,1,482.18,46.45,75.53,27.59,21.39,'100g'),(2,2,199.50,33.58,37.34,35.98,13.54,'100g'),(3,3,123.95,3.20,2.10,22.16,17.65,'100g'),(4,4,3.60,35.39,5.89,2.70,0.94,'100g'),(5,5,165.21,25.71,27.85,19.42,16.18,'100g'),(6,6,361.68,44.12,57.62,9.72,14.19,'100g'),(7,7,203.53,4.72,65.90,14.17,12.33,'100g'),(8,8,431.92,2.71,65.35,25.85,1.82,'100g'),(9,9,364.11,40.03,10.93,7.66,16.09,'100g'),(10,10,70.09,9.17,46.26,34.98,2.26,'100g'),(11,11,404.01,42.80,9.79,26.09,16.22,'100g'),(12,12,7.38,4.66,75.36,9.46,12.19,'100g'),(13,13,240.68,43.23,90.24,6.59,0.06,'100g'),(14,14,195.21,46.33,78.51,11.41,20.90,'100g'),(15,15,365.25,39.17,66.19,19.47,5.70,'100g'),(16,16,0.00,0.00,0.00,0.00,0.00,'100g');
/*!40000 ALTER TABLE `ingredient_nutrition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingredient_price`
--

DROP TABLE IF EXISTS `ingredient_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient_price` (
  `price_id` int NOT NULL AUTO_INCREMENT,
  `ingredient_id` int NOT NULL,
  `price` decimal(15,2) NOT NULL,
  `effective_date` date NOT NULL,
  PRIMARY KEY (`price_id`),
  KEY `idx_ingredient_price_ingredient_id` (`ingredient_id`),
  CONSTRAINT `fk_ip_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`ingredient_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_ip_price` CHECK ((`price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingredient_price`
--

LOCK TABLES `ingredient_price` WRITE;
/*!40000 ALTER TABLE `ingredient_price` DISABLE KEYS */;
INSERT INTO `ingredient_price` VALUES (1,1,22552.39,'2025-08-16'),(2,1,6789.85,'2025-09-18'),(3,1,73837.98,'2026-04-01'),(4,2,7034.80,'2025-05-06'),(5,2,32046.88,'2026-02-05'),(6,2,5964.09,'2026-02-06'),(7,3,48202.06,'2026-03-07'),(8,3,92019.33,'2025-09-17'),(9,3,53581.49,'2025-11-08'),(10,4,6631.08,'2025-08-13'),(11,4,51275.02,'2025-05-24'),(12,4,85282.91,'2025-05-09'),(13,5,7783.61,'2026-05-02'),(14,5,7728.13,'2026-04-10'),(15,5,86320.10,'2025-05-14'),(16,6,40973.78,'2025-06-22'),(17,6,94218.02,'2025-11-07'),(18,6,57397.87,'2026-01-20'),(19,7,58312.99,'2026-03-11'),(20,7,4934.42,'2026-02-08'),(21,7,9116.36,'2025-07-01'),(22,8,66079.94,'2025-11-01'),(23,8,56957.47,'2025-12-20'),(24,8,32320.61,'2026-01-27'),(25,9,26815.80,'2025-11-11'),(26,9,67302.87,'2025-09-08'),(27,9,32104.20,'2025-09-15'),(28,10,27295.84,'2026-01-14'),(29,10,13956.95,'2026-05-04'),(30,10,64904.58,'2025-12-24'),(31,11,46265.23,'2026-04-02'),(32,11,92972.81,'2025-08-25'),(33,11,93637.69,'2025-08-19'),(34,12,1922.23,'2025-08-08'),(35,12,62494.77,'2025-08-12'),(36,12,56736.37,'2025-06-01'),(37,13,10898.14,'2025-08-27'),(38,13,54225.81,'2026-04-18'),(39,13,51082.58,'2025-06-25'),(40,14,14113.20,'2025-11-20'),(41,14,35551.87,'2026-03-18'),(42,14,7810.34,'2025-10-21'),(43,15,25184.16,'2026-03-13'),(44,15,29213.75,'2026-03-20'),(45,15,44380.27,'2025-07-11');
/*!40000 ALTER TABLE `ingredient_price` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kitchen_stock`
--

DROP TABLE IF EXISTS `kitchen_stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kitchen_stock` (
  `stock_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `ingredient_id` int NOT NULL,
  `quantity` double NOT NULL DEFAULT '0',
  `unit` varchar(50) NOT NULL,
  `storage_location` varchar(100) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  PRIMARY KEY (`stock_id`),
  KEY `idx_kitchen_stock_user_id` (`user_id`),
  KEY `idx_kitchen_stock_ingredient_id` (`ingredient_id`),
  CONSTRAINT `fk_ks_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`ingredient_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ks_user` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_ks_quantity` CHECK ((`quantity` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kitchen_stock`
--

LOCK TABLES `kitchen_stock` WRITE;
/*!40000 ALTER TABLE `kitchen_stock` DISABLE KEYS */;
INSERT INTO `kitchen_stock` VALUES (1,1,9,10.58,'ml','Pantry','2026-04-26'),(2,1,12,18.64,'kg','Freezer','2026-06-03'),(3,1,5,5.29,'kg','Freezer','2026-05-03'),(4,1,10,5.45,'ml','Freezer','2026-05-01'),(5,1,11,14.35,'gram','Pantry','2026-05-04'),(6,2,9,12.69,'pcs','Fridge','2026-05-21'),(7,2,8,0.07,'gram','Pantry','2026-06-01'),(8,2,5,3.23,'liter','Cabinet','2026-05-26'),(9,2,1,11.22,'kg','Fridge','2026-05-26'),(10,2,2,18.9,'gram','Fridge','2026-05-26'),(11,3,14,8.6,'kg','Pantry','2026-05-15'),(12,3,6,7.29,'kg','Pantry','2026-05-29'),(13,3,10,4.2,'gram','Fridge','2026-05-20'),(14,3,9,7.07,'ml','Cabinet','2026-04-28'),(15,3,3,19.48,'gram','Freezer','2026-05-16'),(16,4,14,0.5,'pcs','Cabinet','2026-05-09'),(17,4,3,16.04,'gram','Pantry','2026-05-25'),(18,4,13,3.18,'kg','Cabinet','2026-05-27'),(19,4,15,17.44,'liter','Freezer','2026-05-16'),(20,4,7,3.99,'liter','Pantry','2026-05-01'),(21,5,5,0.47,'gram','Cabinet','2026-05-29'),(22,5,14,6.57,'kg','Pantry','2026-04-27'),(23,5,13,7.02,'ml','Cabinet','2026-05-02'),(24,5,4,13.59,'ml','Pantry','2026-05-06'),(25,5,12,18.78,'kg','Pantry','2026-05-19'),(26,6,3,11.93,'pcs','Pantry','2026-04-30'),(27,6,10,8.73,'ml','Fridge','2026-05-18'),(28,6,5,7.7,'ml','Freezer','2026-05-10'),(29,6,1,5.09,'liter','Fridge','2026-05-31'),(30,6,2,10.4,'ml','Freezer','2026-05-10'),(31,7,6,12.46,'kg','Pantry','2026-05-27'),(32,7,7,10.14,'liter','Pantry','2026-05-12'),(33,7,2,8.05,'pcs','Freezer','2026-05-02'),(34,7,11,3.84,'liter','Freezer','2026-05-02'),(35,7,15,12.31,'pcs','Cabinet','2026-05-12'),(36,8,9,4.2,'ml','Pantry','2026-05-24'),(37,8,14,9.3,'liter','Freezer','2026-05-31'),(38,8,1,10.22,'gram','Fridge','2026-05-26'),(39,8,5,5.68,'ml','Pantry','2026-05-08'),(40,8,12,1.87,'gram','Pantry','2026-05-24'),(41,9,4,0.92,'liter','Fridge','2026-05-07'),(42,9,13,9.11,'ml','Freezer','2026-05-02'),(43,9,15,14.37,'liter','Cabinet','2026-05-02'),(44,9,3,7.99,'gram','Fridge','2026-05-07'),(45,9,1,17.85,'kg','Cabinet','2026-05-20'),(46,10,4,9.29,'ml','Freezer','2026-05-27'),(47,10,3,18.35,'kg','Cabinet','2026-05-09'),(48,10,13,2.67,'liter','Pantry','2026-05-07'),(49,10,12,19,'liter','Cabinet','2026-05-03'),(50,10,9,16.61,'ml','Cabinet','2026-05-01'),(51,1,1,1,'kg','Fridge','2026-04-30');
/*!40000 ALTER TABLE `kitchen_stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `meal_slot`
--

DROP TABLE IF EXISTS `meal_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meal_slot` (
  `slot_id` int NOT NULL AUTO_INCREMENT,
  `menu_id` int NOT NULL,
  `recipe_id` int DEFAULT NULL,
  `meal_date` date NOT NULL,
  `meal_time` varchar(50) NOT NULL,
  `is_eating_out` tinyint(1) NOT NULL DEFAULT '0',
  `outside_cost` decimal(15,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`slot_id`),
  KEY `idx_meal_slot_menu_id` (`menu_id`),
  KEY `idx_meal_slot_recipe_id` (`recipe_id`),
  CONSTRAINT `chk_ms_is_eating_out` CHECK ((`is_eating_out` in (0,1))),
  CONSTRAINT `chk_ms_outside_cost` CHECK ((`outside_cost` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `meal_slot`
--

LOCK TABLES `meal_slot` WRITE;
/*!40000 ALTER TABLE `meal_slot` DISABLE KEYS */;
INSERT INTO `meal_slot` VALUES (1,1,6,'2026-05-03','lunch',1,124609.65),(2,1,5,'2026-05-05','snack',0,0.00),(3,1,8,'2026-05-06','snack',0,0.00),(4,1,1,'2026-04-29','snack',1,127642.93),(5,1,8,'2026-05-10','lunch',0,0.00),(6,1,5,'2026-05-10','dinner',1,41948.77),(7,1,9,'2026-05-04','breakfast',1,77494.09),(8,2,2,'2026-05-10','lunch',0,0.00),(9,2,8,'2026-04-30','lunch',1,103591.40),(10,2,8,'2026-04-29','breakfast',1,13958.00),(11,2,7,'2026-05-06','lunch',0,0.00),(12,2,10,'2026-05-05','dinner',1,70989.07),(13,2,7,'2026-04-29','dinner',1,52771.58),(14,2,5,'2026-05-01','dinner',1,37710.55),(15,3,4,'2026-05-11','dinner',0,0.00),(16,3,9,'2026-05-09','lunch',0,0.00),(17,3,4,'2026-05-07','snack',0,0.00),(18,3,10,'2026-04-30','dinner',1,147076.59),(19,3,5,'2026-05-05','lunch',0,0.00),(20,3,3,'2026-05-02','dinner',1,2122.26),(21,3,5,'2026-05-11','breakfast',0,0.00),(22,4,9,'2026-04-30','dinner',0,0.00),(23,4,8,'2026-05-07','breakfast',0,0.00),(24,4,10,'2026-05-01','dinner',0,0.00),(25,4,8,'2026-05-07','snack',1,51105.89),(26,4,5,'2026-05-07','snack',0,0.00),(27,4,2,'2026-05-09','snack',0,0.00),(28,4,2,'2026-05-02','breakfast',1,22759.05),(29,5,2,'2026-04-28','lunch',1,17767.73),(30,5,10,'2026-05-06','lunch',1,116354.03),(31,5,8,'2026-05-08','snack',1,44602.97),(32,5,5,'2026-05-02','breakfast',1,91444.64),(33,5,4,'2026-05-07','lunch',0,0.00),(34,5,2,'2026-05-04','lunch',1,35979.78),(35,5,3,'2026-04-29','breakfast',0,0.00),(36,6,8,'2026-05-05','snack',1,43688.89),(37,6,5,'2026-04-28','dinner',0,0.00),(38,6,2,'2026-05-01','lunch',1,138586.66),(39,6,7,'2026-05-05','breakfast',0,0.00),(40,6,3,'2026-05-06','dinner',0,0.00),(41,6,2,'2026-04-28','breakfast',0,0.00),(42,6,5,'2026-05-01','dinner',0,0.00),(43,7,2,'2026-05-03','snack',1,103301.70),(44,7,5,'2026-04-28','snack',1,65663.83),(45,7,7,'2026-04-30','dinner',0,0.00),(46,7,1,'2026-05-10','breakfast',1,34338.66),(47,7,5,'2026-05-09','breakfast',0,0.00),(48,7,8,'2026-05-11','snack',0,0.00),(49,7,3,'2026-05-11','snack',1,95217.71),(50,8,2,'2026-05-04','snack',1,52194.16),(51,8,6,'2026-05-08','breakfast',1,128659.96),(52,8,7,'2026-05-02','snack',1,43232.67),(53,8,9,'2026-05-06','breakfast',1,68223.89),(54,8,5,'2026-04-29','dinner',1,17388.24),(55,8,9,'2026-05-11','breakfast',1,98649.06),(56,8,7,'2026-05-06','breakfast',1,28137.38),(57,9,10,'2026-04-28','snack',1,93811.31),(58,9,4,'2026-05-02','dinner',0,0.00),(59,9,5,'2026-05-07','snack',0,0.00),(60,9,2,'2026-05-11','breakfast',1,145972.02),(61,9,3,'2026-04-28','dinner',0,0.00),(62,9,9,'2026-05-02','snack',0,0.00),(63,9,4,'2026-05-04','breakfast',0,0.00),(64,10,2,'2026-04-29','lunch',1,74756.33),(65,10,9,'2026-05-01','dinner',1,62323.55),(66,10,8,'2026-05-10','lunch',1,68517.97),(67,10,7,'2026-05-07','lunch',0,0.00),(68,10,2,'2026-04-28','dinner',0,0.00),(69,10,6,'2026-05-08','dinner',1,123082.42),(70,10,5,'2026-05-09','snack',1,129789.09);
/*!40000 ALTER TABLE `meal_slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parameter_planner`
--

DROP TABLE IF EXISTS `parameter_planner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parameter_planner` (
  `parameter_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `budget_id` int NOT NULL,
  `shopping_period_start` date NOT NULL,
  `shopping_period_end` date NOT NULL,
  `meals_per_day` int NOT NULL,
  `snack_per_day` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`parameter_id`),
  KEY `idx_parameter_user_id` (`user_id`),
  KEY `idx_parameter_budget_id` (`budget_id`),
  CONSTRAINT `fk_pp_budget` FOREIGN KEY (`budget_id`) REFERENCES `budget` (`budget_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pp_user` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_pp_meals_per_day` CHECK ((`meals_per_day` > 0)),
  CONSTRAINT `chk_pp_period` CHECK ((`shopping_period_end` >= `shopping_period_start`)),
  CONSTRAINT `chk_pp_snack_per_day` CHECK ((`snack_per_day` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parameter_planner`
--

LOCK TABLES `parameter_planner` WRITE;
/*!40000 ALTER TABLE `parameter_planner` DISABLE KEYS */;
INSERT INTO `parameter_planner` VALUES (1,1,1,'2026-05-04','2026-05-11',3,1),(2,2,2,'2026-04-21','2026-04-28',5,0),(3,3,3,'2026-04-23','2026-04-30',3,1),(4,4,4,'2026-05-02','2026-05-09',2,2),(5,5,5,'2026-04-26','2026-05-03',2,0),(6,6,6,'2026-04-30','2026-05-07',5,2),(7,7,7,'2026-05-04','2026-05-11',4,0),(8,8,8,'2026-04-28','2026-05-05',1,2),(9,9,9,'2026-04-23','2026-04-30',3,1),(10,10,10,'2026-04-30','2026-05-07',1,1);
/*!40000 ALTER TABLE `parameter_planner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recipe`
--

DROP TABLE IF EXISTS `recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recipe` (
  `recipe_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `description` text,
  `serving_size` int NOT NULL DEFAULT '1',
  `status` varchar(50) NOT NULL DEFAULT 'active',
  PRIMARY KEY (`recipe_id`),
  UNIQUE KEY `uq_recipe_name` (`name`),
  CONSTRAINT `chk_recipe_serving_size` CHECK ((`serving_size` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recipe`
--

LOCK TABLES `recipe` WRITE;
/*!40000 ALTER TABLE `recipe` DISABLE KEYS */;
INSERT INTO `recipe` VALUES (1,'Nasi Goreng','Fuga temporibus laboriosam doloribus. Quidem temporibus ipsum sint eius doloremque. Expedita excepturi sint nobis tempora inventore magni recusandae.',3,'inactive'),(2,'Ayam Bakar','Consectetur soluta ut rem vitae possimus. Vitae pariatur animi.',8,'inactive'),(3,'Sayur Bayam','A hic adipisci quibusdam vitae dolore nesciunt ea. Omnis cum eligendi iure minima.',4,'inactive'),(4,'Tumis Tempe','Dolorem ducimus quibusdam veniam earum veniam.',8,'active'),(5,'Sup Ayam','Sit atque vitae soluta ratione quo quia.',5,'inactive'),(6,'Omelette','Suscipit quibusdam est consectetur quam voluptas perferendis accusamus. Excepturi a ea magnam.',2,'inactive'),(7,'Tahu Goreng','Accusantium error ea eveniet vel rem. Officiis harum minus quae. Sed blanditiis consectetur quidem sequi.',4,'inactive'),(8,'Capcay','Necessitatibus consequuntur odit voluptate eaque. Dolor quos animi ea nemo.',6,'inactive'),(9,'Sop Wortel','Qui adipisci excepturi nisi quidem. Quas nostrum ullam deleniti. Voluptas deserunt sequi cupiditate eligendi porro.',2,'active'),(10,'Tempe Orek','Voluptatem ullam eveniet suscipit. Expedita nesciunt impedit maiores. Eius quia optio sequi autem iusto officiis dolorum. Ea porro doloremque labore provident.',3,'active'),(11,'Extreme Portion Meal','Huge serving meal',100,'active');
/*!40000 ALTER TABLE `recipe` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recipe_ingredient`
--

DROP TABLE IF EXISTS `recipe_ingredient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recipe_ingredient` (
  `recipe_ingredient_id` int NOT NULL AUTO_INCREMENT,
  `recipe_id` int NOT NULL,
  `ingredient_id` int NOT NULL,
  `amount` double NOT NULL,
  `unit` varchar(50) NOT NULL,
  PRIMARY KEY (`recipe_ingredient_id`),
  UNIQUE KEY `uq_ri_recipe_ingredient` (`recipe_id`,`ingredient_id`),
  KEY `idx_recipe_ingredient_recipe_id` (`recipe_id`),
  KEY `idx_recipe_ingredient_ingredient_id` (`ingredient_id`),
  CONSTRAINT `fk_ri_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`ingredient_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ri_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `recipe` (`recipe_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_ri_amount` CHECK ((`amount` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recipe_ingredient`
--

LOCK TABLES `recipe_ingredient` WRITE;
/*!40000 ALTER TABLE `recipe_ingredient` DISABLE KEYS */;
INSERT INTO `recipe_ingredient` VALUES (1,1,7,1.15,'liter'),(2,1,12,2.1,'ml'),(3,1,3,2.38,'kg'),(4,1,14,1.11,'liter'),(5,2,7,0.2,'ml'),(6,2,13,1.96,'kg'),(7,2,10,4.72,'pcs'),(8,2,12,3.79,'liter'),(9,3,9,4.02,'gram'),(10,3,12,2.49,'pcs'),(11,3,14,2.24,'kg'),(12,3,15,2.01,'liter'),(13,4,12,4.91,'ml'),(14,4,3,0.23,'liter'),(15,4,8,3,'kg'),(16,4,14,0.51,'liter'),(17,5,3,0.35,'liter'),(18,5,14,1.7,'liter'),(19,5,8,1.7,'liter'),(20,5,15,1.46,'liter'),(21,6,5,0.19,'ml'),(22,6,14,0.36,'pcs'),(23,6,2,1.2,'kg'),(24,6,8,3.93,'kg'),(25,7,13,4.21,'ml'),(26,7,1,0.85,'gram'),(27,7,4,2.42,'kg'),(28,7,15,2.86,'gram'),(29,8,8,0.92,'ml'),(30,8,12,4.82,'kg'),(31,8,5,3.91,'gram'),(32,8,6,4.83,'kg'),(33,9,10,3.42,'liter'),(34,9,1,2.04,'gram'),(35,9,5,0.47,'gram'),(36,9,15,0.6,'pcs'),(37,10,14,4,'ml'),(38,10,11,3.93,'pcs'),(39,10,10,2.71,'pcs'),(40,10,2,0.44,'pcs');
/*!40000 ALTER TABLE `recipe_ingredient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_planner`
--

DROP TABLE IF EXISTS `shopping_planner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_planner` (
  `planner_id` int NOT NULL AUTO_INCREMENT,
  `menu_id` int NOT NULL,
  `created_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `total_estimation` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total_actual` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status` varchar(50) NOT NULL DEFAULT 'draft',
  PRIMARY KEY (`planner_id`),
  KEY `idx_shopping_planner_menu_id` (`menu_id`),
  CONSTRAINT `chk_sp_total_actual` CHECK ((`total_actual` >= 0)),
  CONSTRAINT `chk_sp_total_estimation` CHECK ((`total_estimation` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_planner`
--

LOCK TABLES `shopping_planner` WRITE;
/*!40000 ALTER TABLE `shopping_planner` DISABLE KEYS */;
INSERT INTO `shopping_planner` VALUES (1,1,'2026-03-23 01:23:18',452081.92,489497.97,'completed'),(2,2,'2026-04-12 14:16:17',556398.24,547995.76,'completed'),(3,3,'2026-01-29 20:59:29',933523.73,870905.67,'draft'),(4,4,'2026-04-22 10:00:12',576079.67,241226.82,'completed'),(5,5,'2026-02-26 05:47:29',53210.83,747258.10,'completed'),(6,6,'2026-03-21 06:00:51',392217.96,667051.94,'draft'),(7,7,'2026-01-01 23:46:17',500375.36,46649.42,'completed'),(8,8,'2026-03-14 04:43:06',871003.31,875448.47,'completed'),(9,9,'2026-02-16 13:23:03',108714.58,911337.26,'draft'),(10,10,'2026-03-05 05:57:23',725219.39,415878.83,'draft');
/*!40000 ALTER TABLE `shopping_planner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_planner_item`
--

DROP TABLE IF EXISTS `shopping_planner_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_planner_item` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `planner_id` int NOT NULL,
  `ingredient_id` int NOT NULL,
  `required_qty` double NOT NULL,
  `unit` varchar(50) NOT NULL,
  `estimated_price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `actual_price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status_beli` varchar(50) NOT NULL DEFAULT 'belum',
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `uq_spi_planner_ingredient` (`planner_id`,`ingredient_id`),
  KEY `idx_shopping_item_planner_id` (`planner_id`),
  KEY `idx_shopping_item_ingredient_id` (`ingredient_id`),
  CONSTRAINT `fk_spi_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`ingredient_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_spi_planner` FOREIGN KEY (`planner_id`) REFERENCES `shopping_planner` (`planner_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_spi_actual_price` CHECK ((`actual_price` >= 0)),
  CONSTRAINT `chk_spi_estimated_price` CHECK ((`estimated_price` >= 0)),
  CONSTRAINT `chk_spi_required_qty` CHECK ((`required_qty` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_planner_item`
--

LOCK TABLES `shopping_planner_item` WRITE;
/*!40000 ALTER TABLE `shopping_planner_item` DISABLE KEYS */;
INSERT INTO `shopping_planner_item` VALUES (1,1,2,4.04,'kg',31539.71,39316.19,'sudah'),(2,1,8,6.31,'liter',47752.84,55838.59,'belum'),(3,1,13,2.42,'pcs',31255.51,41216.90,'belum'),(4,1,5,1.07,'kg',22264.60,23919.78,'sudah'),(5,1,6,0.56,'kg',45267.78,44668.30,'sudah'),(6,2,7,7.95,'gram',28730.48,26356.52,'sudah'),(7,2,3,2.48,'ml',31365.81,28512.58,'sudah'),(8,2,4,4.65,'kg',32257.33,40740.96,'sudah'),(9,2,9,6.81,'gram',45159.26,41267.19,'sudah'),(10,2,15,3.06,'liter',49394.93,54749.63,'sudah'),(11,3,14,3.88,'pcs',6226.01,9848.19,'sudah'),(12,3,5,8.32,'liter',2075.04,1192.25,'belum'),(13,3,4,7.48,'pcs',45620.23,52256.34,'belum'),(14,3,7,3.59,'gram',30746.18,35059.55,'belum'),(15,3,8,9.04,'kg',31784.16,31418.15,'sudah'),(16,4,1,3.02,'liter',5416.54,3051.14,'belum'),(17,4,10,8.78,'pcs',39539.13,42501.92,'sudah'),(18,4,6,2.64,'liter',41693.16,51203.92,'sudah'),(19,4,12,3.45,'kg',37581.41,39606.38,'belum'),(20,4,3,9.67,'liter',7897.09,17406.14,'sudah'),(21,5,2,4.6,'pcs',27292.04,31060.44,'sudah'),(22,5,13,2.41,'kg',6308.96,10602.16,'sudah'),(23,5,7,2.29,'kg',45847.54,50378.60,'sudah'),(24,5,1,3.09,'liter',45559.99,42310.13,'belum'),(25,5,5,3.11,'liter',47366.82,44108.63,'belum'),(26,6,15,9.5,'ml',19179.56,20466.46,'belum'),(27,6,9,6.58,'liter',44385.28,48620.06,'sudah'),(28,6,3,3.77,'liter',2603.10,4273.54,'belum'),(29,6,7,1.08,'pcs',42917.06,46084.05,'sudah'),(30,6,8,2.83,'kg',3966.95,13202.11,'sudah'),(31,7,2,0.31,'pcs',30260.15,28913.68,'belum'),(32,7,11,2.13,'ml',39561.93,37669.29,'belum'),(33,7,4,2.41,'gram',40839.60,47669.26,'belum'),(34,7,14,9.84,'gram',14586.47,17689.70,'belum'),(35,7,12,8.68,'gram',6386.39,1609.29,'belum'),(36,8,10,7.44,'ml',3567.55,272.29,'belum'),(37,8,6,7.8,'ml',24336.05,28241.04,'sudah'),(38,8,1,9.46,'kg',25688.10,31596.74,'sudah'),(39,8,3,9.64,'kg',23444.08,33422.88,'sudah'),(40,8,5,1.17,'liter',21887.41,17989.77,'belum'),(41,9,6,6.37,'ml',14475.78,20158.07,'sudah'),(42,9,10,5.35,'liter',49736.76,52320.03,'sudah'),(43,9,3,7.05,'ml',5859.74,11672.34,'belum'),(44,9,2,8.89,'liter',22073.43,22157.40,'sudah'),(45,9,13,7.32,'pcs',20537.78,21939.24,'sudah'),(46,10,6,0.95,'liter',5470.01,1918.48,'sudah'),(47,10,3,5.61,'ml',40776.69,50126.06,'sudah'),(48,10,11,4.17,'liter',33830.65,41852.03,'belum'),(49,10,8,6.04,'pcs',48451.53,45005.54,'belum'),(50,10,2,4.87,'kg',8581.78,8833.49,'sudah');
/*!40000 ALTER TABLE `shopping_planner_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_account`
--

-- user_account is pre-created at the top because other tables reference it.
-- DROP TABLE IF EXISTS `user_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user_account` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `signup_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `profile_image_name` varchar(255) DEFAULT NULL,
  `tipe_admin` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_user_email` (`email`),
  CONSTRAINT `chk_user_active` CHECK ((`active` in (0,1))),
  CONSTRAINT `chk_user_tipe_admin` CHECK ((`tipe_admin` in (0,1)))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_account`
--

LOCK TABLES `user_account` WRITE;
/*!40000 ALTER TABLE `user_account` DISABLE KEYS */;
INSERT IGNORE INTO `user_account` VALUES (1,'user0@example.com','9662760ee4f93f2d023d30ec9d7b8a7de899064b6a76568f213aeb005199d29e','Natalia','Aryani',0,'2025-04-10 20:12:50','profile_0.jpg',1),(2,'user1@example.com','d5f96ef8432f56d800f3091d7fd9ecd88d71df7a0fdbdeff06de2e72594bf298','Asman','Fujiati',0,'2023-06-02 06:07:39','profile_1.jpg',0),(3,'user2@example.com','72bd3a665f6b3defff84daac757840ebb73165258a0cd832717a4fff5a8552d7','Tasdik','Manullang',1,'2022-03-27 20:30:13','profile_2.jpg',0),(4,'user3@example.com','4b3ec25d3aec834fc0a91b14648270712d6d270b48cdebe096e4e5a31a22b153','Paiman','Adriansyah',0,'2023-01-10 11:18:50','profile_3.jpg',0),(5,'user4@example.com','7cbbde34ec0e0eea0a76d1b69e13f72a21378baf4e0a4ff7bb2139d788ff5494','Anom','Purwanti',0,'2024-08-04 00:54:30','profile_4.jpg',0),(6,'user5@example.com','6613a9caec6c9d56d2509abebd419483f283424deccd41175604156da15a78d9','Nasim','Maryati',0,'2022-12-29 00:37:01','profile_5.jpg',0),(7,'user6@example.com','39f58f608d9471a92b2a3eb88a66236a17c9f0b4e5d1fdd8814f1d1020b55b58','Kiandra','Hutagalung',0,'2021-05-11 06:00:09','profile_6.jpg',0),(8,'user7@example.com','6c14f3607da78f1a45b7f755711eb865dce2bd53b53b21a0523ceab9dbbdde2a','Gangsa','Tarihoran',0,'2020-11-25 01:03:31','profile_7.jpg',0),(9,'user8@example.com','2a09347c0b3974c5a55757dd7842d359365718ac767479a0c75f4bb7c36064cb','Shakila','Simbolon',1,'2026-04-11 21:58:26','profile_8.jpg',0),(10,'user9@example.com','e298ddbdd76ad331d72c9373e93bb7758d7843bcbab6e0c9aefad314043a1ee2','Kairav','Usamah',0,'2022-06-20 21:21:43','profile_9.jpg',0),(11,'edgecase@example.com','123','A',NULL,1,'2026-05-05 23:21:50',NULL,0);
/*!40000 ALTER TABLE `user_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weekly_menu`
--

DROP TABLE IF EXISTS `weekly_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_menu` (
  `menu_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `parameter_id` int NOT NULL,
  `week_start_date` date NOT NULL,
  `week_end_date` date NOT NULL,
  `total_estimation` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status_budget` varchar(50) NOT NULL DEFAULT 'draft',
  `created_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`menu_id`),
  KEY `idx_weekly_menu_user_id` (`user_id`),
  KEY `idx_weekly_menu_parameter_id` (`parameter_id`),
  CONSTRAINT `fk_wm_parameter` FOREIGN KEY (`parameter_id`) REFERENCES `parameter_planner` (`parameter_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wm_user` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_wm_total_estimation` CHECK ((`total_estimation` >= 0)),
  CONSTRAINT `chk_wm_week_dates` CHECK ((`week_end_date` >= `week_start_date`))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weekly_menu`
--

LOCK TABLES `weekly_menu` WRITE;
/*!40000 ALTER TABLE `weekly_menu` DISABLE KEYS */;
INSERT INTO `weekly_menu` VALUES (1,1,1,'2026-05-04','2026-05-10',62019.54,'completed','2026-05-05 23:21:50'),(2,2,2,'2026-05-11','2026-05-17',831214.37,'draft','2026-05-05 23:21:50'),(3,3,3,'2026-04-28','2026-05-04',461847.23,'completed','2026-05-05 23:21:50'),(4,4,4,'2026-05-08','2026-05-14',653805.85,'completed','2026-05-05 23:21:50'),(5,5,5,'2026-05-11','2026-05-17',721943.21,'completed','2026-05-05 23:21:50'),(6,6,6,'2026-04-28','2026-05-04',217330.11,'overbudget','2026-05-05 23:21:50'),(7,7,7,'2026-05-10','2026-05-16',967200.04,'completed','2026-05-05 23:21:50'),(8,8,8,'2026-04-28','2026-05-04',635116.74,'overbudget','2026-05-05 23:21:50'),(9,9,9,'2026-05-02','2026-05-08',785909.38,'completed','2026-05-05 23:21:50'),(10,10,10,'2026-05-04','2026-05-10',463795.77,'overbudget','2026-05-05 23:21:50');
/*!40000 ALTER TABLE `weekly_menu` ENABLE KEYS */;
UNLOCK TABLES;

ALTER TABLE `meal_slot`
  ADD CONSTRAINT `fk_ms_menu` FOREIGN KEY (`menu_id`) REFERENCES `weekly_menu` (`menu_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_ms_recipe` FOREIGN KEY (`recipe_id`) REFERENCES `recipe` (`recipe_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `shopping_planner`
  ADD CONSTRAINT `fk_sp_menu` FOREIGN KEY (`menu_id`) REFERENCES `weekly_menu` (`menu_id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-05 23:22:41

-- UC09: Mendukung slot makan di luar tanpa recipe_id
ALTER TABLE `meal_slot`
MODIFY COLUMN `recipe_id` INT NULL;

UPDATE `meal_slot`
SET `recipe_id` = NULL
WHERE `is_eating_out` = 1;

ALTER TABLE `meal_slot`
DROP FOREIGN KEY `fk_ms_recipe`;

ALTER TABLE `meal_slot`
ADD CONSTRAINT `fk_ms_recipe`
FOREIGN KEY (`recipe_id`) REFERENCES `recipe` (`recipe_id`);

ALTER TABLE `meal_slot`
ADD CONSTRAINT `chk_ms_recipe_or_eating_out`
CHECK (
    (`is_eating_out` = 0 AND `recipe_id` IS NOT NULL)
    OR
    (`is_eating_out` = 1 AND `recipe_id` IS NULL)
);