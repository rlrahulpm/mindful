-- Script to create product-module associations
-- This associates all existing modules with all existing products

USE productdb;

-- Insert product-module associations for all combinations
INSERT INTO product_modules (product_id, module_id, is_enabled, completion_percentage)
SELECT 
    p.id as product_id,
    m.id as module_id,
    TRUE as is_enabled,
    0 as completion_percentage
FROM products p
CROSS JOIN modules m
WHERE NOT EXISTS (
    SELECT 1 FROM product_modules pm 
    WHERE pm.product_id = p.id AND pm.module_id = m.id
);

-- Show the created associations
SELECT 
    pm.id,
    p.product_name,
    m.name as module_name,
    pm.is_enabled,
    pm.completion_percentage
FROM product_modules pm
JOIN products p ON pm.product_id = p.id
JOIN modules m ON pm.module_id = m.id
ORDER BY p.product_name, m.display_order;