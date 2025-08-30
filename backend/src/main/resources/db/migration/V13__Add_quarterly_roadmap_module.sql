-- Insert Quarterly Roadmap module
INSERT INTO modules (name, description, created_at, updated_at) VALUES 
('Quarterly Roadmap', 'Plan and manage quarterly product roadmaps with backlog items', NOW(), NOW());

-- Get the module ID
SET @quarterly_roadmap_module_id = LAST_INSERT_ID();

-- Add module to all existing products
INSERT INTO product_modules (product_id, module_id, is_enabled, created_at, updated_at)
SELECT p.id, @quarterly_roadmap_module_id, true, NOW(), NOW()
FROM products p;

-- Add module permissions to all existing roles
INSERT INTO role_product_modules (role_id, product_module_id)
SELECT rpm.role_id, pm.id
FROM role_product_modules rpm
JOIN product_modules pm ON rpm.product_module_id = pm.id
WHERE pm.module_id = @quarterly_roadmap_module_id
GROUP BY rpm.role_id, pm.id;