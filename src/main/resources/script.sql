-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema vertDB
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `vertDB` ;

-- -----------------------------------------------------
-- Schema vertDB
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `vertDB` ;
USE `vertDB` ;

-- -----------------------------------------------------
-- Table `vertDB`.`Categorias`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `vertDB`.`Categorias` ;

CREATE TABLE IF NOT EXISTS `vertDB`.`Categorias` (
  `idCategoria` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idCategoria`))
ENGINE = InnoDB;

CREATE UNIQUE INDEX `nombre_UNIQUE` ON `vertDB`.`Categorias` (`nombre` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `vertDB`.`Productos`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `vertDB`.`Productos` ;

CREATE TABLE IF NOT EXISTS `vertDB`.`Productos` (
  `idProducto` INT NOT NULL AUTO_INCREMENT,
  `idCategoria` INT NOT NULL,
  `nombre` VARCHAR(45) NOT NULL,
  `precio` DECIMAL(6,2) NOT NULL,
  `descripcion` VARCHAR(255) NULL,
  `imgUrl` VARCHAR(255) NULL,
  PRIMARY KEY (`idProducto`),
  CONSTRAINT `fk_Productos_Categorias`
    FOREIGN KEY (`idCategoria`)
    REFERENCES `vertDB`.`Categorias` (`idCategoria`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_Productos_Categorias_idx` ON `vertDB`.`Productos` (`idCategoria` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `vertDB`.`Usuarios`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `vertDB`.`Usuarios` ;

CREATE TABLE IF NOT EXISTS `vertDB`.`Usuarios` (
  `idUsuarios` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(45) NOT NULL,
  `password` CHAR(60) NOT NULL,
  PRIMARY KEY (`idUsuarios`))
ENGINE = InnoDB;

CREATE UNIQUE INDEX `username_UNIQUE` ON `vertDB`.`Usuarios` (`username` ASC) VISIBLE;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
