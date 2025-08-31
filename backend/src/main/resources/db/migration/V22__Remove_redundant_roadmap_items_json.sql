-- Remove redundant roadmap_items_json column since data is now properly normalized
-- in the roadmap_items table with individual columns for each field

-- Drop the redundant JSON column
ALTER TABLE quarterly_roadmap DROP COLUMN roadmap_items_json;

-- Add logging
SELECT 'Successfully removed redundant roadmap_items_json column' as status;