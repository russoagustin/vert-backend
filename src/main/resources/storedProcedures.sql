USE `Vert` ;

DELIMITER //

DROP PROCEDURE IF EXISTS nueva_categoria//

CREATE PROCEDURE nueva_categoria(
    IN p_nombre VARCHAR(30),
    OUT p_idCategoria INTEGER
)
BEGIN
    DECLARE v_nombre_limpio VARCHAR(30);
    DECLARE v_existe_nombre INT;
    DECLARE v_nuevo_orden SMALLINT;
    DECLARE v_nuevo_id INT;
    DECLARE v_lock_adquirido INT;

    -- 1. Intentar adquirir un candado (espera máximo 5 segundos)
    -- Si otro proceso está ejecutando este bloque, el actual esperará.
    SELECT GET_LOCK('lock_creacion_categoria', 5) INTO v_lock_adquirido;

    IF v_lock_adquirido = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El sistema está muy ocupado procesando otras categorías. Intente nuevamente en unos segundos.';
    END IF;

    -- Usamos un bloque BEGIN...END anidado para asegurar que el candado se libere
    -- incluso si ocurre un error (usando un HANDLER).
    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            -- Si ocurre CUALQUIER error, soltamos el candado antes de abortar
            DO RELEASE_LOCK('lock_creacion_categoria');
            RESIGNAL; -- Vuelve a lanzar el error original
        END;

        -- 2. Validaciones básicas
        IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El nombre de la categoría no puede ser nulo, vacío ni estar compuesto solo por espacios.';
        END IF;

        SET v_nombre_limpio = TRIM(p_nombre);

        -- 3. Validar duplicados
        SELECT COUNT(*) INTO v_existe_nombre 
        FROM Vert.Categorias 
        WHERE nombre = v_nombre_limpio;

        IF v_existe_nombre > 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El nombre de la categoría ya se encuentra en uso.';
        END IF;

        -- 4. Cálculo de IDs (Ahora es seguro porque estamos bajo un candado exclusivo)
        SELECT IFNULL(MAX(orden), 0) + 1 INTO v_nuevo_orden 
        FROM Vert.Categorias;

        -- 5. Inserción
        INSERT INTO Vert.Categorias (idCategoria, nombre, orden)
        VALUES (v_nuevo_id, v_nombre_limpio, v_nuevo_orden);
        
		SET p_idCategoria = LAST_INSERT_ID();
        -- 6. Liberar el candado tras el éxito
        DO RELEASE_LOCK('lock_creacion_categoria');
    END;
END //

DELIMITER ;


DELIMITER //

DROP PROCEDURE IF EXISTS modificar_categoria//

CREATE PROCEDURE modificar_categoria(
    IN p_idCategoria INT,
    IN p_nombre VARCHAR(30)
)
proc_principal: BEGIN
    -- Declaración de variables
    DECLARE v_nombre_limpio VARCHAR(30);
    DECLARE v_nombre_actual VARCHAR(30);
    DECLARE v_existe_categoria INT;
    DECLARE v_existe_duplicado INT;

    -- 1. Validar que no sea nulo, vacío ni contenga solo espacios
    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la categoría no puede ser nulo, vacío ni estar compuesto solo por espacios.';
    END IF;

    -- Limpiar espacios al inicio y final
    SET v_nombre_limpio = TRIM(p_nombre);

    -- 2. Verificar que la categoría a modificar realmente exista y obtener su nombre actual
    SELECT COUNT(*), MAX(nombre) INTO v_existe_categoria, v_nombre_actual
    FROM Vert.Categorias
    WHERE idCategoria = p_idCategoria;

    IF v_existe_categoria = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La categoría que intenta modificar no existe.';
    END IF;

    -- 3. Si el nombre nuevo es exactamente igual al que ya tiene, no hacemos nada y salimos
    IF v_nombre_actual = v_nombre_limpio THEN
        LEAVE proc_principal; -- Finaliza la ejecución del procedimiento sin hacer el UPDATE
    END IF;

    -- 4. Validar que el nuevo nombre no esté en uso por OTRA categoría
    SELECT COUNT(*) INTO v_existe_duplicado
    FROM Vert.Categorias
    WHERE nombre = v_nombre_limpio 
      AND idCategoria != p_idCategoria;

    IF v_existe_duplicado > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nuevo nombre ya se encuentra en uso por otra categoría.';
    END IF;

    -- 5. Si pasa todas las validaciones, procedemos a modificar el nombre
    UPDATE Vert.Categorias
    SET nombre = v_nombre_limpio
    WHERE idCategoria = p_idCategoria;

