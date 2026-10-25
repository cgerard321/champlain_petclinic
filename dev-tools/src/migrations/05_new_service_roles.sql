INSERT IGNORE INTO roles (id, code, description) VALUES
    ('5947f103-798c-40a9-921c-4b29a9b2967a', 'MAILER_SERVICE_DEV', 'Mailer Service Dev'),
    ('dca9bb01-095e-4a93-b1a6-9e373979ab87', 'FILES_SERVICE_DEV', 'Files Service Dev'),
    ('7e6bb3c0-181f-47c4-84fe-0c3e0d7e9f98', 'PETCLINIC_FRONTEND_DEV', 'Petclinic Frontend Dev'),
    ('9bb4041d-8dc7-403e-8641-c1d888684c5a', 'EMPLOYEE_FRONTEND_DEV', 'Employee Frontend Dev');

UPDATE services SET service_role = '5947f103-798c-40a9-921c-4b29a9b2967a'
WHERE docker_service = 'mailer-service' AND service_role IS NULL;

UPDATE services SET service_role = 'dca9bb01-095e-4a93-b1a6-9e373979ab87'
WHERE docker_service = 'files-service' AND service_role IS NULL;

UPDATE services SET service_role = '7e6bb3c0-181f-47c4-84fe-0c3e0d7e9f98'
WHERE docker_service = 'petclinic-frontend' AND service_role IS NULL;

UPDATE services SET service_role = '9bb4041d-8dc7-403e-8641-c1d888684c5a'
WHERE docker_service = 'employee-frontend' AND service_role IS NULL;