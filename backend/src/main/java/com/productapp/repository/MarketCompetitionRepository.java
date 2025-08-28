package com.productapp.repository;

import com.productapp.entity.MarketCompetition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketCompetitionRepository extends JpaRepository<MarketCompetition, Long> {
    Optional<MarketCompetition> findByProductId(Long productId);
}