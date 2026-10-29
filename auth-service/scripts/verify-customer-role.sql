-- Read-only. Run before AND after the update while writers remain stopped.
-- Save both outputs. Compare the customer role ID and membership tuples,
-- allowing only the role name to change from OWNER to CUST.
SELECT DATABASE() AS selected_database, VERSION() AS server_version;
SELECT table_name, engine FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name IN ('roles', 'users', 'users_roles')
ORDER BY table_name;

SELECT id, name FROM roles ORDER BY id;

-- Full customer membership rows: preserves duplicate assignments too.
SELECT ur.id AS membership_id, ur.user_id, ur.role_id, u.user_id AS public_user_id
FROM users_roles ur
JOIN roles r ON r.id = ur.role_id
JOIN users u ON u.id = ur.user_id
WHERE r.name IN ('OWNER', 'CUST')
ORDER BY ur.role_id, ur.user_id, ur.id;

-- Distinct customer membership set, separately from assignment-row counts.
SELECT DISTINCT ur.role_id, ur.user_id
FROM users_roles ur JOIN roles r ON r.id = ur.role_id
WHERE r.name IN ('OWNER', 'CUST')
ORDER BY ur.role_id, ur.user_id;

SELECT r.id AS role_id, r.name, COUNT(ur.id) AS membership_rows,
       COUNT(DISTINCT ur.user_id) AS distinct_users
FROM roles r LEFT JOIN users_roles ur ON ur.role_id = r.id
WHERE r.name IN ('OWNER', 'CUST')
GROUP BY r.id, r.name ORDER BY r.id;
