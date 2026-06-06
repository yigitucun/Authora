
DO $$
DECLARE
    old_id  uuid;
    new_id  uuid;
    pair    RECORD;
BEGIN
    FOR pair IN
        SELECT
            old_ct.id  AS old_id,
            new_ct.id  AS new_id,
            new_ct.name AS canonical_name
        FROM connection_types old_ct
        JOIN connection_types new_ct
            ON LOWER(old_ct.name) = LOWER(new_ct.name)
            AND old_ct.id <> new_ct.id
            -- "old" row = the one with empty settings_schema
            AND (old_ct.settings_schema = '[]'::jsonb OR old_ct.settings_schema IS NULL)
            -- "new" row = the one with a populated settings_schema
            AND new_ct.settings_schema <> '[]'::jsonb
    LOOP
        -- Move any app_connections pointing at the old row
        -- to the new canonical row, skipping pairs that would
        -- violate the unique (client_id, connection_type_id) constraint.
        UPDATE app_connections
        SET connection_type_id = pair.new_id
        WHERE connection_type_id = pair.old_id
          AND NOT EXISTS (
              SELECT 1 FROM app_connections
              WHERE client_id = app_connections.client_id
                AND connection_type_id = pair.new_id
          );

        -- Delete any leftover duplicates that couldn't be migrated
        DELETE FROM app_connections WHERE connection_type_id = pair.old_id;
    END LOOP;
END $$;

-- Step 2: Delete the old ALLCAPS / empty-schema rows that are
-- now no longer referenced by any app_connections.
DELETE FROM connection_types
WHERE (settings_schema = '[]'::jsonb OR settings_schema IS NULL)
  AND LOWER(name) IN ('google', 'github', 'facebook')
  AND NOT EXISTS (
      SELECT 1 FROM app_connections
      WHERE app_connections.connection_type_id = connection_types.id
  );

-- Step 3: Add a case-insensitive unique index on name so future
-- seeds can never create case-variant duplicates again.
-- (The existing btree unique index on name remains; this adds
--  an expression index on LOWER(name) as a guard.)
CREATE UNIQUE INDEX IF NOT EXISTS uq_connection_types_name_lower
    ON connection_types (LOWER(name));
