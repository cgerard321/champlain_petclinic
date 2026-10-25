-- Manual AUTH-CPC-2062 maintenance only. Read README.md first.
-- Select the intended AUTH database in the client; no hard-coded database or ID.
-- Use a dedicated connection, stop on errors, and keep application writers stopped.
-- MySQL 5.7 syntax. Routine DDL is outside the data transaction.
DELIMITER $$
CREATE PROCEDURE auth_cpc_2062_rename_owner_to_cust()
SQL SECURITY INVOKER
BEGIN
    DECLARE owner_count INT DEFAULT 0;
    DECLARE cust_count INT DEFAULT 0;
    DECLARE customer_role_id BIGINT;
    DECLARE transactional_tables INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SELECT COUNT(*) INTO transactional_tables
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN ('roles', 'users', 'users_roles')
      AND engine = 'InnoDB';
    IF transactional_tables <> 3 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'AUTH-CPC-2062: expected roles, users, users_roles as InnoDB tables; stopped';
    END IF;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    -- Lock the entire small roles table, including insertion gaps, before checks.
    -- Do not narrow this to an aggregate or a snapshot read.
    SELECT id, name AS locked_role_name FROM roles ORDER BY id FOR UPDATE;

    SELECT COUNT(*) INTO owner_count FROM roles WHERE name = 'OWNER';
    SELECT COUNT(*) INTO cust_count FROM roles WHERE name = 'CUST';

    IF owner_count > 0 AND cust_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'AUTH-CPC-2062 conflict: OWNER and CUST both exist; no data changed';
    ELSEIF owner_count = 0 AND cust_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'AUTH-CPC-2062 missing role: neither OWNER nor CUST exists; no data changed';
    ELSEIF owner_count > 1 OR cust_count > 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'AUTH-CPC-2062: duplicate customer role names; no data changed';
    ELSEIF owner_count = 0 THEN
        COMMIT;
        SELECT 'No rename needed: CUST exists and OWNER is absent; migration history is not verified' AS result;
    ELSE
        SELECT id INTO customer_role_id FROM roles WHERE name = 'OWNER';
        UPDATE roles SET name = 'CUST' WHERE id = customer_role_id AND name = 'OWNER';
        IF ROW_COUNT() <> 1 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'AUTH-CPC-2062: expected exactly one renamed row; rolled back';
        END IF;
        COMMIT;
        SELECT 'Renamed OWNER to CUST; role ID and memberships preserved' AS result,
               customer_role_id AS role_id;
    END IF;
END$$
DELIMITER ;

CALL auth_cpc_2062_rename_owner_to_cust();
DROP PROCEDURE auth_cpc_2062_rename_owner_to_cust;
