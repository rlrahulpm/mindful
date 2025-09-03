package com.productapp.controller;

import com.productapp.entity.KanbanItem;
import com.productapp.entity.Product;
import com.productapp.entity.RoadmapItem;
import com.productapp.repository.KanbanItemRepository;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.RoadmapItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v3/products/{productId}/kanban")
@CrossOrigin(origins = "*")
public class KanbanController {
    
    @Autowired
    private KanbanItemRepository kanbanItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RoadmapItemRepository roadmapItemRepository;
    
    @GetMapping
    public ResponseEntity<?> getKanbanItems(@PathVariable String productId) {
        try {
            System.out.println("KanbanController.getKanbanItems called for productId: " + productId);
            Optional<Product> productOpt = productRepository.findById(Long.parseLong(productId));
            if (!productOpt.isPresent()) {
                System.out.println("Product not found for ID: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            
            Product product = productOpt.get();
            System.out.println("Found product: " + product.getProductName() + " (ID: " + product.getId() + ")");
            List<KanbanItem> items = kanbanItemRepository.findByProductOrderByStatusAscPositionAsc(product);
            System.out.println("Found " + items.size() + " existing kanban items");
            
            // Filter out tracking items that are in COMMITTED status (to avoid duplication with roadmap items)
            List<KanbanItem> filteredItems = items.stream()
                .filter(item -> {
                    boolean isTrackingItem = item.getEpicId() != null && "roadmap-item".equals(item.getLabels());
                    boolean isInCommitted = "COMMITTED".equals(item.getStatus());
                    // Keep the item if it's not a tracking item OR if it's not in COMMITTED
                    boolean keep = !isTrackingItem || !isInCommitted;
                    if (!keep) {
                        System.out.println("  - Filtering out COMMITTED tracking item: " + item.getTitle() + " (epicId: " + item.getEpicId() + ")");
                    } else {
                        System.out.println("  - Keeping kanban item: " + item.getTitle() + " (epicId: " + item.getEpicId() + ", status: " + item.getStatus() + ")");
                    }
                    return keep;
                })
                .collect(Collectors.toList());
            
            // Get published roadmap items and convert them to KanbanItems
            List<RoadmapItem> publishedRoadmapItems = roadmapItemRepository.findPublishedByProductId(Long.parseLong(productId));
            System.out.println("Found " + publishedRoadmapItems.size() + " published roadmap items for product " + productId);
            
            // Convert roadmap items to kanban items for COMMITTED status
            // Only include roadmap items that haven't been moved to other statuses
            List<KanbanItem> roadmapKanbanItems = publishedRoadmapItems.stream()
                .filter(roadmapItem -> {
                    // Check if this roadmap item has been moved to a different status
                    String epicId = roadmapItem.getEpicId();
                    System.out.println("Checking roadmap item with epicId: " + epicId + " (name: " + roadmapItem.getEpicName() + ")");
                    List<KanbanItem> trackingItems = kanbanItemRepository.findByEpicIdAndProduct(epicId, product);
                    
                    // Only exclude if there's a tracking item that is NOT in COMMITTED status
                    boolean hasNonCommittedTracking = trackingItems.stream()
                        .anyMatch(item -> !"COMMITTED".equals(item.getStatus()));
                    
                    System.out.println("Found " + trackingItems.size() + " tracking items for epicId: " + epicId);
                    if (!trackingItems.isEmpty()) {
                        trackingItems.forEach(item -> 
                            System.out.println("  - Tracking item status: " + item.getStatus()));
                    }
                    boolean shouldInclude = !hasNonCommittedTracking; // Include if no tracking item or only COMMITTED tracking
                    System.out.println("Should include in COMMITTED? " + shouldInclude);
                    return shouldInclude;
                })
                .map(roadmapItem -> {
                    KanbanItem kanbanItem = new KanbanItem();
                    kanbanItem.setId(roadmapItem.getId() * -1); // Negative ID to distinguish from regular kanban items
                    kanbanItem.setTitle(roadmapItem.getEpicName());
                    kanbanItem.setDescription(roadmapItem.getEpicDescription());
                    kanbanItem.setStatus("COMMITTED");
                    kanbanItem.setPosition(0); // Place at the beginning
                    kanbanItem.setPriority(roadmapItem.getPriority());
                    kanbanItem.setAssignee(roadmapItem.getAssignedTeam());
                    kanbanItem.setDueDate(roadmapItem.getEndDate() != null ? roadmapItem.getEndDate().atStartOfDay() : null);
                    kanbanItem.setEpicId(roadmapItem.getEpicId());
                    kanbanItem.setLabels("roadmap-item");
                    kanbanItem.setProduct(product);
                    return kanbanItem;
                })
                .collect(Collectors.toList());
            
            // Combine regular kanban items with roadmap items
            List<KanbanItem> allItems = new java.util.ArrayList<>();
            allItems.addAll(roadmapKanbanItems);
            allItems.addAll(filteredItems);
            
            // Group items by status
            Map<String, List<KanbanItem>> groupedItems = allItems.stream()
                .collect(Collectors.groupingBy(KanbanItem::getStatus));
            
            // Ensure all columns exist even if empty
            if (!groupedItems.containsKey("COMMITTED")) groupedItems.put("COMMITTED", new java.util.ArrayList<>());
            if (!groupedItems.containsKey("TODO")) groupedItems.put("TODO", new java.util.ArrayList<>());
            if (!groupedItems.containsKey("IN_PROGRESS")) groupedItems.put("IN_PROGRESS", new java.util.ArrayList<>());
            if (!groupedItems.containsKey("DONE")) groupedItems.put("DONE", new java.util.ArrayList<>());
            
            System.out.println("Returning grouped items with keys: " + groupedItems.keySet());
            System.out.println("COMMITTED items count: " + groupedItems.get("COMMITTED").size());
            return ResponseEntity.ok(groupedItems);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching kanban items: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createKanbanItem(@PathVariable String productId, 
                                               @RequestBody KanbanItem kanbanItem) {
        // Kanban items are now automatically generated from published roadmap items
        // Manual creation is not allowed
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body("Kanban items are automatically populated from published roadmap items. Manual creation is not allowed.");
    }
    
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateKanbanItem(@PathVariable String productId,
                                               @PathVariable Long itemId,
                                               @RequestBody KanbanItem updatedItem) {
        try {
            // Check if this is a roadmap item (negative ID)
            if (itemId < 0) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body("Roadmap items cannot be edited from Kanban board. Please edit from the Roadmap.");
            }
            
            Optional<Product> productOpt = productRepository.findById(Long.parseLong(productId));
            if (!productOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            
            Optional<KanbanItem> itemOpt = kanbanItemRepository.findById(itemId);
            if (!itemOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kanban item not found");
            }
            
            KanbanItem item = itemOpt.get();
            
            // Update fields
            if (updatedItem.getTitle() != null) item.setTitle(updatedItem.getTitle());
            if (updatedItem.getDescription() != null) item.setDescription(updatedItem.getDescription());
            if (updatedItem.getPriority() != null) item.setPriority(updatedItem.getPriority());
            if (updatedItem.getAssignee() != null) item.setAssignee(updatedItem.getAssignee());
            if (updatedItem.getDueDate() != null) item.setDueDate(updatedItem.getDueDate());
            if (updatedItem.getLabels() != null) item.setLabels(updatedItem.getLabels());
            if (updatedItem.getEpicId() != null) item.setEpicId(updatedItem.getEpicId());
            if (updatedItem.getStoryPoints() != null) item.setStoryPoints(updatedItem.getStoryPoints());
            
            KanbanItem saved = kanbanItemRepository.save(item);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating kanban item: " + e.getMessage());
        }
    }
    
    @PutMapping("/{itemId}/move")
    public ResponseEntity<?> moveKanbanItem(@PathVariable String productId,
                                             @PathVariable Long itemId,
                                             @RequestBody Map<String, Object> moveData) {
        try {
            Optional<Product> productOpt = productRepository.findById(Long.parseLong(productId));
            if (!productOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            
            Product product = productOpt.get();
            String newStatus = (String) moveData.get("status");
            Integer newPosition = (Integer) moveData.get("position");
            
            // Check if this is a roadmap item (negative ID)
            if (itemId < 0) {
                // For roadmap items, we create/update a corresponding kanban item to track progress
                // The original roadmap item stays in COMMITTED but we track its progress
                Long roadmapItemId = Math.abs(itemId);
                
                Optional<RoadmapItem> roadmapItemOpt = roadmapItemRepository.findById(roadmapItemId);
                if (!roadmapItemOpt.isPresent()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Roadmap item not found");
                }
                
                RoadmapItem roadmapItem = roadmapItemOpt.get();
                
                // Look for existing kanban item for this roadmap item
                List<KanbanItem> existingItems = kanbanItemRepository.findByEpicIdAndProduct(roadmapItem.getEpicId(), product);
                KanbanItem trackingItem;
                
                if (existingItems.isEmpty()) {
                    // Create new tracking item
                    trackingItem = new KanbanItem();
                    trackingItem.setTitle(roadmapItem.getEpicName());
                    trackingItem.setDescription(roadmapItem.getEpicDescription());
                    trackingItem.setPriority(roadmapItem.getPriority());
                    trackingItem.setAssignee(roadmapItem.getAssignedTeam());
                    trackingItem.setDueDate(roadmapItem.getEndDate() != null ? roadmapItem.getEndDate().atStartOfDay() : null);
                    trackingItem.setEpicId(roadmapItem.getEpicId());
                    trackingItem.setLabels("roadmap-item");
                    trackingItem.setProduct(product);
                } else {
                    trackingItem = existingItems.get(0);
                }
                
                // Update status and position
                trackingItem.setStatus(newStatus);
                trackingItem.setPosition(newPosition);
                
                // Update the corresponding roadmap item status to match the Kanban status
                String roadmapStatus = mapKanbanStatusToRoadmapStatus(newStatus);
                roadmapItem.setStatus(roadmapStatus);
                roadmapItemRepository.save(roadmapItem);
                System.out.println("Updated roadmap item status to: " + roadmapStatus + " for epic: " + roadmapItem.getEpicName());
                
                KanbanItem saved = kanbanItemRepository.save(trackingItem);
                return ResponseEntity.ok(saved);
            }
            
            // Regular kanban item handling
            Optional<KanbanItem> itemOpt = kanbanItemRepository.findById(itemId);
            if (!itemOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kanban item not found");
            }
            
            KanbanItem item = itemOpt.get();
            String oldStatus = item.getStatus();
            Integer oldPosition = item.getPosition();
            
            // If moving to a different column
            if (!oldStatus.equals(newStatus)) {
                // Update positions in old column
                List<KanbanItem> oldColumnItems = kanbanItemRepository.findByProductAndStatusOrderByPositionAsc(product, oldStatus);
                for (KanbanItem oldItem : oldColumnItems) {
                    if (oldItem.getPosition() > oldPosition) {
                        oldItem.setPosition(oldItem.getPosition() - 1);
                        kanbanItemRepository.save(oldItem);
                    }
                }
                
                // Update positions in new column
                List<KanbanItem> newColumnItems = kanbanItemRepository.findByProductAndStatusOrderByPositionAsc(product, newStatus);
                for (KanbanItem newItem : newColumnItems) {
                    if (newItem.getPosition() >= newPosition) {
                        newItem.setPosition(newItem.getPosition() + 1);
                        kanbanItemRepository.save(newItem);
                    }
                }
            } else {
                // Moving within the same column
                List<KanbanItem> columnItems = kanbanItemRepository.findByProductAndStatusOrderByPositionAsc(product, newStatus);
                
                if (newPosition < oldPosition) {
                    // Moving up
                    for (KanbanItem columnItem : columnItems) {
                        if (columnItem.getPosition() >= newPosition && columnItem.getPosition() < oldPosition) {
                            columnItem.setPosition(columnItem.getPosition() + 1);
                            kanbanItemRepository.save(columnItem);
                        }
                    }
                } else if (newPosition > oldPosition) {
                    // Moving down
                    for (KanbanItem columnItem : columnItems) {
                        if (columnItem.getPosition() > oldPosition && columnItem.getPosition() <= newPosition) {
                            columnItem.setPosition(columnItem.getPosition() - 1);
                            kanbanItemRepository.save(columnItem);
                        }
                    }
                }
            }
            
            // Update the moved item
            item.setStatus(newStatus);
            item.setPosition(newPosition);
            KanbanItem saved = kanbanItemRepository.save(item);
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error moving kanban item: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteKanbanItem(@PathVariable String productId,
                                               @PathVariable Long itemId) {
        try {
            // Check if this is a roadmap item (negative ID)
            if (itemId < 0) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body("Roadmap items cannot be deleted from Kanban board. They are managed from the Roadmap.");
            }
            
            Optional<Product> productOpt = productRepository.findById(Long.parseLong(productId));
            if (!productOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            
            Optional<KanbanItem> itemOpt = kanbanItemRepository.findById(itemId);
            if (!itemOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kanban item not found");
            }
            
            Product product = productOpt.get();
            KanbanItem item = itemOpt.get();
            
            // Update positions of items after the deleted item
            List<KanbanItem> columnItems = kanbanItemRepository.findByProductAndStatusOrderByPositionAsc(product, item.getStatus());
            for (KanbanItem columnItem : columnItems) {
                if (columnItem.getPosition() > item.getPosition()) {
                    columnItem.setPosition(columnItem.getPosition() - 1);
                    kanbanItemRepository.save(columnItem);
                }
            }
            
            kanbanItemRepository.deleteById(itemId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Kanban item deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting kanban item: " + e.getMessage());
        }
    }
    
    private String mapKanbanStatusToRoadmapStatus(String kanbanStatus) {
        // Map Kanban statuses to corresponding Roadmap statuses
        switch (kanbanStatus) {
            case "COMMITTED":
                return "Committed";
            case "TODO": 
                return "To-Do";
            case "IN_PROGRESS":
                return "In-Progress";
            case "DONE":
                return "Done";
            default:
                return "Committed"; // Default fallback
        }
    }
}