
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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `max_quiz_count` int NOT NULL,
  `max_wordbook_count` int NOT NULL,
  `max_wordbooks_per_quiz` int NOT NULL,
  `max_words_per_book` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `login_at` datetime(6) NOT NULL,
  `user_agent` text,
  `user_client_ip` varchar(255) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `attempted_email` varchar(255) DEFAULT NULL,
  `login_success` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prompt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `last_modified_by` bigint NOT NULL,
  `prompt_name` varchar(50) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `prompt_type_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbmy165dl2obejveiyff962i2o` (`prompt_type_id`),
  CONSTRAINT `FKhcc9c862ii7u9en5ymwej0dav` FOREIGN KEY (`prompt_type_id`) REFERENCES `quiz_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `name` varchar(100) NOT NULL,
  `sharing_status` varchar(20) NOT NULL DEFAULT 'PUBLIC',
  `created_by` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlkbkwkpy099nrwd9eytxba9sf` (`created_by`),
  CONSTRAINT `FKlkbkwkpy099nrwd9eytxba9sf` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_allowed_type` (
  `quiz_id` bigint NOT NULL,
  `quiz_type_id` bigint NOT NULL,
  KEY `FKns1th248t22omgd0c1bvhwsus` (`quiz_type_id`),
  KEY `FKrj0wdbip0dsikxmb6tbepq3ig` (`quiz_id`),
  CONSTRAINT `FKns1th248t22omgd0c1bvhwsus` FOREIGN KEY (`quiz_type_id`) REFERENCES `quiz_type` (`id`),
  CONSTRAINT `FKrj0wdbip0dsikxmb6tbepq3ig` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_correct` bit(1) DEFAULT NULL,
  `question_order` int NOT NULL,
  `quiz_session_id` bigint NOT NULL,
  `word_id` bigint NOT NULL,
  `ai_generated_sentence` varchar(255) DEFAULT NULL,
  `ai_generated_translation` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq3hk5492ed8oiuxuhe1ipwokb` (`quiz_session_id`),
  KEY `FKsrgpy5c74k1djie9y3k2o2ois` (`word_id`),
  CONSTRAINT `FKq3hk5492ed8oiuxuhe1ipwokb` FOREIGN KEY (`quiz_session_id`) REFERENCES `quiz_session` (`id`),
  CONSTRAINT `FKsrgpy5c74k1djie9y3k2o2ois` FOREIGN KEY (`word_id`) REFERENCES `words` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=196 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempted_at` datetime(6) NOT NULL,
  `score` int NOT NULL,
  `quiz_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `is_quiz_active` bit(1) NOT NULL,
  `quiz_type_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKiv0bo0e0f1p69i01xyy19r32x` (`quiz_id`),
  KEY `FK3vxr0hkfy3y0f00o21f9m5l5l` (`user_id`),
  KEY `FKbgllxv1wprogan7o5q3envi7m` (`quiz_type_id`),
  CONSTRAINT `FK3vxr0hkfy3y0f00o21f9m5l5l` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKbgllxv1wprogan7o5q3envi7m` FOREIGN KEY (`quiz_type_id`) REFERENCES `quiz_type` (`id`),
  CONSTRAINT `FKiv0bo0e0f1p69i01xyy19r32x` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_by` bigint NOT NULL,
  `last_modified_by` bigint NOT NULL,
  `quiz_type_description` varchar(500) NOT NULL,
  `quiz_type_name` varchar(50) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `use_ai` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg37yvsxh5cgsh7g80qjqr0y0n` (`quiz_type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_wordbook` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quiz_id` bigint NOT NULL,
  `wordbook_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl8yq6kuny9mndjx609pur2vt` (`quiz_id`),
  KEY `FK4fjwir2rdym50ko6x9cphicvk` (`wordbook_id`),
  CONSTRAINT `FK4fjwir2rdym50ko6x9cphicvk` FOREIGN KEY (`wordbook_id`) REFERENCES `wordbooks` (`id`),
  CONSTRAINT `FKl8yq6kuny9mndjx609pur2vt` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_connection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `connection_status` varchar(20) NOT NULL,
  `connection_type` varchar(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `target_user_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKb7ah0nd68b1tnw7j2iqnr49hy` (`user_id`,`target_user_id`),
  KEY `FKfvtjqgnhho3ydbgyq3h77qx2w` (`target_user_id`),
  CONSTRAINT `FKb2n6yqdlf5s2r69havbp2othd` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfvtjqgnhho3ydbgyq3h77qx2w` FOREIGN KEY (`target_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(50) NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wordbooks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `created_by` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK91a074e6qq9d6n8ranckesyax` (`created_by`),
  CONSTRAINT `FK91a074e6qq9d6n8ranckesyax` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `words` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text NOT NULL,
  `term` varchar(100) NOT NULL,
  `wordbook_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_words_wordbook_term` (`wordbook_id`,`term`),
  CONSTRAINT `FKl1nbe1cphr56ou4sgh6jag3ih` FOREIGN KEY (`wordbook_id`) REFERENCES `wordbooks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

