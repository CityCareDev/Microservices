-- Drop the old assigned_doctor_id column if it exists (renamed to assigned_staff_id)
SET @dbname = DATABASE();
SET @tablename = 'patients';
SET @columnname = 'assigned_doctor_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' DROP COLUMN ', @columnname),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;
