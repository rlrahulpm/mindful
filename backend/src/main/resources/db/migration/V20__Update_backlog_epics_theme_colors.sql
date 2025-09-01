-- Update backlog_epics theme colors to match current theme colors from themes table
-- This fixes cases where epics were created before theme colors were updated

-- First, let's update theme colors where theme_id matches
UPDATE backlog_epics be
SET theme_color = t.color
FROM themes t 
WHERE be.theme_id = CAST(t.id AS VARCHAR(255))
AND be.theme_color != t.color;

-- Also update theme_name in case it was changed
UPDATE backlog_epics be  
SET theme_name = t.name
FROM themes t
WHERE be.theme_id = CAST(t.id AS VARCHAR(255))
AND be.theme_name != t.name;