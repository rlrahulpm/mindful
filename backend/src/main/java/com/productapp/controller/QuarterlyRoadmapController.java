package com.productapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.productapp.dto.QuarterlyRoadmapRequest;
import com.productapp.dto.QuarterlyRoadmapResponse;
import com.productapp.entity.QuarterlyRoadmap;
import com.productapp.repository.QuarterlyRoadmapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/{productId}/roadmap")
@CrossOrigin(origins = "http://localhost:3000")
public class QuarterlyRoadmapController {

    @Autowired
    private QuarterlyRoadmapRepository quarterlyRoadmapRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<QuarterlyRoadmapResponse>> getAllRoadmaps(@PathVariable Long productId) {
        try {
            List<QuarterlyRoadmap> roadmaps = quarterlyRoadmapRepository.findByProductId(productId);
            List<QuarterlyRoadmapResponse> responses = new ArrayList<>();
            
            for (QuarterlyRoadmap roadmap : roadmaps) {
                QuarterlyRoadmapResponse response = convertToResponse(roadmap);
                responses.add(response);
            }
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @GetMapping("/{year}/{quarter}")
    public ResponseEntity<QuarterlyRoadmapResponse> getRoadmap(
            @PathVariable Long productId,
            @PathVariable Integer year,
            @PathVariable Integer quarter) {
        try {
            Optional<QuarterlyRoadmap> roadmapOpt = quarterlyRoadmapRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            
            if (roadmapOpt.isPresent()) {
                QuarterlyRoadmapResponse response = convertToResponse(roadmapOpt.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdateRoadmap(
            @PathVariable Long productId,
            @RequestBody QuarterlyRoadmapRequest request) {
        try {
            request.setProductId(productId);
            
            // Check for epic conflicts with other quarters
            System.out.println("Checking epic conflicts for product: " + productId + ", year: " + request.getYear() + ", quarter: " + request.getQuarter());
            System.out.println("Number of roadmap items to check: " + (request.getRoadmapItems() != null ? request.getRoadmapItems().size() : 0));
            List<String> conflictingEpics = checkEpicConflicts(productId, request.getYear(), request.getQuarter(), request.getRoadmapItems());
            System.out.println("Found conflicting epics: " + conflictingEpics);
            if (!conflictingEpics.isEmpty()) {
                System.out.println("Returning conflict response with epics: " + conflictingEpics);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("The following epics are already assigned to other quarters: " + String.join(", ", conflictingEpics));
            }
            
            // Check if roadmap already exists for this product, year, and quarter
            Optional<QuarterlyRoadmap> existingRoadmapOpt = quarterlyRoadmapRepository
                    .findByProductIdAndYearAndQuarter(productId, request.getYear(), request.getQuarter());
            
            QuarterlyRoadmap roadmap;
            if (existingRoadmapOpt.isPresent()) {
                // Update existing roadmap
                roadmap = existingRoadmapOpt.get();
            } else {
                // Create new roadmap
                roadmap = new QuarterlyRoadmap();
                roadmap.setProductId(productId);
                roadmap.setYear(request.getYear());
                roadmap.setQuarter(request.getQuarter());
            }
            
            // Convert roadmap items to JSON string
            String roadmapItemsJson = objectMapper.writeValueAsString(request.getRoadmapItems());
            roadmap.setRoadmapItems(roadmapItemsJson);
            
            QuarterlyRoadmap savedRoadmap = quarterlyRoadmapRepository.save(roadmap);
            QuarterlyRoadmapResponse response = convertToResponse(savedRoadmap);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{year}/{quarter}")
    public ResponseEntity<Void> deleteRoadmap(
            @PathVariable Long productId,
            @PathVariable Integer year,
            @PathVariable Integer quarter) {
        try {
            Optional<QuarterlyRoadmap> roadmapOpt = quarterlyRoadmapRepository.findByProductIdAndYearAndQuarter(productId, year, quarter);
            
            if (roadmapOpt.isPresent()) {
                quarterlyRoadmapRepository.delete(roadmapOpt.get());
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAvailableYears(@PathVariable Long productId) {
        try {
            List<Integer> years = quarterlyRoadmapRepository.findDistinctYearsByProductId(productId);
            return ResponseEntity.ok(years);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }
    
    @GetMapping("/years/{year}/quarters")
    public ResponseEntity<List<Integer>> getAvailableQuarters(@PathVariable Long productId, @PathVariable Integer year) {
        try {
            List<Integer> quarters = quarterlyRoadmapRepository.findDistinctQuartersByProductIdAndYear(productId, year);
            return ResponseEntity.ok(quarters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }
    
    @GetMapping("/assigned-epics")
    public ResponseEntity<Set<String>> getAssignedEpicIds(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer excludeYear,
            @RequestParam(required = false) Integer excludeQuarter) {
        try {
            Set<String> assignedEpicIds = new HashSet<>();
            List<QuarterlyRoadmap> roadmaps;
            
            if (excludeYear != null && excludeQuarter != null) {
                // Get all roadmaps except the specified quarter
                roadmaps = quarterlyRoadmapRepository.findByProductIdExcludingQuarter(productId, excludeYear, excludeQuarter);
            } else {
                // Get all roadmaps for the product
                roadmaps = quarterlyRoadmapRepository.findByProductId(productId);
            }
            
            // Extract epic IDs from all roadmaps
            for (QuarterlyRoadmap roadmap : roadmaps) {
                if (roadmap.getRoadmapItems() != null && !roadmap.getRoadmapItems().isEmpty()) {
                    try {
                        List<QuarterlyRoadmapRequest.RoadmapItem> items = objectMapper.readValue(
                            roadmap.getRoadmapItems(),
                            new TypeReference<List<QuarterlyRoadmapRequest.RoadmapItem>>() {}
                        );
                        for (QuarterlyRoadmapRequest.RoadmapItem item : items) {
                            assignedEpicIds.add(item.getEpicId());
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing roadmap items for roadmap ID: " + roadmap.getId());
                    }
                }
            }
            
            return ResponseEntity.ok(assignedEpicIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashSet<>());
        }
    }

    private QuarterlyRoadmapResponse convertToResponse(QuarterlyRoadmap roadmap) throws JsonProcessingException {
        QuarterlyRoadmapResponse response = new QuarterlyRoadmapResponse();
        response.setId(roadmap.getId());
        response.setProductId(roadmap.getProductId());
        response.setYear(roadmap.getYear());
        response.setQuarter(roadmap.getQuarter());
        response.setCreatedAt(roadmap.getCreatedAt());
        response.setUpdatedAt(roadmap.getUpdatedAt());
        
        // Convert JSON string back to list of roadmap items
        if (roadmap.getRoadmapItems() != null && !roadmap.getRoadmapItems().isEmpty()) {
            List<QuarterlyRoadmapRequest.RoadmapItem> roadmapItems = objectMapper.readValue(
                    roadmap.getRoadmapItems(),
                    new TypeReference<List<QuarterlyRoadmapRequest.RoadmapItem>>() {}
            );
            response.setRoadmapItems(roadmapItems);
        } else {
            response.setRoadmapItems(new ArrayList<>());
        }
        
        return response;
    }
    
    private List<String> checkEpicConflicts(Long productId, Integer year, Integer quarter, List<QuarterlyRoadmapRequest.RoadmapItem> newItems) {
        List<String> conflictingEpics = new ArrayList<>();
        
        if (newItems == null || newItems.isEmpty()) {
            System.out.println("No new items to check for conflicts");
            return conflictingEpics;
        }
        
        // Get all existing roadmaps except the current one
        List<QuarterlyRoadmap> otherRoadmaps = quarterlyRoadmapRepository.findByProductIdExcludingQuarter(productId, year, quarter);
        System.out.println("Found " + otherRoadmaps.size() + " other roadmaps to check against");
        
        // Extract epic IDs from new items
        Set<String> newEpicIds = newItems.stream()
            .map(item -> item.getEpicId())
            .collect(Collectors.toSet());
        System.out.println("New epic IDs to check: " + newEpicIds);
        
        // Check each existing roadmap for conflicts
        for (QuarterlyRoadmap roadmap : otherRoadmaps) {
            try {
                System.out.println("Checking roadmap for Q" + roadmap.getQuarter() + " " + roadmap.getYear());
                if (roadmap.getRoadmapItems() != null && !roadmap.getRoadmapItems().isEmpty()) {
                    List<QuarterlyRoadmapRequest.RoadmapItem> existingItems = objectMapper.readValue(
                        roadmap.getRoadmapItems(),
                        new TypeReference<List<QuarterlyRoadmapRequest.RoadmapItem>>() {}
                    );
                    System.out.println("Found " + existingItems.size() + " existing items in this roadmap");
                    
                    for (QuarterlyRoadmapRequest.RoadmapItem existingItem : existingItems) {
                        System.out.println("Checking existing epic: " + existingItem.getEpicId() + " (" + existingItem.getEpicName() + ")");
                        if (newEpicIds.contains(existingItem.getEpicId())) {
                            System.out.println("CONFLICT FOUND! Epic " + existingItem.getEpicId() + " is already in Q" + roadmap.getQuarter() + " " + roadmap.getYear());
                            conflictingEpics.add(existingItem.getEpicName() + " (Q" + roadmap.getQuarter() + " " + roadmap.getYear() + ")");
                        }
                    }
                }
            } catch (Exception e) {
                // Log error but continue checking other roadmaps
                System.err.println("Error parsing roadmap items for roadmap ID: " + roadmap.getId());
            }
        }
        
        return conflictingEpics;
    }
}