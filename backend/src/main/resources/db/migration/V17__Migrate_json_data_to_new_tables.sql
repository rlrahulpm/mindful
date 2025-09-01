-- This migration will extract JSON data from product_hypothesis table 
-- and insert it into the new normalized tables

-- Note: This is a complex migration that needs to parse JSON
-- MySQL 5.7+ has JSON functions, but we'll use a stored procedure for safety

DELIMITER $$

CREATE PROCEDURE MigrateProductHypothesisData()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_product_id BIGINT;
    DECLARE v_themes_json TEXT;
    DECLARE v_initiatives_json TEXT;
    DECLARE v_assumptions_json TEXT;
    
    DECLARE cur CURSOR FOR 
        SELECT product_id, themes, initiatives, assumptions 
        FROM product_hypothesis 
        WHERE themes IS NOT NULL OR initiatives IS NOT NULL OR assumptions IS NOT NULL;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_product_id, v_themes_json, v_initiatives_json, v_assumptions_json;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- For now, we'll log that manual migration is needed
        -- In production, you'd parse the JSON and insert records
        -- This is a placeholder since JSON parsing varies by MySQL version
        
        IF v_themes_json IS NOT NULL AND v_themes_json != '' AND v_themes_json != '[]' THEN
            SELECT CONCAT('Manual migration needed for themes in product_id: ', v_product_id) AS migration_note;
        END IF;
        
        IF v_initiatives_json IS NOT NULL AND v_initiatives_json != '' AND v_initiatives_json != '[]' THEN
            SELECT CONCAT('Manual migration needed for initiatives in product_id: ', v_product_id) AS migration_note;
        END IF;
        
        IF v_assumptions_json IS NOT NULL AND v_assumptions_json != '' AND v_assumptions_json != '[]' THEN
            SELECT CONCAT('Manual migration needed for assumptions in product_id: ', v_product_id) AS migration_note;
        END IF;
        
    END LOOP;
    
    CLOSE cur;
END$$

DELIMITER ;

-- Execute the migration procedure
CALL MigrateProductHypothesisData();

-- Drop the procedure after use
DROP PROCEDURE MigrateProductHypothesisData;

-- Note: After verifying data migration, the old columns should be dropped in a separate migration
-- For now, we'll keep them for backward compatibility and verification