END //

DELIMITER ;

DELIMITER //

DROP PROCEDURE IF EXISTS cambiar_orden_categoria//

CREATE PROCEDURE cambiar_orden_categoria(
    IN p_idCategoria INT,
    IN p_orden SMALLINT
)
proc_principal: BEGIN
    -- Declaración de variables
    DECLARE v_orden_actual SMALLINT;
    DECLARE v_max_orden SMALLINT;
    DECLARE v_existe INT;
    DECLARE v_nuevo_orden SMALLINT;

    -- Manejador de errores para revertir los cambios si algo sale mal
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- 1. Validar que la categoría exista y obtener su orden actual
    SELECT COUNT(*), MAX(orden) INTO v_existe, v_orden_actual
    FROM Vert.Categorias
    WHERE idCategoria = p_idCategoria;

    IF v_existe = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La categoría especificada no existe.';
    END IF;

    -- 2. Obtener el máximo orden actual para validar límites
    SELECT IFNULL(MAX(orden), 1) INTO v_max_orden 
    FROM Vert.Categorias;

    -- Limitar el nuevo orden para que no sea menor a 1 ni mayor al máximo existente
    SET v_nuevo_orden = p_orden;
    
    IF v_nuevo_orden < 1 THEN
        SET v_nuevo_orden = 1;
    ELSEIF v_nuevo_orden > v_max_orden THEN
        SET v_nuevo_orden = v_max_orden;
    END IF;

    -- 3. Si el orden ya es el mismo, no hacemos nada
    IF v_orden_actual = v_nuevo_orden THEN
        LEAVE proc_principal;
    END IF;

    -- 4. Iniciar transacción segura
    START TRANSACTION;

    -- 5. Desplazar las demás categorías dependiendo de la dirección del cambio
    IF v_nuevo_orden < v_orden_actual THEN
        -- CASO A: La categoría "Sube" (ej. de la pos 5 a la 2)
        -- Los elementos intermedios (2, 3, 4) deben "Bajar" (+1) a las posiciones 3, 4, 5
        UPDATE Vert.Categorias
        SET orden = orden + 1
        WHERE orden >= v_nuevo_orden 
          AND orden < v_orden_actual;
    ELSE
        -- CASO B: La categoría "Baja" (ej. de la pos 2 a la 5)
        -- Los elementos intermedios (3, 4, 5) deben "Subir" (-1) a las posiciones 2, 3, 4
        UPDATE Vert.Categorias
        SET orden = orden - 1
        WHERE orden > v_orden_actual 
          AND orden <= v_nuevo_orden;
    END IF;

    -- 6. Finalmente, asignar el nuevo orden a la categoría seleccionada
    UPDATE Vert.Categorias
    SET orden = v_nuevo_orden
    WHERE idCategoria = p_idCategoria;

    -- Confirmar los cambios
    COMMIT;

END //

DELIMITER ;

DELIMITER //

DROP PROCEDURE IF EXISTS nueva_subcategoria//

