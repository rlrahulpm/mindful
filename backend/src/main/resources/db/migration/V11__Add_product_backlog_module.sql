-- Insert Product Backlog module
INSERT INTO modules (name, description, icon, is_active, display_order, created_at, updated_at) 
VALUES ('Product Backlog', 'Manage product epics with themes, initiatives, and tracks', 'list_alt', 1, 4, NOW(), NOW());

-- Get the module ID of the newly inserted module
SET @backlog_module_id = LAST_INSERT_ID();

-- Add Product Backlog module to all existing products
INSERT INTO product_modules (product_id, module_id, is_enabled, created_at, updated_at)
SELECT p.id, @backlog_module_id, 1, NOW(), NOW()
FROM products p;