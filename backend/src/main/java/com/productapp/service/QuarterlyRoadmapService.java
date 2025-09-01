package com.productapp.service;

import com.productapp.dto.QuarterlyRoadmapRequest;
import com.productapp.entity.QuarterlyRoadmap;
import com.productapp.entity.RoadmapItem;
import com.productapp.repository.QuarterlyRoadmapRepository;
import com.productapp.repository.RoadmapItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class QuarterlyRoadmapService {

    private static final Logger logger = LoggerFactory.getLogger(QuarterlyRoadmapService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private QuarterlyRoadmapRepository quarterlyRoadmapRepository;
    
    @Autowired
    private RoadmapItemRepository roadmapItemRepository;

    @Transactional
    public QuarterlyRoadmap createOrUpdateRoadmap(Long productId, QuarterlyRoadmapRequest request) {

        // Find or create roadmap
        Optional<QuarterlyRoadmap> existingRoadmapOpt = quarterlyRoadmapRepository
                .findByProductIdAndYearAndQuarter(productId, request.getYear(), request.getQuarter());
        
        QuarterlyRoadmap roadmap;
        if (existingRoadmapOpt.isPresent()) {
            roadmap = existingRoadmapOpt.get();
            // Delete existing items first
            roadmapItemRepository.deleteByRoadmapId(roadmap.getId());
        } else {
            roadmap = new QuarterlyRoadmap();
            roadmap.setProductId(productId);
            roadmap.setYear(request.getYear());
            roadmap.setQuarter(request.getQuarter());
        }
        
        // Save roadmap first to ensure we have an ID
        roadmap = quarterlyRoadmapRepository.save(roadmap);
        
        // Convert and save new items
        if (request.getRoadmapItems() != null && !request.getRoadmapItems().isEmpty()) {
            for (QuarterlyRoadmapRequest.RoadmapItem requestItem : request.getRoadmapItems()) {
                RoadmapItem item = new RoadmapItem();
                item.setRoadmap(roadmap);
                item.setEpicId(requestItem.getEpicId());
                item.setEpicName(requestItem.getEpicName());
                item.setEpicDescription(requestItem.getEpicDescription());
                item.setPriority(requestItem.getPriority());
                item.setStatus(requestItem.getStatus());
                item.setEstimatedEffort(requestItem.getEstimatedEffort());
                item.setAssignedTeam(requestItem.getAssignedTeam());
                item.setReach(requestItem.getReach());
                item.setImpact(requestItem.getImpact());
                item.setConfidence(requestItem.getConfidence());
                item.setRiceScore(requestItem.getRiceScore());
                item.setEffortRating(requestItem.getEffortRating());
                
                // Set initiative and theme information if provided
                item.setInitiativeName(requestItem.getInitiativeName());
                item.setThemeName(requestItem.getThemeName());
                item.setThemeColor(requestItem.getThemeColor());
                
                // Parse dates if provided
                if (requestItem.getStartDate() != null && !requestItem.getStartDate().isEmpty()) {
                    item.setStartDate(LocalDate.parse(requestItem.getStartDate(), DATE_FORMATTER));
                }
                if (requestItem.getEndDate() != null && !requestItem.getEndDate().isEmpty()) {
                    item.setEndDate(LocalDate.parse(requestItem.getEndDate(), DATE_FORMATTER));
                }
                
                // Save each item individually to ensure proper persistence
                RoadmapItem savedItem = roadmapItemRepository.save(item);
            }
        }
        
        // Force flush to ensure data is written to database within this transaction
        quarterlyRoadmapRepository.flush();
        roadmapItemRepository.flush();
        
        return roadmap;
    }
}