CREATE PROCEDURE nueva_subcategoria(
    IN p_idCategoria INT,
    IN p_nombre VARCHAR(30),
    OUT p_idSubCategoria INT
)
BEGIN
    DECLARE v_nombre_limpio VARCHAR(30);
    DECLARE v_existe_categoria INT;
    DECLARE v_existe_nombre INT;
    DECLARE v_nuevo_orden SMALLINT;
    DECLARE v_nuevo_id INT;
    DECLARE v_lock_adquirido INT;
    DECLARE v_lock_name VARCHAR(64);

    SET v_lock_name = CONCAT('lock_creacion_subcat_', IFNULL(p_idCategoria, 0));

    -- 1. Intentar adquirir un candado (espera máximo 5 segundos)
    SELECT GET_LOCK(v_lock_name, 5) INTO v_lock_adquirido;

    IF v_lock_adquirido = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El sistema está muy ocupado procesando otras subcategorías. Intente nuevamente en unos segundos.';
    END IF;

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            DO RELEASE_LOCK(v_lock_name);
            RESIGNAL;
        END;

        -- 2. Validar que la categoría exista
        IF p_idCategoria IS NULL THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El ID de la categoría no puede ser nulo.';
        END IF;

        SELECT COUNT(*) INTO v_existe_categoria
        FROM Vert.Categorias
        WHERE idCategoria = p_idCategoria;

        IF v_existe_categoria = 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: La categoría especificada no existe.';
        END IF;

        -- 3. Validar nombre
        IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El nombre de la subcategoría no puede ser nulo, vacío ni estar compuesto solo por espacios.';
        END IF;

        SET v_nombre_limpio = TRIM(p_nombre);

        -- 4. Validar duplicados dentro de la categoría
        SELECT COUNT(*) INTO v_existe_nombre 
        FROM Vert.SubCategorias 
        WHERE idCategoria = p_idCategoria AND nombre = v_nombre_limpio;

        IF v_existe_nombre > 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El nombre de la subcategoría ya se encuentra en uso para esta categoría.';
        END IF;

        -- 5. Cálculo del nuevo orden para la subcategoría
        SELECT IFNULL(MAX(orden), 0) + 1 INTO v_nuevo_orden 
        FROM Vert.SubCategorias
        WHERE idCategoria = p_idCategoria;

        -- 6. Cálculo del nuevo ID
        SELECT IFNULL(MAX(idSubCategoria), 0) + 1 INTO v_nuevo_id 
        FROM Vert.SubCategorias;

        -- 7. Inserción
        INSERT INTO Vert.SubCategorias (idSubCategoria, idCategoria, nombre, orden)
        VALUES (v_nuevo_id, p_idCategoria, v_nombre_limpio, v_nuevo_orden);
        
        SET p_idSubCategoria = v_nuevo_id;

        -- 8. Liberar el candado tras el éxito
        DO RELEASE_LOCK(v_lock_name);
    END;
END //

DELIMITER ;

DELIMITER //

DROP PROCEDURE IF EXISTS modificar_subcategoria//

CREATE PROCEDURE modificar_subcategoria(
    IN p_idCategoria INT,
    IN p_idSubCategoria INT,
    IN p_nombre VARCHAR(30)
)
proc_principal: BEGIN
    DECLARE v_nombre_limpio VARCHAR(30);
    DECLARE v_nombre_actual VARCHAR(30);
    DECLARE v_existe_subcategoria INT;
    DECLARE v_existe_duplicado INT;

    -- 1. Validar nombre
    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre de la subcategoría no puede ser nulo, vacío ni estar compuesto solo por espacios.';
    END IF;

    SET v_nombre_limpio = TRIM(p_nombre);

    -- 2. Verificar que la subcategoría exista en la categoría especificada
    SELECT COUNT(*), MAX(nombre) INTO v_existe_subcategoria, v_nombre_actual
    FROM Vert.SubCategorias
    WHERE idSubCategoria = p_idSubCategoria AND idCategoria = p_idCategoria;

    IF v_existe_subcategoria = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La subcategoría que intenta modificar no existe.';
    END IF;

    -- 3. Si el nombre nuevo es exactamente igual al que ya tiene, salir
    IF v_nombre_actual = v_nombre_limpio THEN
        LEAVE proc_principal;
    END IF;

    -- 4. Validar que el nuevo nombre no esté en uso por otra subcategoría de la misma categoría
    SELECT COUNT(*) INTO v_existe_duplicado
    FROM Vert.SubCategorias
    WHERE nombre = v_nombre_limpio 
      AND idCategoria = p_idCategoria
      AND idSubCategoria != p_idSubCategoria;

    IF v_existe_duplicado > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nuevo nombre ya se encuentra en uso por otra subcategoría de la categoría.';
    END IF;

    -- 5. Actualizar
    UPDATE Vert.SubCategorias
    SET nombre = v_nombre_limpio
    WHERE idSubCategoria = p_idSubCategoria AND idCategoria = p_idCategoria;

END //

DELIMITER ;

DELIMITER //

DROP PROCEDURE IF EXISTS cambiar_orden_subcategoria//

