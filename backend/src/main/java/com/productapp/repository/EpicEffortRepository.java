package com.productapp.repository;

import com.productapp.entity.EpicEffort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpicEffortRepository extends JpaRepository<EpicEffort, Long> {
    
    @Query("SELECT ee FROM EpicEffort ee WHERE ee.capacityPlanId = :capacityPlanId ORDER BY ee.epicName, ee.teamId")
    List<EpicEffort> findByCapacityPlanIdOrderByEpicNameTeamId(@Param("capacityPlanId") Long capacityPlanId);
    
    @Query("SELECT ee FROM EpicEffort ee WHERE ee.capacityPlanId = :capacityPlanId AND ee.epicId = :epicId ORDER BY ee.teamId")
    List<EpicEffort> findByCapacityPlanIdAndEpicIdOrderByTeamId(@Param("capacityPlanId") Long capacityPlanId, @Param("epicId") String epicId);
    
    @Query("SELECT ee FROM EpicEffort ee WHERE ee.capacityPlanId = :capacityPlanId AND ee.teamId = :teamId ORDER BY ee.epicName")
    List<EpicEffort> findByCapacityPlanIdAndTeamIdOrderByEpicName(@Param("capacityPlanId") Long capacityPlanId, @Param("teamId") Long teamId);
    
    @Query("SELECT ee FROM EpicEffort ee WHERE ee.capacityPlanId = :capacityPlanId AND ee.epicId = :epicId AND ee.teamId = :teamId")
    Optional<EpicEffort> findByCapacityPlanIdAndEpicIdAndTeamId(@Param("capacityPlanId") Long capacityPlanId, @Param("epicId") String epicId, @Param("teamId") Long teamId);
    
    @Query("DELETE FROM EpicEffort ee WHERE ee.capacityPlanId = :capacityPlanId AND ee.epicId = :epicId")
    void deleteByCapacityPlanIdAndEpicId(@Param("capacityPlanId") Long capacityPlanId, @Param("epicId") String epicId);
}