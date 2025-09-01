-- Create themes table
CREATE TABLE themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(7) NOT NULL, -- Hex color code
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_theme_name_per_product (product_id, name)
);

-- Create initiatives table
CREATE TABLE initiatives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL, -- Low, Medium, High, Critical
    timeline VARCHAR(255),
    owner VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_initiative_title_per_product (product_id, title)
);

-- Create assumptions table (also needs normalization)
CREATE TABLE assumptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    assumption TEXT NOT NULL,
    confidence VARCHAR(50) NOT NULL, -- Low, Medium, High
    impact VARCHAR(50) NOT NULL, -- Low, Medium, High
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Add indexes for better query performance
CREATE INDEX idx_themes_product_id ON themes(product_id);
CREATE INDEX idx_initiatives_product_id ON initiatives(product_id);
CREATE INDEX idx_assumptions_product_id ON assumptions(product_id);