CREATE PROCEDURE cambiar_orden_subcategoria(
    IN p_idCategoria INT,
    IN p_idSubCategoria INT,
    IN p_orden SMALLINT
)
proc_principal: BEGIN
    DECLARE v_orden_actual SMALLINT;
    DECLARE v_max_orden SMALLINT;
    DECLARE v_existe INT;
    DECLARE v_nuevo_orden SMALLINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- 1. Validar que la subcategoría exista en la categoría especificada
    SELECT COUNT(*), MAX(orden) INTO v_existe, v_orden_actual
    FROM Vert.SubCategorias
    WHERE idSubCategoria = p_idSubCategoria AND idCategoria = p_idCategoria;

    IF v_existe = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La subcategoría especificada no existe.';
    END IF;

    -- 2. Obtener el máximo orden actual dentro de la categoría
    SELECT IFNULL(MAX(orden), 1) INTO v_max_orden 
    FROM Vert.SubCategorias
    WHERE idCategoria = p_idCategoria;

    SET v_nuevo_orden = p_orden;
    
    IF v_nuevo_orden < 1 THEN
        SET v_nuevo_orden = 1;
    ELSEIF v_nuevo_orden > v_max_orden THEN
        SET v_nuevo_orden = v_max_orden;
    END IF;

    -- 3. Si el orden ya es el mismo, salir
    IF v_orden_actual = v_nuevo_orden THEN
        LEAVE proc_principal;
    END IF;

    -- 4. Iniciar transacción segura
    START TRANSACTION;

    -- 5. Desplazar las demás subcategorías de la categoría
    IF v_nuevo_orden < v_orden_actual THEN
        -- Sube
        UPDATE Vert.SubCategorias
        SET orden = orden + 1
        WHERE idCategoria = p_idCategoria
          AND orden >= v_nuevo_orden 
          AND orden < v_orden_actual;
    ELSE
        -- Baja
        UPDATE Vert.SubCategorias
        SET orden = orden - 1
        WHERE idCategoria = p_idCategoria
          AND orden > v_orden_actual 
          AND orden <= v_nuevo_orden;
    END IF;

    -- 6. Asignar nuevo orden a la subcategoría seleccionada
    UPDATE Vert.SubCategorias
    SET orden = v_nuevo_orden
    WHERE idSubCategoria = p_idSubCategoria AND idCategoria = p_idCategoria;

    -- Confirmar los cambios
    COMMIT;

END //

DELIMITER ;

DELIMITER //

DROP PROCEDURE IF EXISTS nuevo_producto//

CREATE PROCEDURE nuevo_producto(
    IN p_idCategoria INT,
    IN p_idSubCategoria INT,
    IN p_nombre VARCHAR(30),
    IN p_precio DECIMAL(9,2),
    IN p_precioDescuento DECIMAL(9,2),
    IN p_descripcion VARCHAR(255),
    IN p_imgUrl VARCHAR(300),
    IN p_cantidad TINYINT,
    OUT p_idProducto INT
)
BEGIN
    DECLARE v_nombre_limpio VARCHAR(30);
    DECLARE v_existe_subcategoria INT;
    DECLARE v_existe_nombre INT;
    DECLARE v_nuevo_id INT;
    DECLARE v_lock_adquirido INT;

    -- 1. Intentar adquirir un candado (espera máximo 5 segundos)
    SELECT GET_LOCK('lock_creacion_producto', 5) INTO v_lock_adquirido;

    IF v_lock_adquirido = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El sistema está muy ocupado procesando otros productos. Intente nuevamente en unos segundos.';
    END IF;

    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            DO RELEASE_LOCK('lock_creacion_producto');
            RESIGNAL;
        END;

        -- 2. Validar IDs de categoría y subcategoría
        IF p_idCategoria IS NULL THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El ID de la categoría no puede ser nulo.';
        END IF;

        IF p_idSubCategoria IS NULL THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El ID de la subcategoría no puede ser nulo.';
        END IF;

        -- Validar que la subcategoría pertenezca a la categoría
        SELECT COUNT(*) INTO v_existe_subcategoria
        FROM Vert.SubCategorias
        WHERE idSubCategoria = p_idSubCategoria AND idCategoria = p_idCategoria;

        IF v_existe_subcategoria = 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: La subcategoría especificada no existe para la categoría indicada.';
        END IF;

        -- 3. Validar nombre
        IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El nombre del producto no puede ser nulo, vacío ni estar compuesto solo por espacios.';
        END IF;

        SET v_nombre_limpio = TRIM(p_nombre);

        -- 4. Validar precio
        IF p_precio IS NULL OR p_precio < 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El precio del producto debe ser mayor o igual a 0.';
        END IF;

        -- Validar precio de descuento si existe
        IF p_precioDescuento IS NOT NULL AND (p_precioDescuento < 0 OR p_precioDescuento > p_precio) THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El precio de descuento no puede ser negativo ni mayor al precio regular.';
        END IF;

        -- 5. Validar imagen
        IF p_imgUrl IS NULL OR TRIM(p_imgUrl) = '' THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: La URL de la imagen del producto no puede ser nula ni vacía.';
        END IF;

        -- 6. Validar duplicados de nombre
        SELECT COUNT(*) INTO v_existe_nombre 
        FROM Vert.Productos 
        WHERE nombre = v_nombre_limpio;

        IF v_existe_nombre > 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Error: El nombre del producto ya se encuentra en uso.';
        END IF;

        -- 7. Cálculo de nuevo ID
        SELECT IFNULL(MAX(idProducto), 0) + 1 INTO v_nuevo_id 
        FROM Vert.Productos;

        -- 8. Inserción
        INSERT INTO Vert.Productos (idProducto, idSubCategoria, idCategoria, nombre, precio, imgUrl, precioDescuento, descripcion, cantidad)
        VALUES (v_nuevo_id, p_idSubCategoria, p_idCategoria, v_nombre_limpio, p_precio, TRIM(p_imgUrl), p_precioDescuento, p_descripcion, p_cantidad);

        SET p_idProducto = v_nuevo_id;

        -- 9. Liberar el candado tras el éxito
        DO RELEASE_LOCK('lock_creacion_producto');
    END;
