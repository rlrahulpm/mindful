package com.productapp.service;

import com.productapp.entity.Assumption;
import com.productapp.entity.Product;
import com.productapp.repository.AssumptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssumptionService {
    
    @Autowired
    private AssumptionRepository assumptionRepository;
    
    public List<Assumption> getAssumptionsByProduct(Product product) {
        return assumptionRepository.findByProduct(product);
    }
    
    public List<Assumption> getAssumptionsByProductId(Long productId) {
        return assumptionRepository.findByProductId(productId);
    }
    
    public Optional<Assumption> getAssumptionById(Long id) {
        return assumptionRepository.findById(id);
    }
    
    public Optional<Assumption> getAssumptionByIdAndProductId(Long id, Long productId) {
        return assumptionRepository.findByIdAndProductId(id, productId);
    }
    
    public Assumption saveAssumption(Assumption assumption) {
        return assumptionRepository.save(assumption);
    }
    
    public Assumption createAssumption(Product product, String assumption, String confidence, String impact) {
        Assumption newAssumption = new Assumption(product, assumption, confidence, impact);
        return assumptionRepository.save(newAssumption);
    }
    
    public Assumption updateAssumption(Long id, Long productId, String assumption, String confidence, String impact) {
        Optional<Assumption> existingAssumption = assumptionRepository.findByIdAndProductId(id, productId);
        if (existingAssumption.isEmpty()) {
            throw new IllegalArgumentException("Assumption not found");
        }
        
        Assumption assumptionEntity = existingAssumption.get();
        assumptionEntity.setAssumption(assumption);
        assumptionEntity.setConfidence(confidence);
        assumptionEntity.setImpact(impact);
        
        return assumptionRepository.save(assumptionEntity);
    }
    
    public void deleteAssumption(Long id, Long productId) {
        Optional<Assumption> assumption = assumptionRepository.findByIdAndProductId(id, productId);
        if (assumption.isPresent()) {
            assumptionRepository.delete(assumption.get());
        }
    }
    
    public void deleteAssumptionsByProductId(Long productId) {
        assumptionRepository.deleteByProductId(productId);
    }
}