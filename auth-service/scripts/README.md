# AUTH customer role update

AUTH startup automatically renames OWNER to CUST before the loader's existing-data
check, preserving role IDs and memberships. CUST-only is a no-op; fresh databases
still seed CUST. Both roles cause startup failure: an operator must reconcile the
conflict before retrying; no roles are merged/deleted. Persistence failures also
abort startup. Existing OWNER sessions require logout/login for new CUST tokens.
The scripts below remain a manual fallback; unlike startup, they reject neither-role
databases. Coordinate older writers during rollout and bootstrap empty databases
with one AUTH instance first.

These scripts are intentionally outside `src/main/resources` and are never run
at startup. AUTH currently has a bootstrap schema, not a migration framework.
`schema-mysql.sql` defines `roles(id, name UNIQUE)` and
`users_roles(id, user_id, role_id)` with foreign keys to `users` and `roles`.
Both root Compose files specify `mysql:5.7`. The update uses MySQL 5.7 syntax,
including `SIGNAL`, `RESIGNAL`, and an InnoDB transaction; no MySQL 8-only syntax.
Check the actual server version and schema before execution. This does not
provide application-wide OWNER compatibility or prove past migration history.

## Execute later

1. Arrange a maintenance window, back up the intended AUTH database, and stop
   all application/admin writers for the entire before/update/after interval.
   Review consumers of role names before resuming them. The role table lock
   protects the precondition checks during the transaction; it does not freeze
   memberships throughout separate verification sessions.
2. Use a dedicated MySQL client connection with the intended AUTH database
   selected. No script contains `USE` or assumes role ID 3. Check `DATABASE()`,
   `VERSION()`, and `SHOW CREATE TABLE` for `roles`, `users`, `users_roles`.
   Confirm the checked-in column/foreign-key structure, unique role names, and
   InnoDB tables, with no unexpected triggers. The script rejects non-InnoDB or
   missing tables. Do not run from a session containing pending work: routine
   creation/removal implicitly commits independently of the data transaction.
3. From the repository root, run the read-only verification and save its output.
   Then run the update, and repeat verification. For example, in a shell with
   input redirection (replace HOST and OPERATOR; password is prompted):

   ```sh
   mysql --host=HOST --user=OPERATOR --password --database=auth-db --batch < auth-service/scripts/verify-customer-role.sql > before.tsv
   mysql --host=HOST --user=OPERATOR --password --database=auth-db --batch < auth-service/scripts/rename-owner-to-cust.sql > update.tsv
   mysql --host=HOST --user=OPERATOR --password --database=auth-db --batch < auth-service/scripts/verify-customer-role.sql > after.tsv
   ```

   PowerShell does not support `<` input redirection; run these examples in
   `cmd.exe` or a Unix shell. Do not use `--force`. Inspect exit status and errors
   after each command; stop on any failure. Run only against an explicitly
   selected target later, never as part of application startup.

   Operator privileges: SELECT on the three tables and metadata visibility,
   UPDATE on `roles`, CREATE ROUTINE, EXECUTE, and ALTER ROUTINE for cleanup.
   The helper uses invoker privileges. It is created, called, and removed by the
   script. If a helper of the same name already exists, CREATE fails; the script
   intentionally does not replace it. Investigate before retrying.
4. Outcomes: OWNER only renames exactly that row to CUST; CUST only reports no
   rename needed, without verifying migration history. Both or neither produce
   a clear SQL error and roll back without changing role/account/membership data.
   Other SQL errors also roll back and are rethrown. No role is merged/deleted,
   and no account or membership is written.
5. Compare saved outputs: every role ID stays the same; only the former OWNER
   name changes. Compare **all sorted membership tuples and the distinct
   (role_id, user_id) set**, plus counts. Counts alone cannot prove identical
   membership: different users can produce the same count. An empty membership
   set is valid. For CUST-only, compare the existing CUST set without inferring
   historical OWNER membership. Keep writers stopped until verification ends.

On a failed CALL, the data transaction rolls back, but the helper may remain
because batch execution stops before DROP. Routine DDL is not transactional.
After inspecting the error and confirming the connection/target, remove only
this helper with `DROP PROCEDURE auth_cpc_2062_rename_owner_to_cust;` before a
retry. Do not resolve a both-role conflict by deleting or merging roles here.

MySQL references:
[locking reads](https://docs.oracle.com/cd/E17952_01/mysql-5.7-en/innodb-locking-reads.html),
[SIGNAL](https://docs.oracle.com/cd/E17952_01/mysql-5.7-en/signal.html),
[implicit commits](https://docs.oracle.com/cd/E17952_01/mysql-5.7-en/implicit-commit.html).

## Validation performed

Executed against a disposable MariaDB 10.4.32 instance using the AUTH schema,
a temporary data directory, and a separate loopback port. Seven scenarios passed:
OWNER only, CUST only, both, neither, empty customer memberships, injected UPDATE
failure, and missing-table rejection. Full role/account/membership snapshots
were compared, including duplicate assignments, multiple users, an unrelated
role, and customer ID 42. Repeat execution after renaming also passed, and the
verification queries ran before/after all scenarios with the complete schema.
The disposable server was shut down and its data directory removed.

Subsequently executed both prepared scripts on an isolated `mysql:5.7` container
reporting **MySQL 5.7.44**. All seven scenarios passed: OWNER only, CUST only,
both roles, neither role, OWNER without memberships, repeated execution after
renaming, and an induced UPDATE failure. Complete ordered snapshots of `roles`,
`users`, `users_roles`, and `reset_password_token` matched the expected results,
including duplicate assignments and unrelated memberships. Only the intended
OWNER name changed in successful rename cases. Rejections and no-ops preserved
all data. The failure trigger changed a user row before raising an error; that
change also rolled back. CUST-only and repeat runs explicitly left migration
history unverified. Missing-table rejection was covered by the earlier MariaDB
run, not this seven-scenario MySQL run.

The MySQL container had networking disabled, no published ports or application
mounts, and `/var/lib/mysql` on temporary memory-backed storage. It was removed
successfully and its absence confirmed. The reproducible fixture harness is
`validate-mysql57-disposable-20261014.py`; run it from the repository with Python
and Docker available. It creates only a uniquely named disposable container and
writes a local JSON evidence report. MariaDB results remain supplemental.

**No application database has been migrated by these disposable validations.**
