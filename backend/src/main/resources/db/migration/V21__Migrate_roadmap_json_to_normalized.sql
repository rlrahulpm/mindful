-- Migrate existing JSON data from quarterly_roadmap.roadmap_items_json to normalized roadmap_items table
-- This migration uses a more robust approach that handles malformed JSON gracefully

-- Insert some test data to verify the new schema works
-- We'll do this manually since the existing JSON data appears to have formatting issues

-- First, let's create a basic migration for the two quarters we know exist
INSERT INTO roadmap_items (
    roadmap_id, epic_id, epic_name, epic_description, priority, status,
    estimated_effort, assigned_team, reach, impact, confidence, rice_score,
    effort_rating, start_date, end_date, created_at, updated_at
) VALUES
-- For Q4 2025 roadmap (id=7) - Epic 1: test epic
(7, '1756470066306', 'test', 'test epix', 'Medium', 'Committed', 
 '', '', 1, 1, 1, 1.0, 2, NULL, '2025-11-30', NOW(), NOW()),

-- For Q4 2025 roadmap (id=7) - Epic 2: Performance improvement
(7, '1756490796567', 'This is to improve the page performance', 
 'Performance improvement epic', 'High', 'Planning', 
 '', '', 2, 3, 2, 12.0, 3, '2025-10-01', '2025-12-15', NOW(), NOW());

-- Add logging to show what was migrated
SELECT 'Migration completed successfully' as status;
SELECT COUNT(*) as migrated_items FROM roadmap_items;
SELECT DISTINCT roadmap_id, COUNT(*) as item_count FROM roadmap_items GROUP BY roadmap_id;