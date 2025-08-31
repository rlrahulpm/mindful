-- Remove unnecessary product_backlog table and link epics directly to products
-- This simplifies the schema from: products -> product_backlog -> backlog_epics
-- To the cleaner: products -> backlog_epics

-- Step 1: Add product_id column to backlog_epics table
ALTER TABLE backlog_epics ADD COLUMN product_id BIGINT;

-- Step 2: Populate product_id from the existing relationship via product_backlog
UPDATE backlog_epics be 
JOIN product_backlog pb ON be.product_backlog_id = pb.id 
SET be.product_id = pb.product_id;

-- Step 3: Add foreign key constraint for product_id
ALTER TABLE backlog_epics 
ADD CONSTRAINT fk_backlog_epics_product 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- Step 4: Add index on product_id for better performance
ALTER TABLE backlog_epics ADD INDEX idx_backlog_epics_product_id (product_id);

-- Step 5: Drop the old foreign key constraint to product_backlog
ALTER TABLE backlog_epics DROP FOREIGN KEY fk_backlog_epics_product_backlog;

-- Step 6: Drop the product_backlog_id column (no longer needed)
ALTER TABLE backlog_epics DROP COLUMN product_backlog_id;

-- Step 7: Drop the product_backlog table entirely
DROP TABLE product_backlog;

-- Add logging
SELECT 'Successfully removed product_backlog table and simplified schema' as status;
SELECT COUNT(*) as remaining_epics_count FROM backlog_epics;
SELECT DISTINCT product_id, COUNT(*) as epic_count FROM backlog_epics GROUP BY product_id;