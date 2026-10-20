INSERT IGNORE INTO roles (id, code, description)
VALUES ('166ef69f-03bd-4f28-920c-0627aac2071d', 'API_GATEWAY_DEV', 'API Gateway Dev');

UPDATE services
SET service_role = '166ef69f-03bd-4f28-920c-0627aac2071d'
WHERE docker_service = 'api-gateway'
  AND service_role IS NULL;