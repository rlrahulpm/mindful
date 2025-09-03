-- Create kanban_items table
CREATE TABLE IF NOT EXISTS kanban_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    position INT,
    priority VARCHAR(20),
    assignee VARCHAR(255),
    due_date TIMESTAMP,
    labels VARCHAR(500),
    epic_id VARCHAR(255),
    story_points INT,
    product_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);