package com.productapp.service;

import com.productapp.entity.Initiative;
import com.productapp.entity.Product;
import com.productapp.repository.InitiativeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InitiativeService {
    
    @Autowired
    private InitiativeRepository initiativeRepository;
    
    public List<Initiative> getInitiativesByProduct(Product product) {
        return initiativeRepository.findByProduct(product);
    }
    
    public List<Initiative> getInitiativesByProductId(Long productId) {
        return initiativeRepository.findByProductId(productId);
    }
    
    public Optional<Initiative> getInitiativeById(Long id) {
        return initiativeRepository.findById(id);
    }
    
    public Optional<Initiative> getInitiativeByIdAndProductId(Long id, Long productId) {
        return initiativeRepository.findByIdAndProductId(id, productId);
    }
    
    public Initiative saveInitiative(Initiative initiative) {
        return initiativeRepository.save(initiative);
    }
    
    public Initiative createInitiative(Product product, String title, String description, 
                                     String priority, String timeline, String owner) {
        if (initiativeRepository.existsByProductIdAndTitle(product.getId(), title)) {
            throw new IllegalArgumentException("Initiative with title '" + title + "' already exists for this product");
        }
        
        Initiative initiative = new Initiative(product, title, description, priority, timeline, owner);
        return initiativeRepository.save(initiative);
    }
    
    public Initiative updateInitiative(Long id, Long productId, String title, String description, 
                                     String priority, String timeline, String owner) {
        Optional<Initiative> existingInitiative = initiativeRepository.findByIdAndProductId(id, productId);
        if (existingInitiative.isEmpty()) {
            throw new IllegalArgumentException("Initiative not found");
        }
        
        Initiative initiative = existingInitiative.get();
        
        if (!initiative.getTitle().equals(title) && 
            initiativeRepository.existsByProductIdAndTitle(productId, title)) {
            throw new IllegalArgumentException("Initiative with title '" + title + "' already exists for this product");
        }
        
        initiative.setTitle(title);
        initiative.setDescription(description);
        initiative.setPriority(priority);
        initiative.setTimeline(timeline);
        initiative.setOwner(owner);
        
        return initiativeRepository.save(initiative);
    }
    
    public void deleteInitiative(Long id, Long productId) {
        Optional<Initiative> initiative = initiativeRepository.findByIdAndProductId(id, productId);
        if (initiative.isPresent()) {
            initiativeRepository.delete(initiative.get());
        }
    }
    
    public void deleteInitiativesByProductId(Long productId) {
        initiativeRepository.deleteByProductId(productId);
    }
}