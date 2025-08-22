-- Create organizations table
CREATE TABLE organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

-- Insert default organization for existing data
INSERT INTO organizations (name, description, created_at, updated_at) 
VALUES ('Default Organization', 'Default organization for existing users and products', NOW(6), NOW(6));

-- Add organization columns to users table
ALTER TABLE users 
ADD COLUMN organization_id BIGINT,
ADD COLUMN is_global_superadmin BOOLEAN DEFAULT FALSE NOT NULL;

-- Add foreign key constraint for users.organization_id
ALTER TABLE users 
ADD CONSTRAINT FK_users_organization_id 
FOREIGN KEY (organization_id) REFERENCES organizations(id);

-- Update existing users to belong to the default organization
UPDATE users SET organization_id = 1;

-- Set rlrahul2030@gmail.com as global superadmin
UPDATE users 
SET is_global_superadmin = TRUE, is_superadmin = TRUE 
WHERE email = 'rlrahul2030@gmail.com';

-- Make organization_id NOT NULL after setting default values
ALTER TABLE users MODIFY COLUMN organization_id BIGINT NOT NULL;

-- Add organization column to products table
ALTER TABLE products 
ADD COLUMN organization_id BIGINT;

-- Add foreign key constraint for products.organization_id
ALTER TABLE products 
ADD CONSTRAINT FK_products_organization_id 
FOREIGN KEY (organization_id) REFERENCES organizations(id);

-- Update existing products to belong to the same organization as their owner
UPDATE products p 
JOIN users u ON p.user_id = u.id 
SET p.organization_id = u.organization_id;

-- Make organization_id NOT NULL after setting default values
ALTER TABLE products MODIFY COLUMN organization_id BIGINT NOT NULL;