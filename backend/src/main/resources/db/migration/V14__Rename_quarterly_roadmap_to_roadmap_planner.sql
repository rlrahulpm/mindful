-- Update Quarterly Roadmap module name to Roadmap Planner
UPDATE modules 
SET name = 'Roadmap Planner', 
    description = 'Plan and manage product roadmaps with backlog items',
    updated_at = NOW()
WHERE name = 'Quarterly Roadmap';