END //

DELIMITER ;

DELIMITER //

DROP PROCEDURE IF EXISTS modificar_producto//

CREATE PROCEDURE modificar_producto(
    IN p_idProducto INT,
    IN p_idCategoria INT,
    IN p_idSubCategoria INT,
    IN p_nombre VARCHAR(30),
    IN p_precio DECIMAL(9,2),
    IN p_precioDescuento DECIMAL(9,2),
    IN p_descripcion VARCHAR(255),
    IN p_imgUrl VARCHAR(300),
    IN p_cantidad TINYINT
)
proc_principal: BEGIN
    DECLARE v_nombre_limpio VARCHAR(30);
    DECLARE v_existe_producto INT;
    DECLARE v_existe_subcategoria INT;
    DECLARE v_existe_duplicado INT;

    -- 1. Validar ID producto
    IF p_idProducto IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El ID del producto no puede ser nulo.';
    END IF;

    -- 2. Verificar existencia del producto
    SELECT COUNT(*) INTO v_existe_producto
    FROM Vert.Productos
    WHERE idProducto = p_idProducto;

    IF v_existe_producto = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El producto que intenta modificar no existe.';
    END IF;

    -- 3. Validar categoría y subcategoría
    IF p_idCategoria IS NULL OR p_idSubCategoria IS NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El ID de la categoría y subcategoría no pueden ser nulos.';
    END IF;

    SELECT COUNT(*) INTO v_existe_subcategoria
    FROM Vert.SubCategorias
    WHERE idSubCategoria = p_idSubCategoria AND idCategoria = p_idCategoria;

    IF v_existe_subcategoria = 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La subcategoría especificada no existe para la categoría indicada.';
    END IF;

    -- 4. Validar nombre
    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nombre del producto no puede ser nulo, vacío ni estar compuesto solo por espacios.';
    END IF;

    SET v_nombre_limpio = TRIM(p_nombre);

    -- 5. Validar duplicados de nombre en otros productos
    SELECT COUNT(*) INTO v_existe_duplicado
    FROM Vert.Productos
    WHERE nombre = v_nombre_limpio AND idProducto != p_idProducto;

    IF v_existe_duplicado > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El nuevo nombre ya se encuentra en uso por otro producto.';
    END IF;

    -- 6. Validar precio
    IF p_precio IS NULL OR p_precio < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El precio del producto debe ser mayor o igual a 0.';
    END IF;

    IF p_precioDescuento IS NOT NULL AND (p_precioDescuento < 0 OR p_precioDescuento > p_precio) THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: El precio de descuento no puede ser negativo ni mayor al precio regular.';
    END IF;

    -- 7. Validar imgUrl
    IF p_imgUrl IS NULL OR TRIM(p_imgUrl) = '' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Error: La URL de la imagen no puede ser nula ni vacía.';
    END IF;

    -- 8. Actualizar producto
    UPDATE Vert.Productos
    SET idCategoria = p_idCategoria,
        idSubCategoria = p_idSubCategoria,
        nombre = v_nombre_limpio,
        precio = p_precio,
        precioDescuento = p_precioDescuento,
        descripcion = p_descripcion,
        imgUrl = TRIM(p_imgUrl),
        cantidad = p_cantidad
    WHERE idProducto = p_idProducto;

END //

DELIMITER ;