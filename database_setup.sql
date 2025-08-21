-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: pahanaedu
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bill_items`
--

DROP TABLE IF EXISTS `bill_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_items` (
  `bill_item_id` int NOT NULL AUTO_INCREMENT,
  `bill_id` int NOT NULL,
  `item_code` varchar(10) NOT NULL,
  `item_name` varchar(255) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `line_total` decimal(12,2) NOT NULL,
  PRIMARY KEY (`bill_item_id`),
  KEY `idx_bill_id` (`bill_id`),
  KEY `idx_item_code` (`item_code`),
  CONSTRAINT `fk_bill_items_bill` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`bill_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bill_items_item` FOREIGN KEY (`item_code`) REFERENCES `items` (`item_code`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill_items`
--

LOCK TABLES `bill_items` WRITE;
/*!40000 ALTER TABLE `bill_items` DISABLE KEYS */;
INSERT INTO `bill_items` VALUES (19,25,'ITM0027','English Literature Textbook Grade 11',750.00,1,750.00),(20,26,'ITM0083','Atlas 120pgs Excercise book',1200.00,10,12000.00),(21,27,'ITM0005','Faber-Castell Colour Pencils (24pk)',1200.00,1,1200.00),(22,28,'ITM0024','Thumb Tacks (Box of 100)',150.00,1,150.00),(23,29,'ITM0003','Casio Scientific Calculator fx-82MS',1850.00,1,1850.00),(24,30,'ITM0008','Ballpoint Pen (Blue) - Dozen',360.00,2,720.00),(25,31,'ITM0003','Casio Scientific Calculator fx-82MS',1850.00,1,1850.00),(26,32,'ITM0031','Sinhala Madhya Lipi Potha',450.00,1,450.00),(27,33,'ITM0015','Stapler No. 35',480.00,1,480.00),(28,34,'ITM0001','Atlas Pencil (HB) - 12pack',240.00,1,240.00),(29,35,'ITM0018','Transparent Tape 1 inch',60.00,1,60.00),(30,36,'ITM0007','Olasa Highlighters (Assorted 4pk)',350.00,1,350.00),(31,37,'ITM0009','Ballpoint Pen (Red) - Dozen',360.00,1,360.00),(46,45,'ITM0002','Kokuyo Long Notebook 80p (Single Line)',320.00,1,320.00),(47,45,'ITM0080','Blu-Tack (50g)',300.00,1,300.00),(48,45,'ITM0066','Maths Set Square (45 Degree)',70.00,2,140.00),(49,45,'ITM0027','English Literature Textbook Grade 11',750.00,1,750.00),(50,46,'ITM0081','Atlas 80pgs Excercise CR Book',240.00,1,240.00),(51,47,'ITM0001','Atlas Pencil (HB) - 12pack',240.00,1,240.00),(52,48,'ITM0001','Atlas Pencil (HB) - 12pack',240.00,1,240.00);
/*!40000 ALTER TABLE `bill_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `invoice_number` varchar(20) NOT NULL,
  `customer_account_no` varchar(10) NOT NULL,
  `bill_date` datetime NOT NULL,
  `subtotal` decimal(12,2) NOT NULL DEFAULT '0.00',
  `discount_rate` decimal(5,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `status` enum('draft','sent','paid','overdue','cancelled') NOT NULL DEFAULT 'draft',
  `notes` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`bill_id`),
  UNIQUE KEY `invoice_number` (`invoice_number`),
  KEY `idx_invoice_number` (`invoice_number`),
  KEY `idx_customer_account` (`customer_account_no`),
  KEY `idx_bill_date` (`bill_date`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_bills_customer` FOREIGN KEY (`customer_account_no`) REFERENCES `customers` (`account_no`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bills`
--

LOCK TABLES `bills` WRITE;
/*!40000 ALTER TABLE `bills` DISABLE KEYS */;
INSERT INTO `bills` VALUES (25,'2025-08-20-00001','ACC0050','2025-08-20 12:35:00',750.00,10.00,75.00,675.00,'draft','Discount given','2025-08-20 07:07:19','2025-08-20 07:09:49'),(26,'2025-08-20-00002','ACC0020','2025-08-20 12:44:00',12000.00,10.00,1200.00,10800.00,'draft','Test Bill 001','2025-08-20 07:15:44','2025-08-20 07:15:44'),(27,'2025-08-20-00003','ACC0001','2025-08-20 12:46:00',1200.00,0.00,0.00,1200.00,'draft','','2025-08-20 07:17:16','2025-08-20 07:17:16'),(28,'2025-08-20-00004','ACC0005','2025-08-20 12:48:00',150.00,0.00,0.00,150.00,'draft','','2025-08-20 07:21:58','2025-08-20 07:21:58'),(29,'2025-08-20-00005','ACC0008','2025-08-20 12:57:00',1850.00,2.50,46.25,1803.75,'overdue','Referred From Cardiff','2025-08-20 07:28:20','2025-08-20 07:35:11'),(30,'2025-08-20-00006','ACC0003','2025-08-20 13:03:00',720.00,0.00,0.00,720.00,'draft','','2025-08-20 07:36:14','2025-08-20 07:36:14'),(31,'2025-08-20-00007','ACC0001','2025-08-20 13:10:00',1850.00,0.00,0.00,1850.00,'draft','','2025-08-20 07:40:55','2025-08-20 07:40:55'),(32,'2025-08-20-00008','ACC0007','2025-08-20 13:18:00',450.00,0.00,0.00,450.00,'paid','','2025-08-20 07:48:33','2025-08-20 20:40:10'),(33,'2025-08-20-00009','ACC0004','2025-08-20 14:15:00',480.00,1.00,4.80,475.20,'paid','','2025-08-20 08:46:24','2025-08-20 20:40:14'),(34,'2025-08-20-00010','ACC0002','2025-08-20 14:26:00',240.00,0.00,0.00,240.00,'paid','','2025-08-20 08:57:09','2025-08-20 08:57:10'),(35,'2025-08-20-00011','ACC0037','2025-08-20 14:34:00',60.00,0.00,0.00,60.00,'paid','','2025-08-20 09:05:17','2025-08-20 09:05:17'),(36,'2025-08-20-00012','ACC0013','2025-08-20 14:35:00',350.00,0.00,0.00,350.00,'paid','','2025-08-20 09:09:35','2025-08-20 09:09:35'),(37,'2025-08-20-00013','ACC0009','2025-08-20 14:47:00',360.00,0.00,0.00,360.00,'overdue','','2025-08-20 09:17:59','2025-08-20 20:43:23'),(45,'2025-08-21-00002','ACC0006','2025-08-21 02:39:00',1510.00,0.00,0.00,1510.00,'paid','Done','2025-08-20 21:11:59','2025-08-20 21:11:59'),(46,'2025-08-21-00003','ACC0003','2025-08-21 16:25:00',240.00,0.00,0.00,240.00,'paid','','2025-08-21 10:55:29','2025-08-21 10:55:29'),(47,'2025-08-21-00004','ACC0001','2025-08-21 16:37:00',240.00,0.00,0.00,240.00,'paid','','2025-08-21 11:36:14','2025-08-21 11:36:14'),(48,'2025-08-21-00005','ACC0001','2025-08-21 17:06:00',240.00,0.00,0.00,240.00,'paid','','2025-08-21 11:36:28','2025-08-21 11:36:28');
/*!40000 ALTER TABLE `bills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `account_no` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_no`),
  UNIQUE KEY `account_no` (`account_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES ('ACC0001','John Smith','john.smith@example.com','123 Main St, Anytown, USA 10','5555010056','2025-08-18 11:34:30','2025-08-19 15:51:32'),('ACC0002','Emily Johnson','emily.johnson@example.com','456 Oak Ave, Somewhere, USA','555-0102','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0003','Michael Williams','michael.williams@example.com','789 Pine Rd, Nowhere, USA','555-0103','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0004','Sarah Brown','sarah.brown@example.com','321 Elm St, Anycity, USA','555-0104','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0005','David Jones','david.jones@example.com','654 Maple Dr, Yourtown, USA','555-0105','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0006','Jennifer Garcia','jennifer.garcia@example.com','987 Cedar Ln, Thistown, USA','555-0106','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0007','Robert Miller','robert.miller@example.com','135 Birch Blvd, Thatcity, USA','555-0107','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0008','Lisa Davis','lisa.davis@example.com','246 Walnut Way, Othertown, USA','555-0108','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0009','Thomas Rodriguez','thomas.rodriguez@example.com','369 Spruce Ct, Newtown, USA','555-0109','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0010','Nancy Martinez','nancy.martinez@example.com','482 Ash St, Oldtown, USA','555-0110','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0011','Daniel Hernandez','daniel.hernandez@example.com','591 Poplar Ave, Smalltown, USA','555-0111','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0012','Karen Lopez','karen.lopez@example.com','624 Willow Rd, Largetown, USA','555-0112','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0013','James Gonzalez','james.gonzalez@example.com','753 Redwood Dr, Hometown, USA','555-0113','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0014','Betty Wilson','betty.wilson@example.com','864 Magnolia Ln, Yourcity, USA','555-0114','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0015','Charles Anderson','charles.anderson@example.com','975 Sycamore Blvd, Ourcity, USA','555-0115','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0016','Margaret Thomas','margaret.thomas@example.com','108 Sequoia Way, Theirtown, USA','555-0116','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0017','Joseph Taylor','joseph.taylor@example.com','219 Juniper Ct, Thiscity, USA','555-0117','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0018','Dorothy Moore','dorothy.moore@example.com','330 Acacia St, Thatplace, USA','555-0118','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0019','Christopher Jackson','christopher.jackson@example.com','441 Palm Ave, Othercity, USA','555-0119','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0020','Sandra Martin','sandra.martin@example.com','552 Olive Rd, Nexttown, USA','555-0120','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0021','Matthew Lee','matthew.lee@example.com','663 Peach Dr, Lastcity, USA','555-0121','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0022','Carol Perez','carol.perez@example.com','774 Plum Ln, Firsttown, USA','555-0122','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0023','Donald Thompson','donald.thompson@example.com','885 Cherry Blvd, Mytown, USA','555-0123','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0024','Ruth White','ruth.white@example.com','996 Apple Way, Yourplace, USA','555-0124','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0025','Mark Harris','mark.harris@example.com','101 Grape St, Ourplace, USA','555-0125','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0026','Sharon Sanchez','sharon.sanchez@example.com','212 Lemon Ave, Theirplace, USA','555-0126','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0027','Paul Clark','paul.clark@example.com','323 Lime Rd, Thisplace, USA','555-0127','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0028','Michelle Ramirez','michelle.ramirez@example.com','434 Orange Dr, Thatspot, USA','555-0128','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0029','Steven Lewis','steven.lewis@example.com','545 Banana Ln, Otherspot, USA','555-0129','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0030','Laura Robinson','laura.robinson@example.com','656 Mango Blvd, Newplace, USA','555-0130','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0031','Andrew Walker','andrew.walker@example.com','767 Pear Way, Oldplace, USA','555-0131','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0032','Donna Young','donna.young@example.com','878 Kiwi St, Smallplace, USA','555-0132','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0033','Kenneth Allen','kenneth.allen@example.com','989 Berry Ave, Largeplace, USA','555-0133','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0034','Deborah King','deborah.king@example.com','110 Melon Rd, Homeplace, USA','555-0134','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0035','Joshua Wright','joshua.wright@example.com','221 Papaya Dr, Yourspot, USA','555-0135','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0036','Jessica Scott','jessica.scott@example.com','332 Coconut Ln, Ourspot, USA','555-0136','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0037','Kevin Torres','kevin.torres@example.com','443 Avocado Blvd, Theirspot, USA','555-0137','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0038','Amy Nguyen','amy.nguyen@example.com','554 Guava Way, Thisspot, USA','555-0138','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0039','Brian Hill','brian.hill@example.com','665 Fig St, Thatplace, USA','555-0139','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0040','Kimberly Flores','kimberly.flores@example.com','776 Date Ave, Otherplace, USA','555-0140','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0041','George Green','george.green@example.com','887 Apricot Rd, Nextplace, USA','555-0141','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0042','Angela Adams','angela.adams@example.com','998 Peach Dr, Lastplace, USA','555-0142','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0043','Edward Nelson','edward.nelson@example.com','109 Plum Ln, Firstplace, USA','555-0143','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0044','Martha Baker','martha.baker@example.com','210 Cherry Blvd, Myplace, USA','555-0144','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0045','Ronald Hall','ronald.hall@example.com','321 Apple Way, Yourhome, USA','555-0145','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0046','Brenda Rivera','brenda.rivera@example.com','432 Grape St, Ourhome, USA','555-0146','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0047','Timothy Campbell','timothy.campbell@example.com','543 Lemon Ave, Theirhome, USA','555-0147','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0048','Pamela Mitchell','pamela.mitchell@example.com','654 Lime Rd, Thishome, USA','555-0148','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0049','Jason Carter','jason.carter@example.com','765 Orange Dr, Thathome, USA','555-0149','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0050','Cynthia Roberts','cynthia.roberts@example.com','876 Banana Ln, Otherhome, USA','555-0150','2025-08-18 11:34:30','2025-08-18 11:34:30'),('ACC0051','Emily Hopkins','emily@example.com','','4837957384','2025-08-18 12:09:49','2025-08-18 12:09:49'),('ACC0052','Emily Hopkins','emily@example.com','walasmulla','4837957384','2025-08-18 13:22:00','2025-08-18 14:12:19'),('ACC0053','jjfnjdnkjf','regingie@gmail.com','njcnieenci','4837957386','2025-08-19 13:43:49','2025-08-19 13:44:06');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice_sequence`
--

DROP TABLE IF EXISTS `invoice_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice_sequence` (
  `invoice_period` varchar(7) NOT NULL,
  `last_sequence` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`invoice_period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_sequence`
--

LOCK TABLES `invoice_sequence` WRITE;
/*!40000 ALTER TABLE `invoice_sequence` DISABLE KEYS */;
INSERT INTO `invoice_sequence` VALUES ('2025-08',55);
/*!40000 ALTER TABLE `invoice_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `item_code` varchar(10) NOT NULL,
  `name` varchar(255) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `qty_on_hand` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES ('ITM0001','Atlas Pencil (HB) - 12pack',240.00,10),('ITM0002','Kokuyo Long Notebook 80p (Single Line)',320.00,85),('ITM0003','Casio Scientific Calculator fx-82MS',1850.00,40),('ITM0004','Giotto Turbo Eraser (Pack of 3)',180.00,200),('ITM0005','Faber-Castell Colour Pencils (24pk)',1200.00,60),('ITM0006','White Glue 40ml (Pidilite)',110.00,120),('ITM0007','Olasa Highlighters (Assorted 4pk)',350.00,95),('ITM0008','Ballpoint Pen (Blue) - Dozen',360.00,300),('ITM0009','Ballpoint Pen (Red) - Dozen',360.00,280),('ITM0010','Ballpoint Pen (Black) - Dozen',360.00,310),('ITM0011','Office File (Blue A4 Foolscap)',95.00,180),('ITM0012','Office File (Red A4 Foolscap)',95.00,160),('ITM0013','Drawing Book A4 (40 pages)',220.00,75),('ITM0014','Geometry Box (Metal)',650.00,50),('ITM0015','Stapler No. 35',480.00,65),('ITM0016','Box of Stapler Pins (1000pcs)',280.00,110),('ITM0017','Scissors (Medium 6 inch)',420.00,45),('ITM0018','Transparent Tape 1 inch',60.00,250),('ITM0019','Transparent Tape Dispenser',220.00,70),('ITM0020','White Board Marker (Black)',100.00,140),('ITM0021','White Board Marker (Blue)',100.00,130),('ITM0022','White Board Marker (Red)',100.00,125),('ITM0023','White Board Duster',350.00,40),('ITM0024','Thumb Tacks (Box of 100)',150.00,90),('ITM0025','Paper Clips (Box of 100)',80.00,200),('ITM0026','Sinhala Aluth Pariganana Potha Grade 10',600.00,55),('ITM0027','English Literature Textbook Grade 11',750.00,40),('ITM0028','GCE O/L Science Past Papers Book',1100.00,35),('ITM0029','GCE A/L Combined Mathematics Part 1',1450.00,30),('ITM0030','Oxford English-Sinhala Dictionary',1800.00,25),('ITM0031','Sinhala Madhya Lipi Potha',450.00,60),('ITM0032','A4 Printing Paper (500 sheets)',2200.00,28),('ITM0033','Manila Folders (Pack of 10)',300.00,85),('ITM0034','Permanent Marker (Sharpie)',220.00,110),('ITM0035','Watercolour Paint Set (12 colours)',950.00,42),('ITM0036','Paint Brush Set (5 sizes)',550.00,38),('ITM0037','Glitter Pen Set (6 colours)',380.00,75),('ITM0038','Correction Pen (White)',120.00,150),('ITM0039','Pencil Sharpener (Metal)',90.00,180),('ITM0040','Pencil Sharpener (Plastic)',40.00,220),('ITM0044','Stamp Pad (Black)',320.00,50),('ITM0045','Rubber Stamps (Date Received)',650.00,15),('ITM0046','Binder Clips (Medium, Pack of 12)',280.00,95),('ITM0047','Hole Punch (Single)',520.00,30),('ITM0048','Hole Punch (Double)',750.00,25),('ITM0049','Writing Pad (200 pages, Lined)',280.00,80),('ITM0050','Writing Pad (200 pages, Unlined)',280.00,70),('ITM0051','Brown Parcel Paper (Roll)',450.00,20),('ITM0052','Invoice Book (Duplicate, 50 sets)',600.00,40),('ITM0053','Receipt Book (Triplicate, 100 sets)',1100.00,22),('ITM0054','Graph Paper Pad A4',200.00,58),('ITM0055','Fountain Pen (Beginner)',550.00,35),('ITM0056','Bottle of Blue Ink (50ml)',300.00,60),('ITM0057','Calligraphy Pen Set',1500.00,18),('ITM0058','Book Cover Roll (Transparent)',400.00,45),('ITM0059','Book Cover Roll (Brown)',350.00,50),('ITM0060','Sticky Notes 3x3 (Yellow, 100 sheets)',180.00,120),('ITM0061','Sticky Notes 4x6 (Assorted Colours)',250.00,85),('ITM0062','Desk Organizer Tray',1200.00,15),('ITM0063','Bulldog Clip (Large, 41mm)',50.00,200),('ITM0064','Presentation Portfolio Folder',400.00,35),('ITM0065','Card Stock Paper (Assorted, 20 sheets)',380.00,40),('ITM0066','Maths Set Square (45 Degree)',70.00,150),('ITM0067','Maths Set Square (60 Degree)',70.00,150),('ITM0068','Maths Protractor 180 Degree',80.00,130),('ITM0069','Compass for Geometry',250.00,65),('ITM0070','Index Dividers (A4, 12 tabs)',320.00,55),('ITM0071','Ledger Book (500 pages)',1800.00,20),('ITM0072','Guest Register',1200.00,18),('ITM0073','Visitor Pass Holders (10pk)',450.00,30),('ITM0074','ID Card Lanyard (Plain)',150.00,100),('ITM0075','ID Card Lanyard (Printed)',220.00,80),('ITM0076','White Card Stock (10 sheets)',200.00,70),('ITM0077','Butterfly Clips (Pack of 50)',110.00,160),('ITM0078','Paper Fasteners (Box of 100)',90.00,140),('ITM0079','Carbon Paper (10 sheets)',200.00,45),('ITM0080','Blu-Tack (50g)',300.00,90),('ITM0081','Atlas 80pgs Excercise CR Book',240.00,40),('ITM0082','Atlas 80pgs Excercise book',280.00,25),('ITM0083','Atlas 120pgs Excercise book',1200.00,100),('ITM0084','Atlas 200pgs Excercise book',360.00,40),('ITM0085','test',10.00,10),('ITM0086','Atlas Marker Pen',150.00,50),('ITM0087','Atlas Gum Bottle',200.00,20);
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('Admin','admin@123');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'pahanaedu'
--

--
-- Dumping routines for database 'pahanaedu'
--
/*!50003 DROP PROCEDURE IF EXISTS `GetNextInvoiceNumber` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetNextInvoiceNumber`(OUT next_invoice_number VARCHAR(20))
BEGIN
    DECLARE current_period VARCHAR(7);
    DECLARE next_seq INT;
    
    SET current_period = DATE_FORMAT(NOW(), '%Y-%m');
    
    INSERT INTO invoice_sequence (invoice_period, last_sequence) 
    VALUES (current_period, 1)
    ON DUPLICATE KEY UPDATE last_sequence = last_sequence + 1;
    
    SELECT last_sequence INTO next_seq 
    FROM invoice_sequence 
    WHERE invoice_period = current_period;
    
    SET next_invoice_number = CONCAT(current_period, '-', LPAD(next_seq, 5, '0'));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-21 22:50:57
