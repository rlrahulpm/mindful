-- Create roadmap_items table with proper normalized structure
CREATE TABLE roadmap_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    epic_id VARCHAR(255) NOT NULL,
    epic_name VARCHAR(255) NOT NULL,
    epic_description TEXT,
    priority VARCHAR(50),
    status VARCHAR(50),
    estimated_effort VARCHAR(100),
    assigned_team VARCHAR(255),
    reach INT,
    impact INT,
    confidence INT,
    rice_score DOUBLE,
    effort_rating INT,
    start_date DATE,
    end_date DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (roadmap_id) REFERENCES quarterly_roadmap(id) ON DELETE CASCADE,
    INDEX idx_roadmap_id (roadmap_id),
    INDEX idx_epic_id (epic_id),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date)
);

-- Rename existing column to avoid conflicts
ALTER TABLE quarterly_roadmap 
RENAME COLUMN roadmap_items TO roadmap_items_json;

-- Migrate existing JSON data to the new normalized structure
-- This will be done in the application code to handle JSON parsing