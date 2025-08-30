-- Create teams table
CREATE TABLE teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    product_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_teams_product_id (product_id),
    UNIQUE KEY uk_teams_product_name (product_id, name)
);

-- Create capacity_plans table
CREATE TABLE capacity_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    year INT NOT NULL,
    quarter INT NOT NULL CHECK (quarter BETWEEN 1 AND 4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_capacity_plans_product (product_id),
    INDEX idx_capacity_plans_year_quarter (year, quarter),
    UNIQUE KEY uk_capacity_plans_product_year_quarter (product_id, year, quarter)
);

-- Create epic_efforts table
CREATE TABLE epic_efforts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    capacity_plan_id BIGINT NOT NULL,
    epic_id VARCHAR(255) NOT NULL,
    epic_name VARCHAR(500) NOT NULL,
    team_id BIGINT NOT NULL,
    effort_days INT NOT NULL DEFAULT 0 CHECK (effort_days >= 0),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (capacity_plan_id) REFERENCES capacity_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    INDEX idx_epic_efforts_capacity_plan (capacity_plan_id),
    INDEX idx_epic_efforts_epic (epic_id),
    INDEX idx_epic_efforts_team (team_id),
    UNIQUE KEY uk_epic_efforts_plan_epic_team (capacity_plan_id, epic_id, team_id)
);

-- Insert default teams for existing products
INSERT INTO teams (name, description, product_id, created_at, updated_at)
SELECT 'Frontend', 'Frontend development team', p.id, NOW(), NOW()
FROM products p;

INSERT INTO teams (name, description, product_id, created_at, updated_at)
SELECT 'Backend', 'Backend development team', p.id, NOW(), NOW()
FROM products p;

INSERT INTO teams (name, description, product_id, created_at, updated_at)
SELECT 'Data Engineering', 'Data engineering and analytics team', p.id, NOW(), NOW()
FROM products p;

INSERT INTO teams (name, description, product_id, created_at, updated_at)
SELECT 'QA', 'Quality assurance and testing team', p.id, NOW(), NOW()
FROM products p;