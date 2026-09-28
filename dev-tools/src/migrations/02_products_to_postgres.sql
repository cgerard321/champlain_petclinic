DELETE FROM service_dbs
WHERE service_docker_service = 'products-service'
  AND db_type = 'MONGO';

INSERT IGNORE INTO service_dbs
    (service_docker_service, db_name, db_user_env, db_password_env, db_host, db_type)
SELECT 'products-service', 'products-db', 'DB_USER', 'DB_PASSWORD', 'postgres-products', 'POSTGRES'
FROM services
WHERE docker_service = 'products-service';