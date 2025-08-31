-- Create normalized backlog_epics table 
-- Each epic gets its own row instead of being dumped in JSON blob

CREATE TABLE backlog_epics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_backlog_id BIGINT NOT NULL,
    epic_id VARCHAR(255) NOT NULL,
    epic_name VARCHAR(255) NOT NULL,
    epic_description TEXT,
    theme_id VARCHAR(255),
    theme_name VARCHAR(255),
    theme_color VARCHAR(7),
    initiative_id VARCHAR(255),
    initiative_name VARCHAR(255),
    track VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_backlog_id) REFERENCES product_backlog(id) ON DELETE CASCADE,
    INDEX idx_product_backlog_id (product_backlog_id),
    INDEX idx_epic_id (epic_id),
    INDEX idx_theme_id (theme_id),
    INDEX idx_initiative_id (initiative_id),
    INDEX idx_track (track)
);

-- Rename existing column to avoid conflicts during migration
ALTER TABLE product_backlog 
RENAME COLUMN epics TO epics_json;

-- Add logging
SELECT 'Created normalized backlog_epics table and renamed epics column' as status;