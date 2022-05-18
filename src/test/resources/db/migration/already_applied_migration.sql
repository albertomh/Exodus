-- Used by `testCSETriggersMigrationsWithPopulatedDatabase` to verify
-- existing, applied migrations are not picked up again.

SELECT count(*) AS "table_count"
FROM information_schema.tables
WHERE LOWER(table_schema) = 'public';
