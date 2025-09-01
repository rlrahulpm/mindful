-- Remove legacy JSON columns from product_hypothesis table after successful migration
-- WARNING: Only run this migration after confirming:
-- 1. Data migration service has been executed successfully
-- 2. All JSON data has been properly migrated to normalized tables  
-- 3. Frontend is updated to use new API endpoints (if needed)
-- 4. Full system testing is complete

-- Step 1: Verify migration is complete (this will fail if any records are not migrated)
-- This serves as a safety check - if this query returns any rows, migration is not complete
SELECT 'Migration check: Found unmigrated records' as warning, COUNT(*) as unmigrated_count
FROM product_hypothesis 
WHERE json_migrated = 0 OR json_migrated IS NULL
HAVING COUNT(*) > 0;

-- Step 2: Remove the legacy JSON columns
ALTER TABLE product_hypothesis DROP COLUMN assumptions;
ALTER TABLE product_hypothesis DROP COLUMN initiatives; 
ALTER TABLE product_hypothesis DROP COLUMN themes;

-- Step 3: Remove the migration tracking column (no longer needed)
ALTER TABLE product_hypothesis DROP COLUMN json_migrated;