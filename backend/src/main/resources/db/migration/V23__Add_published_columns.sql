-- Add published columns to roadmap items table
ALTER TABLE roadmap_items 
ADD COLUMN published BOOLEAN DEFAULT false,
ADD COLUMN published_date DATE;

-- Add published columns to quarterly roadmap table
ALTER TABLE quarterly_roadmap 
ADD COLUMN published BOOLEAN DEFAULT false,
ADD COLUMN published_date DATE;