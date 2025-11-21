USE vehiculo_seguro;

-- Parámetros
SET @N := 25;    
SET @OFFSET := 0;     
DROP TEMPORARY TABLE IF EXISTS seq;
CREATE TEMPORARY TABLE seq (n INT PRIMARY KEY) ENGINE=Memory;

-- Generador 1..@N (vía producto cartesiano de dígitos)
INSERT INTO seq (n)
SELECT x.n
FROM (
  SELECT d0.v + 10*d1.v + 100*d2.v + 1000*d3.v + 10000*d4.v + 100000*d5.v + 1 AS n
  FROM (SELECT 0 v UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d0
  CROSS JOIN (SELECT 0 v UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d1
  CROSS JOIN (SELECT 0 v UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d2
  CROSS JOIN (SELECT 0 v UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d3
  CROSS JOIN (SELECT 0 v UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d4
  CROSS JOIN (SELECT 0 v UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d5
) x
WHERE x.n <= @N;


SET @PREF_POL := 'POL';
SET @WIN_DIAS := 720;

START TRANSACTION;
-- Base en función del estado actual (importante para corridas incrementales)
SET @base_pol := COALESCE((SELECT MAX(CAST(SUBSTRING(nro_poliza, 4) AS UNSIGNED))
                           FROM SeguroVehicular
                           WHERE nro_poliza REGEXP '^[A-Z]{3}[0-9]+$'), 0);

INSERT INTO SeguroVehicular (aseguradora, nro_poliza, tipo_cobertura, vencimiento)
SELECT
  ELT(1 + ((@OFFSET + s.n) % 6), 'Sancor','Allianz','Mapfre','Provincia','La Segunda','Zurich') AS aseguradora,
  CONCAT(@PREF_POL, LPAD(@base_pol + s.n, 7, '0'))                                              AS nro_poliza,
  tc.id                                                                                          AS tipo_cobertura,
  CURRENT_DATE + INTERVAL ((@OFFSET + s.n) % @WIN_DIAS) DAY                                      AS vencimiento
FROM seq s
JOIN (
  SELECT 'RC' AS codigo UNION ALL
  SELECT 'TERCEROS' UNION ALL
  SELECT 'TODO_RIESGO'
) m ON m.codigo = CASE ((@OFFSET + s.n) % 3)
                    WHEN 0 THEN 'RC'
                    WHEN 1 THEN 'TERCEROS'
                    ELSE 'TODO_RIESGO'
                  END
JOIN TipoCobertura tc ON tc.codigo = m.codigo;
COMMIT;


START TRANSACTION;
SET @PREF_DOM := 'DX';
SET @PREF_CH  := 'CH';
SET @ANIO_BASE := 1995;
SET @ANIO_RANGO := 30; 

INSERT INTO Vehiculo (dominio, marca, modelo, anio, nro_chasis, seguro_id)
SELECT
  LPAD(UPPER(CONV(s.id, 10, 36)), 6, '0')  											 AS dominio,
  ELT(1 + (s.id % 6), 'Toyota','VW','Ford','Fiat','Renault','Chevrolet')             AS marca,
  ELT(1 + (s.id % 6), 'Base','LX','SE','Active','Cross','GT')                        AS modelo,
  @ANIO_BASE + (s.id % @ANIO_RANGO)                                                  AS anio,
  CONCAT(@PREF_CH, LPAD(s.id, 8, '0'))                                               AS nro_chasis,
  s.id                                                                               AS seguro_id
FROM SeguroVehicular s
LEFT JOIN Vehiculo v ON v.seguro_id = s.id
WHERE v.seguro_id IS NULL;
COMMIT;

