-- Sync roadmap_items theme colors with backlog_epics
-- This ensures roadmap visualization shows consistent theme colors

-- Update roadmap_items to use the correct theme colors from backlog_epics
UPDATE roadmap_items ri
JOIN backlog_epics be ON ri.epic_id = be.epic_id
SET ri.theme_color = be.theme_color,
    ri.theme_name = be.theme_name
WHERE be.theme_color IS NOT NULL AND be.theme_name IS NOT NULL;

-- Also update any items that have NULL theme values
UPDATE roadmap_items ri
JOIN backlog_epics be ON ri.epic_id = be.epic_id
SET ri.theme_color = be.theme_color,
    ri.theme_name = be.theme_name
WHERE ri.theme_color IS NULL OR ri.theme_name IS NULL;