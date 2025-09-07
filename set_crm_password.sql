-- Update password for admin@mindful.com to 'Admin@123'
-- The BCrypt hash below is for 'Admin@123'
UPDATE users 
SET password = '$2a$10$DowJone1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKL'
WHERE email = 'admin@mindful.com';