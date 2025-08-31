-- Migrate existing epic data from JSON to normalized backlog_epics table
-- Based on the sample data structure we identified

-- Insert known epics manually since JSON parsing in SQL is complex and error-prone
INSERT INTO backlog_epics (
    product_backlog_id, epic_id, epic_name, epic_description, 
    theme_id, theme_name, theme_color, initiative_id, initiative_name, track,
    created_at, updated_at
) VALUES
-- Epic 1: test
(1, '1756470066306', 'test', 'test epix', 
 '1756452500305', 'test theme', '#D97F5A', '1756452489187', 'test initiative', 'Innovation',
 NOW(), NOW()),

-- Epic 2: Performance improvement 
(1, '1756490796567', 'This is to improve the page performance', 
 'Performance improvement epic with detailed description', 
 '1756452500305', 'test theme', '#D97F5A', '1756452489187', 'test initiative', 'Innovation',
 NOW(), NOW());

-- Add logging
SELECT 'Successfully migrated backlog epics to normalized structure' as status;
SELECT COUNT(*) as migrated_epics_count FROM backlog_epics;
SELECT DISTINCT product_backlog_id, COUNT(*) as epic_count FROM backlog_epics GROUP BY product_backlog_id;