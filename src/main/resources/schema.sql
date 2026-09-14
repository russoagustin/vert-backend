 -- MySQL Workbench Forward Engineering


SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;

SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';


-- -----------------------------------------------------

-- Schema Vert

-- -----------------------------------------------------

DROP SCHEMA IF EXISTS `Vert` ;


-- -----------------------------------------------------

-- Schema Vert

-- -----------------------------------------------------

CREATE SCHEMA IF NOT EXISTS `Vert` DEFAULT CHARACTER SET utf8 ;

USE `Vert` ;


-- -----------------------------------------------------

-- Table `Vert`.`Categorias`

-- -----------------------------------------------------

DROP TABLE IF EXISTS `Vert`.`Categorias` ;


CREATE TABLE IF NOT EXISTS `Vert`.`Categorias` (

`idCategoria` INT NOT NULL AUTO_INCREMENT,

`nombre` VARCHAR(30) NOT NULL,

`orden` SMALLINT NOT NULL,

PRIMARY KEY (`idCategoria`))

ENGINE = InnoDB;


CREATE UNIQUE INDEX `nombre_UNIQUE` ON `Vert`.`Categorias` (`nombre` ASC) VISIBLE;



-- -----------------------------------------------------

-- Table `Vert`.`SubCategorias`

-- -----------------------------------------------------

DROP TABLE IF EXISTS `Vert`.`SubCategorias` ;


CREATE TABLE IF NOT EXISTS `Vert`.`SubCategorias` (

`idSubCategoria` INT NOT NULL,

`idCategoria` INT NOT NULL,

`nombre` VARCHAR(30) NOT NULL,

`orden` SMALLINT NOT NULL,

PRIMARY KEY (`idSubCategoria`, `idCategoria`),

CONSTRAINT `fk_SubCategorias_Categorias`

FOREIGN KEY (`idCategoria`)

REFERENCES `Vert`.`Categorias` (`idCategoria`)

ON DELETE NO ACTION

ON UPDATE NO ACTION)

ENGINE = InnoDB;


CREATE INDEX `fk_SubCategorias_Categorias_idx` ON `Vert`.`SubCategorias` (`idCategoria` ASC) VISIBLE;


CREATE UNIQUE INDEX `uq_nombre_idCategoria` ON `Vert`.`SubCategorias` (`idCategoria` ASC, `nombre` ASC) VISIBLE;



-- -----------------------------------------------------

-- Table `Vert`.`Productos`

-- -----------------------------------------------------

DROP TABLE IF EXISTS `Vert`.`Productos` ;


CREATE TABLE IF NOT EXISTS `Vert`.`Productos` (

`idProducto` INT NOT NULL,

`idSubCategoria` INT NOT NULL,

`idCategoria` INT NOT NULL,

`nombre` VARCHAR(30) NOT NULL,

`precio` DECIMAL(9,2) NOT NULL,

`imgUrl` VARCHAR(300) NOT NULL,

`precioDescuento` DECIMAL(9,2) NULL,

`descripcion` VARCHAR(255) NULL,

`cantidad` TINYINT NULL,

PRIMARY KEY (`idProducto`),

CONSTRAINT `fk_Productos_SubCategorias1`

FOREIGN KEY (`idSubCategoria` , `idCategoria`)

REFERENCES `Vert`.`SubCategorias` (`idSubCategoria` , `idCategoria`)

ON DELETE NO ACTION

ON UPDATE NO ACTION)

ENGINE = InnoDB;


CREATE INDEX `fk_Productos_SubCategorias1_idx` ON `Vert`.`Productos` (`idSubCategoria` ASC, `idCategoria` ASC) VISIBLE;



-- -----------------------------------------------------

-- Table `Vert`.`Usuarios`

-- -----------------------------------------------------

DROP TABLE IF EXISTS `Vert`.`Usuarios` ;


CREATE TABLE IF NOT EXISTS `Vert`.`Usuarios` (

`idUsuario` INT NOT NULL AUTO_INCREMENT,

`username` VARCHAR(50) NOT NULL,

`password` VARCHAR(255) NOT NULL,

PRIMARY KEY (`idUsuario`))

ENGINE = InnoDB;


CREATE UNIQUE INDEX `username_UNIQUE` ON `Vert`.`Usuarios` (`username` ASC) VISIBLE;



SET SQL_MODE=@OLD_SQL_MODE;

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;

SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS; 
