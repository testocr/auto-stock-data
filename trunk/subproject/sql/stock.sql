-- phpMyAdmin SQL Dump
-- version 3.4.5
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Feb 13, 2014 at 09:46 AM
-- Server version: 5.5.16
-- PHP Version: 5.3.8

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `stock`
--

-- --------------------------------------------------------

--
-- Table structure for table `best_stock`
--

CREATE TABLE IF NOT EXISTS `best_stock` (
  `top_stock` varchar(20) NOT NULL,
  `close_price` int(11) DEFAULT NULL,
  `open_price` int(11) DEFAULT NULL,
  `cel_price` int(11) DEFAULT NULL,
  `flo_price` int(11) DEFAULT NULL,
  `vol` bigint(20) DEFAULT NULL,
  `fund` bigint(20) DEFAULT NULL,
  `odd_buy` bigint(20) DEFAULT NULL,
  `odd_sell` bigint(20) DEFAULT NULL,
  `cel_price_52W` int(11) DEFAULT NULL,
  `flo_price_52W` int(11) DEFAULT NULL,
  `vol_52W` bigint(20) DEFAULT NULL,
  `foreign_buy` bigint(20) DEFAULT NULL,
  `foreign_percent` float DEFAULT NULL,
  `common_dividend` int(11) DEFAULT NULL,
  `common_dividend_percent` float DEFAULT NULL,
  `beta` float DEFAULT NULL,
  `eps` int(11) DEFAULT NULL,
  `pe` float DEFAULT NULL,
  `fpe` float DEFAULT NULL,
  `bvps` int(11) DEFAULT NULL,
  `pb` float DEFAULT NULL,
  `tran_date` varchar(20) NOT NULL,
  PRIMARY KEY (`top_stock`,`tran_date`),
  KEY `top_stock` (`top_stock`),
  KEY `tran_date` (`tran_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `best_stock`
--

INSERT INTO `best_stock` (`top_stock`, `close_price`, `open_price`, `cel_price`, `flo_price`, `vol`, `fund`, `odd_buy`, `odd_sell`, `cel_price_52W`, `flo_price_52W`, `vol_52W`, `foreign_buy`, `foreign_percent`, `common_dividend`, `common_dividend_percent`, `beta`, `eps`, `pe`, `fpe`, `bvps`, `pb`, `tran_date`) VALUES
('FLC', 10300, 10500, 10600, 10200, 7626980, 795, 947510, 919670, 11700, 4400, 3158988, 4300, 1.57, 0, 0, 1.33, 990, 10.404, 8.64079, 16125, 0.63876, '02/13/2014'),
('HAG', 24800, 24900, 25300, 24300, 7613670, 17810, 121000, 303990, 26900, 19200, 1612250, 107100, 34.8, 1500, 0.0604839, 1.43, 1033, 24.0077, NULL, 17976, 1.37962, '02/13/2014'),
('MBB', 14700, 14200, 14700, 14200, 6852050, 16547, 761220, 877460, 14000, 12000, 562201, 1000, 10, 0, 0, 0.94, 1987, 7.39809, 78.7261, 13299, 1.10535, '02/13/2014'),
('PVT', 13200, 13100, 13200, 12900, 7943560, 3070, 430080, 0, 12900, 4200, 1794918, 253890, 5.47, 200, 0.0151515, 1.24, 860, 15.3488, 78.7261, 11682, 1.12994, '02/13/2014'),
('SSI', 24400, 24600, 24700, 23700, 4636700, 8558, 137550, 242500, 24400, 15700, 1420079, 11040, 49, 3000, 0.122951, 1.22, 1061, 22.9972, NULL, 14243, 1.71312, '02/13/2014');

-- --------------------------------------------------------

--
-- Table structure for table `top_stock`
--

CREATE TABLE IF NOT EXISTS `top_stock` (
  `stock_code` varchar(20) NOT NULL,
  `val` bigint(20) NOT NULL,
  `tran_date` varchar(20) NOT NULL,
  PRIMARY KEY (`stock_code`,`tran_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `top_stock`
--

INSERT INTO `top_stock` (`stock_code`, `val`, `tran_date`) VALUES
('FLC', 79394000000, '13/02/2014'),
('HAG', 189462000000, '13/02/2014'),
('MBB', 99464000000, '13/02/2014'),
('PVT', 103981000000, '13/02/2014'),
('SSI', 112044000000, '13/02/2014');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
