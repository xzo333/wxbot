/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MariaDB
 Source Server Version : 101103 (10.11.3-MariaDB)
 Source Host           : localhost:3306
 Source Schema         : wxbot

 Target Server Type    : MariaDB
 Target Server Version : 101103 (10.11.3-MariaDB)
 File Encoding         : 65001

 Date: 04/07/2023 10:53:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for orderdate
-- ----------------------------
DROP TABLE IF EXISTS `orderdate`;
CREATE TABLE `orderdate`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `orderid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单',
  `date` timestamp NULL DEFAULT current_timestamp() COMMENT '日期',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接单员',
  `grade` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级',
  `wxid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'wxid',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for userdate
-- ----------------------------
DROP TABLE IF EXISTS `userdate`;
CREATE TABLE `userdate`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `grade` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级',
  `battery` int(10) NULL DEFAULT NULL COMMENT '电池',
  `historicalbattery` int(10) NULL DEFAULT NULL COMMENT '历史电池',
  `state` int(2) NULL DEFAULT NULL COMMENT '状态冻结等级',
  `wxid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'wxid',
  `continuation` int(10) NULL DEFAULT NULL COMMENT '续单数',
  `numberoforders` int(10) NULL DEFAULT NULL COMMENT '接单数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 185 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
