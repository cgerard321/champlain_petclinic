CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  pass_hash VARBINARY(128) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS roles (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id VARCHAR(36) NOT NULL,
  role_id VARCHAR(36) NOT NULL,
  PRIMARY KEY (user_id, role_id),
  INDEX idx_user_roles_user_id (user_id),
  INDEX idx_user_roles_role_id (role_id),
  CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_user_roles_role
    FOREIGN KEY (role_id) REFERENCES roles(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sessions (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  INDEX idx_sessions_user_id (user_id),
  CONSTRAINT fk_sessions_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS services (
  docker_service VARCHAR(255) NOT NULL PRIMARY KEY,
  service_role VARCHAR(36) NULL,
  INDEX idx_services_service_role (service_role),
  CONSTRAINT fk_services_role
    FOREIGN KEY (service_role) REFERENCES roles(id)
    ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS service_dbs (
  service_docker_service VARCHAR(255) NOT NULL,
  db_name VARCHAR(255) NOT NULL,
  db_user_env VARCHAR(255) NOT NULL,
  db_password_env VARCHAR(255) NOT NULL,
  db_host VARCHAR(255) NOT NULL,
  db_type ENUM('MONGO','MYSQL','POSTGRES','UNKNOWN') NOT NULL,
  PRIMARY KEY (service_docker_service, db_name),
  INDEX idx_service_dbs_service (service_docker_service),
  CONSTRAINT fk_service_dbs_service
    FOREIGN KEY (service_docker_service) REFERENCES services(docker_service)
    ON DELETE CASCADE
);
