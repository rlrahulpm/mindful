-- Migrate existing JSON data from product_hypothesis table to normalized tables
-- This script will parse JSON data and insert into themes, initiatives, and assumptions tables

-- Note: This migration assumes the JSON structure follows the format used in the frontend
-- If the JSON structure is different, this script may need to be adjusted

-- For themes: Expected JSON format: [{"name": "Theme Name", "description": "Description", "color": "#color"}]
-- For initiatives: Expected JSON format: [{"title": "Initiative Title", "description": "Description", "priority": "High", "timeline": "Q1 2024", "owner": "Owner Name"}]
-- For assumptions: Expected JSON format: [{"assumption": "Assumption text", "confidence": "High", "impact": "Medium"}]

-- Since MySQL doesn't have great JSON parsing capabilities in older versions,
-- we'll create a procedure to handle this migration
-- This is a basic approach that will need to be customized based on actual data structure

-- First, let's create a simple migration that handles basic cases
-- More complex JSON parsing would require application-level migration

-- Add a flag to track migration status
ALTER TABLE product_hypothesis ADD COLUMN json_migrated BOOLEAN DEFAULT FALSE;

-- The actual JSON parsing and migration will be handled by a separate Java service
-- This is because SQL-based JSON parsing is complex and error-prone
-- We'll create the structure here and handle the data migration separately