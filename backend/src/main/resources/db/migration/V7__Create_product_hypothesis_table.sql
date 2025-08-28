CREATE TABLE IF NOT EXISTS product_hypothesis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    hypothesis_statement TEXT,
    problem_statement TEXT,
    solution_approach TEXT,
    success_metrics TEXT,
    assumptions TEXT,
    risks TEXT,
    initiatives TEXT,
    themes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_product_hypothesis_product_id ON product_hypothesis(product_id);