# CPC-2024 — Product image migration and cutover

## Current implementation

Product image reads, uploads and new catalog seeding use Files Service exclusively. Both PROD frontends consume embedded product image data. The /images endpoint preserves its response contract for CART while using Files Service only. The legacy image repository is read-only and used solely by the migration; original rows are retained for recovery, not served as a fallback.

Both repository Compose configurations enable `APP_IMAGE_MIGRATION_ENABLED=true` for products-service, so deployment runs the migration before cutover. Outside Compose, migration remains opt-in. On an existing environment, back up the product database and Files Service storage, pause product/image writes, and run one products-service instance during migration. Verify migration before routing traffic to the cutover build. Startup refuses to continue if products still reference rows in the legacy images table. Fresh installations use `app.seed-data.enabled=true` (the default). Tests disable automatic seeding; deployments can also disable it explicitly.

Products-service depends on the Files Service container starting, then polls its existing `/metrics` HTTP endpoint before migration or catalog seeding. The wait runs when either migration or seeding is enabled. With both disabled (as in the test profile), startup still checks for legacy references but does not contact Files Service. Individual requests time out after five seconds, retries wait two seconds, and the complete wait is limited to two minutes. This checks HTTP startup availability, not ongoing storage health. If the wait fails, no migration uploads have been claimed. Uploads themselves are not blindly retried because a lost response could otherwise create duplicate files. Existing journal and byte-verification safeguards still handle upload failures.

Migration runs before catalog seeding and processes every legacy image, including unreferenced rows. Each upload is read back and compared against original bytes and MIME type before product IDs are replaced. The link update and VERIFIED journal state are transactional. Products whose image changed meanwhile are not overwritten. A failed item is logged and processing continues; any failure fails startup.

Fresh seed images are uploaded and verified before new products are saved, using `seed:` journal keys distinct from legacy image IDs. Restarting after a product-save failure reuses the uploaded file. Existing products skip seed publication entirely, preserving employee edits and deleted images. Files Service and other teams' code are unchanged.

## Journal and safe reruns

`product_image_migration` records each legacy ID, new file ID and state:

- `PENDING`: no upload has been claimed; safe to attempt.
- `UPLOADING`: request may have reached Files Service. No automatic repeat upload.
- `UPLOADED`: new ID is saved; rerun reads/verifies the same file and attempts linking again.
- `VERIFIED`: file and product-link transaction succeeded; rerun skips this completed entry, preserving subsequent employee image edits/deletions. A migration rerun is not a file-health audit.

Files Service creates its own IDs and has no idempotency key. A connection loss or process crash after a POST can therefore leave an uncertain outcome. For `UPLOADING`, inspect Files Service records/storage and compare the candidate file's bytes and MIME type with the source. If an upload exists, record its verified file ID and set the journal state to `UPLOADED`. Only reset to `PENDING` when it is established that no upload exists and no migration instance remains active. Do not guess an ID or blindly reset all entries. Retain the originals throughout reconciliation.

A readback failure with a recorded file ID is retried against that same ID. Never delete the source to resolve an error. Filename equality is deliberately not required because Files Service normalizes filenames.

## Deployment verification

1. Run the opt-in migration against the intended environment and retain its logs.
2. Confirm every source row has a verified journal entry:

```sql
SELECT i.image_id, m.state, m.file_id
FROM images i LEFT JOIN product_image_migration m ON m.legacy_image_id = i.image_id
WHERE m.state IS DISTINCT FROM 'VERIFIED';
```

3. Confirm no product references a legacy image:

```sql
SELECT p.product_id, p.image_id
FROM products p JOIN images i ON p.image_id = i.image_id;
```

Both queries must return no rows. Also check any orphan product IDs separately; this migration cannot recreate non-seeded image bytes that are already missing.

4. Rerun with no new source rows; confirm no new uploads and unchanged product IDs.
5. In both customer and employee fronts, inspect product lists and detail views, shared images, and a product that already used Files Service before migration. Save screenshots and failed-request logs, if any. Confirm image upload/update still works.
6. Disable the migration flag after a successful run. Keep originals and the journal through the verification period.
7. Retain the read-only source table and journal for recovery. This change removes runtime legacy reads/writes and frontend fallback; it deliberately does not drop original data. Do not route traffic to this build until migration and image verification succeed.

## Automated checks

```bash
./gradlew :products-service:test
```

Use `gradlew.bat` on Windows. PostgreSQL integration tests require Docker. Tests cover upload/readback failure, uncertain-upload protection, saved-ID reruns, concurrent claims, shared references, transactional rollback, employee edits and the frontend-compatible image response.

## Isolated verification completed on October 25, 2026

The original migration stage passed 237 products-service tests. After adjusting completed-entry reruns to preserve later image edits, all 10 migration unit tests passed again. The PROD frontend fallback removal passed targeted ESLint, customer TypeScript, Prettier checks and 36 employee PROD tests using an isolated PROD test configuration. The normal Angular test command also compiles unrelated failing tests; no other teams' files were changed.

A separate local PostgreSQL/MySQL/MinIO setup with the existing local Files Service image migrated 40 seeded images, retained all 40 originals and left zero legacy product references. Browser checks verified both product catalogs/details after removing PROD frontend fallback requests. An employee UI image replacement appeared in the customer view and survived a migration rerun with zero new uploads. The test image was restored afterward. Employee authentication was a test fixture and the gateway was bypassed; this is local image-flow evidence, not deployed-environment verification.

The October 25 results above describe the earlier migration/frontend stage. The October 26 backend cutover removes legacy runtime storage and seeding. Its final verification results are recorded separately below. No legacy table drop is included.

## Backend cutover verification — October 26, 2026

Verified on main 0f520a95d plus this change: 245 products-service tests and the required coverage check passed. All 36 employee PROD tests passed. Customer TypeScript, targeted frontend lint and changed-file Prettier checks passed. Full customer lint stopped at an import/namespace tool error in unrelated customers/AddPetModal.tsx with the reused local dependency installation; clean-install CI remains the authority for repository-wide lint.

Fresh isolated startup created 40 products, 40 verified Files Service seed entries and zero legacy image rows. A second startup made zero uploads. The existing migrated database retained all 40 originals with zero legacy product references; its rerun also made zero uploads. All 40 products in each database returned matching nonempty image bytes through list/detail/compatibility APIs. Both frontend catalog/detail browser checks passed against the cutover backend without legacy-image requests or image failures. This setup uses a test employee identity and bypasses the gateway; no real shop data was migrated.
