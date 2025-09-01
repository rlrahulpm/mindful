-- Fix theme_id mismatch in backlog_epics table
-- The backlog_epics table has theme_id values that don't match the actual theme IDs in themes table
-- Update backlog_epics to use the correct theme_id and theme_color values

-- Update theme_id and theme_color for epics that have "test theme" but wrong theme_id
UPDATE backlog_epics 
SET 
    theme_id = '1',
    theme_color = '#7f59d9'
WHERE theme_name = 'test theme' 
AND theme_id != '1';