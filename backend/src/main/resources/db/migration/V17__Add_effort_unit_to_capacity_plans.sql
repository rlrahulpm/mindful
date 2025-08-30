-- V17__Add_effort_unit_to_capacity_plans.sql
-- Add effort_unit column to capacity_plans table

ALTER TABLE capacity_plans 
ADD COLUMN effort_unit VARCHAR(10) NOT NULL DEFAULT 'SPRINTS';