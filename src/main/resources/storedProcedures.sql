USE `vertDB` ;
DELIMITER $$

DROP PROCEDURE IF EXISTS `nueva_categoria`$$

CREATE PROCEDURE `nueva_categoria`(
    IN p_nombre VARCHAR(45),
    OUT p_idCategoria INT
)
BEGIN
    DECLARE v_existe INT DEFAULT 0;
    
    -- 1. Validar que el nombre no sea NULL
    IF p_nombre IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la categoría no puede ser nulo.';
    END IF;

    -- 2. Validar que no sea vacío ni esté compuesto solo por espacios
    IF TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la categoría no puede estar vacío ni contener únicamente espacios.';
    END IF;

    -- 3. Validar que el nombre no esté ya en uso
    SELECT COUNT(*) INTO v_existe 
    FROM Categorias 
    WHERE nombre = TRIM(p_nombre);
    
    IF v_existe > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la categoría ya se encuentra en uso.';
    END IF;

    -- 4. Si todo está bien, crear la categoría
    INSERT INTO Categorias (nombre) 
    VALUES (TRIM(p_nombre));
    
    -- 5. Asignar el ID generado al parámetro de salida
    SET p_idCategoria = LAST_INSERT_ID();

END$$

DELIMITER ;


DELIMITER $$

DROP PROCEDURE IF EXISTS `modificar_categoria`$$

CREATE PROCEDURE `modificar_categoria`(
    IN p_idCategoria INT,
    IN p_nombre VARCHAR(45)
)
sp_modificar: BEGIN
    DECLARE v_existe_nombre INT DEFAULT 0;
    DECLARE v_nombre_actual VARCHAR(45);

    -- 1. Validar que el nombre no sea NULL
    IF p_nombre IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la categoría no puede ser nulo.';
    END IF;

    -- 2. Validar que no sea vacío ni esté compuesto solo por espacios
    IF TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la categoría no puede estar vacío ni contener únicamente espacios.';
    END IF;

    -- 3. Obtener el nombre actual de la categoría
    SELECT nombre INTO v_nombre_actual
    FROM Categorias
    WHERE idCategoria = p_idCategoria;

    -- Validar que la categoría que se intenta modificar realmente exista
    IF v_nombre_actual IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La categoría que intenta modificar no existe.';
    END IF;

    -- 4. Si el nombre nuevo es exactamente igual al actual, salimos sin hacer nada
    IF v_nombre_actual = TRIM(p_nombre) THEN
        LEAVE sp_modificar;
    END IF;

    -- 5. Validar que el nuevo nombre no esté en uso por OTRA categoría
    SELECT COUNT(*) INTO v_existe_nombre 
    FROM Categorias 
    WHERE nombre = TRIM(p_nombre) AND idCategoria != p_idCategoria;
    
    IF v_existe_nombre > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la categoría ya se encuentra en uso.';
    END IF;

    -- 6. Si pasó todas las validaciones, actualizar
    UPDATE Categorias 
    SET nombre = TRIM(p_nombre)
    WHERE idCategoria = p_idCategoria;

END$$

DELIMITER ;