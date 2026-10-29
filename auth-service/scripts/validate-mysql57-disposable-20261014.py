"""Explicit disposable-only validation; never targets an application database."""
import json
import pathlib
import subprocess
import time
import uuid

ROOT = pathlib.Path(__file__).resolve().parents[2]
NAME = 'auth-cpc-2062-validation-' + uuid.uuid4().hex[:12]
REPORT = ROOT / (NAME + '-results.json')
result = {'container': NAME, 'image': 'mysql:5.7', 'scenarios': []}

def docker(*args, sql=None, check=True):
    p = subprocess.run(['docker', *args], input=sql, text=True, capture_output=True)
    if check and p.returncode:
        raise RuntimeError(p.stderr.strip() or p.stdout.strip())
    return p

def query(sql, check=True):
    return docker('exec', '-i', NAME, 'mysql', '-uroot', '--batch', '--raw',
                  '--database=auth-db', sql=sql, check=check)

def snapshot():
    return {table: query('SELECT * FROM ' + table + ' ORDER BY id;').stdout
            for table in ['roles', 'users', 'users_roles', 'reset_password_token']}

schema = (ROOT / 'auth-service/src/main/resources/schema-mysql.sql').read_text()
migration = (ROOT / 'auth-service/scripts/rename-owner-to-cust.sql').read_text()
verification = (ROOT / 'auth-service/scripts/verify-customer-role.sql').read_text()

def fixture(kind, empty=False, failure=False):
    query('DROP DATABASE `auth-db`; CREATE DATABASE `auth-db`;')
    query(schema)
    query("""INSERT INTO users(id,user_id,email,username,password,verified,is_disabled) VALUES
      (7,'fixture-public-7','fixture7@example.invalid','fixture7','fixture-only',1,0),
      (19,'fixture-public-19','fixture19@example.invalid','fixture19','fixture-only',0,1),
      (31,'fixture-public-31','fixture31@example.invalid','fixture31','fixture-only',1,0);
      INSERT INTO roles(id,name) VALUES(5,'ADMIN'),(80,'VET');
      INSERT INTO users_roles(id,user_id,role_id) VALUES(11,7,5),(12,31,80);
      INSERT INTO reset_password_token VALUES(1,'fixture-only',7,'2030-01-01');""")
    if kind in ('owner', 'both'):
        query("INSERT INTO roles VALUES(42,'OWNER');")
    if kind in ('cust', 'both'):
        query("INSERT INTO roles VALUES(63,'CUST');")
    if not empty and kind in ('owner', 'both'):
        query('INSERT INTO users_roles VALUES(101,7,42),(102,7,42),(103,19,42);')
    if kind in ('cust', 'both'):
        query('INSERT INTO users_roles VALUES(201,31,63),(202,31,63);')
    if failure:
        query("""DELIMITER $$
CREATE TRIGGER fail_role_update BEFORE UPDATE ON roles FOR EACH ROW
BEGIN
 UPDATE users SET verified=0 WHERE id=7;
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Injected UPDATE failure';
END$$
DELIMITER ;
""")

def run_case(label, kind=None, empty=False, failure=False, repeat=False):
    if not repeat:
        fixture(kind, empty, failure)
    before = snapshot()
    vb = query(verification)
    execution = query(migration, check=False)
    after = snapshot()
    va = query(verification)
    expected = dict(before)
    rename = kind == 'owner' and not failure and not repeat
    if rename:
        expected['roles'] = before['roles'].replace('42\tOWNER\n', '42\tCUST\n')
    reject = kind in ('both', 'neither') or failure
    expected_message = ('Injected UPDATE failure' if failure else
        'OWNER and CUST both exist' if kind == 'both' else
        'neither OWNER nor CUST exists' if kind == 'neither' else
        'Renamed OWNER to CUST' if rename else
        'migration history is not verified')
    passed = (after == expected and (execution.returncode != 0) == reject
              and expected_message in execution.stdout + execution.stderr)
    result['scenarios'].append({
        'name': label, 'passed': passed, 'exit_code': execution.returncode,
        'stdout': execution.stdout, 'stderr': execution.stderr,
        'before': before, 'after': after,
        'verification_before': vb.stdout, 'verification_after': va.stdout,
        'full_snapshot_matches_expected': after == expected})
    print(label + ': ' + ('PASS' if passed else 'FAIL'), flush=True)
    if not passed:
        raise AssertionError(label)

created = False
try:
    docker('run', '--detach', '--name', NAME, '--label', 'auth-cpc-2062=disposable-validation',
           '--network', 'none', '--tmpfs', '/var/lib/mysql:rw,size=1g',
           '--env', 'MYSQL_ALLOW_EMPTY_PASSWORD=yes', '--env', 'MYSQL_DATABASE=auth-db',
           'mysql:5.7')
    created = True
    for attempt in range(90):
        ping = docker('exec', NAME, 'mysqladmin', '-uroot', 'ping', check=False)
        if ping.returncode == 0:
            # Entrypoint briefly runs a temporary initialization server.
            state = docker('exec', NAME, 'sh', '-c', 'test "$(cat /proc/1/comm)" = mysqld', check=False)
            if state.returncode == 0:
                break
        time.sleep(2)
    else:
        raise RuntimeError('Disposable MySQL did not become ready')
    result['server_version'] = query('SELECT VERSION();').stdout.strip()
    print(result['server_version'], flush=True)
    result['storage_network'] = docker('inspect', '--format',
        '{{json .HostConfig.Tmpfs}} {{.HostConfig.NetworkMode}} {{json .HostConfig.PortBindings}} {{json .Mounts}}', NAME).stdout.strip()
    run_case('1 OWNER only', 'owner')
    run_case('6 repeated execution', repeat=True)
    run_case('2 CUST only', 'cust')
    run_case('3 both roles', 'both')
    run_case('4 neither role', 'neither')
    run_case('5 OWNER without memberships', 'owner', empty=True)
    run_case('7 induced UPDATE failure and trigger-side-effect rollback', 'owner', failure=True)
except Exception as exc:
    result['error'] = str(exc)
    print('ERROR: ' + str(exc), flush=True)
finally:
    if created:
        cleanup = docker('rm', '--force', NAME, check=False)
        result['cleanup_exit_code'] = cleanup.returncode
        print('Disposable container cleanup exit: ' + str(cleanup.returncode), flush=True)
        result['cleanup_confirmed_absent'] = docker('inspect', NAME, check=False).returncode != 0
    REPORT.write_text(json.dumps(result, indent=2), encoding='utf-8')
    print('Report: ' + str(REPORT), flush=True)
if ('error' in result or not all(s['passed'] for s in result['scenarios'])
        or len(result['scenarios']) != 7 or result.get('cleanup_exit_code') != 0
        or not result.get('cleanup_confirmed_absent')):
    raise SystemExit(1)
