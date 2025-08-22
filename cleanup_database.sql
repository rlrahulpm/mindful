-- Database Cleanup Script
-- This script removes all test data while keeping:
-- 1. The superadmin user 'rlrahul2030@gmail.com'
-- 2. Core modules (Product Basics, Market & Competition Analysis)

USE productdb;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Step 1: Delete all role_product_modules associations
DELETE FROM role_product_modules;

-- Step 2: Delete all product_modules
DELETE FROM product_modules;

-- Step 3: Delete all products
DELETE FROM products;

-- Step 4: Delete all roles
DELETE FROM roles;

-- Step 5: Delete all users except the superadmin
DELETE FROM users WHERE email != 'rlrahul2030@gmail.com';

-- Step 6: Ensure the superadmin user has the correct settings
UPDATE users 
SET is_superadmin = 1, role_id = NULL 
WHERE email = 'rlrahul2030@gmail.com';

-- Step 7: Keep only the core modules (reset any test modules)
DELETE FROM modules;

-- Re-insert the core modules
INSERT INTO modules (id, name, description, icon, is_active, display_order) VALUES
(1, 'Product Basics', 'Define your product fundamentals, features, and core specifications', '📋', TRUE, 1),
(2, 'Market & Competition Analysis', 'Analyze your market position, competitors, and opportunities', '📊', TRUE, 2);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Show the cleaned state
SELECT 'Users in system:' as Info;
SELECT id, email, is_superadmin, role_id FROM users;

SELECT '\nModules in system:' as Info;
SELECT id, name, is_active FROM modules;

SELECT '\nProducts in system:' as Info;
SELECT COUNT(*) as product_count FROM products;

SELECT '\nRoles in system:' as Info;
SELECT COUNT(*) as role_count FROM roles;

SELECT '\nProduct-Module associations:' as Info;
SELECT COUNT(*) as product_module_count FROM product_modules;