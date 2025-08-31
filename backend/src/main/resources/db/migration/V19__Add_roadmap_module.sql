-- Insert Roadmap module
INSERT INTO modules (name, description, created_at, updated_at) VALUES 
('Roadmap', 'Visualize roadmap items across quarters in timeline and Gantt chart views', NOW(), NOW());

-- Get the module ID
SET @roadmap_module_id = LAST_INSERT_ID();

-- Add module to all existing products
INSERT INTO product_modules (product_id, module_id, is_enabled, created_at, updated_at)
SELECT p.id, @roadmap_module_id, true, NOW(), NOW()
FROM products p;

-- Add module permissions to all existing roles
INSERT INTO role_product_modules (role_id, product_module_id)
SELECT rpm.role_id, pm.id
FROM role_product_modules rpm
JOIN product_modules pm ON rpm.product_module_id = pm.id
JOIN product_modules pm2 ON pm.product_id = pm2.product_id
WHERE pm2.module_id = @roadmap_module_id
GROUP BY rpm.role_id, pm.id;