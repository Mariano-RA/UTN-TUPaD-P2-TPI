
-- Crear base si no existe
CREATE DATABASE IF NOT EXISTS vehiculo_seguro
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vehiculo_seguro;

DROP TABLE IF EXISTS Vehiculo;
DROP TABLE IF EXISTS SeguroVehicular;
DROP TABLE IF EXISTS TipoCobertura;

-- =========================================
-- Catálogo: TipoCobertura
-- =========================================
CREATE TABLE TipoCobertura (
  id         SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo     VARCHAR(20)  NOT NULL UNIQUE,      
  nombre     VARCHAR(80)  NOT NULL,
  eliminado  BOOLEAN NOT NULL DEFAULT FALSE,
  orden      SMALLINT UNSIGNED DEFAULT 0,

  CONSTRAINT ck_tc_codigo_not_blank CHECK (TRIM(codigo) <> ''),
  CONSTRAINT ck_tc_nombre_not_blank CHECK (TRIM(nombre) <> '')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO TipoCobertura (codigo, nombre, orden) VALUES
  ('RC',          'Responsabilidad Civil', 1),
  ('TERCEROS',    'Terceros',              2),
  ('TODO_RIESGO', 'Todo Riesgo',           3)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), orden = VALUES(orden);

-- =========================================
-- Tabla B: SeguroVehicular
-- =========================================
CREATE TABLE SeguroVehicular (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  eliminado        BOOLEAN NOT NULL DEFAULT FALSE,           
  aseguradora      VARCHAR(80) NOT NULL,
  nro_poliza       VARCHAR(50) UNIQUE,                           
  tipo_cobertura   SMALLINT UNSIGNED NOT NULL,         
  vencimiento      DATE NOT NULL,

  -- Checks de dominio
  CONSTRAINT ck_seg_aseguradora_not_blank CHECK (TRIM(aseguradora) <> ''),
  CONSTRAINT ck_seg_poliza_not_blank CHECK (nro_poliza IS NULL OR TRIM(nro_poliza) <> ''),


  CONSTRAINT fk_seg_tipocob FOREIGN KEY (tipo_cobertura)
    REFERENCES TipoCobertura(id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =========================================
-- Tabla A: Vehiculo
-- =========================================
CREATE TABLE Vehiculo (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  eliminado     BOOLEAN NOT NULL DEFAULT FALSE,               
  dominio       VARCHAR(10) NOT NULL,                     
  marca         VARCHAR(50) NOT NULL,
  modelo        VARCHAR(50) NOT NULL,
  anio          INT,                                            
  nro_chasis    VARCHAR(50),
  seguro_id     BIGINT NOT NULL UNIQUE,                        

  -- Unicidades de identificadores
  CONSTRAINT uq_vehiculo_dominio    UNIQUE (dominio),
  CONSTRAINT uq_vehiculo_nro_chasis UNIQUE (nro_chasis),

  -- Checks de dominio
  CONSTRAINT ck_veh_dominio_not_blank CHECK (TRIM(dominio) <> ''),
  CONSTRAINT ck_veh_marca_not_blank   CHECK (TRIM(marca)   <> ''),
  CONSTRAINT ck_veh_modelo_not_blank  CHECK (TRIM(modelo)  <> ''),
  CONSTRAINT ck_veh_anio_rango        CHECK (anio IS NULL OR (anio BETWEEN 1886 AND 2100)),
  CONSTRAINT ck_veh_dominio_len       CHECK (CHAR_LENGTH(dominio) IN (6,7)),

  -- 1→1 unidireccional (A → B): A (Vehiculo) referencia a B (Seguro)
  CONSTRAINT fk_vehiculo_seguro FOREIGN KEY (seguro_id)
    REFERENCES SeguroVehicular (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
