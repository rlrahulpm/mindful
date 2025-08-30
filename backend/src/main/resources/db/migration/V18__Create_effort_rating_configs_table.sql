CREATE TABLE effort_rating_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    unit_type VARCHAR(255) NOT NULL,
    star_1_max INT NOT NULL DEFAULT 2,
    star_2_min INT NOT NULL DEFAULT 3,
    star_2_max INT NOT NULL DEFAULT 4,
    star_3_min INT NOT NULL DEFAULT 5,
    star_3_max INT NOT NULL DEFAULT 6,
    star_4_min INT NOT NULL DEFAULT 7,
    star_4_max INT NOT NULL DEFAULT 8,
    star_5_min INT NOT NULL DEFAULT 9,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_effort_rating_configs_product 
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_effort_rating_configs_product_unit 
        UNIQUE KEY (product_id, unit_type)
);