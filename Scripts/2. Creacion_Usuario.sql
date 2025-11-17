use vehiculo_seguro;

CREATE USER 'tfi_user'@'localhost' IDENTIFIED BY 'tfi_pass_123';

GRANT ALL PRIVILEGES ON vehiculo_seguro.* TO 'tfi_user'@'localhost';

FLUSH PRIVILEGES;
