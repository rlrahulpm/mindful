-- Insert Kanban Board module if it doesn't exist
INSERT INTO modules (name, description, icon, is_active, display_order, created_at, updated_at)
SELECT 'Kanban Board', 'Track and manage work items from Committed to Done', 'view_kanban', true, 8, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM modules WHERE name = 'Kanban Board'
);

-- Get the module ID
SET @kanban_module_id = (SELECT id FROM modules WHERE name = 'Kanban Board');

-- Insert ProductModule entries for all products if they don't exist
INSERT INTO product_modules (product_id, module_id, is_enabled, created_at, updated_at)
SELECT p.product_id, @kanban_module_id, true, NOW(), NOW()
FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM product_modules 
    WHERE product_id = p.product_id AND module_id = @kanban_module_id
);