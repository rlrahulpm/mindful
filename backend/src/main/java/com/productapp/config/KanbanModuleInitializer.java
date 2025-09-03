package com.productapp.config;

import com.productapp.entity.Module;
import com.productapp.entity.Product;
import com.productapp.entity.ProductModule;
import com.productapp.repository.ModuleRepository;
import com.productapp.repository.ProductModuleRepository;
import com.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Order(8)
public class KanbanModuleInitializer implements CommandLineRunner {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductModuleRepository productModuleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if Kanban Board module exists
        Optional<Module> existingModule = moduleRepository.findByName("Kanban Board");
        
        Module kanbanModule;
        if (!existingModule.isPresent()) {
            // Create Kanban Board module
            kanbanModule = new Module();
            kanbanModule.setName("Kanban Board");
            kanbanModule.setDescription("Track and manage work items from Committed to Done");
            kanbanModule.setIcon("view_kanban");
            kanbanModule.setIsActive(true);
            kanbanModule.setDisplayOrder(8);
            kanbanModule = moduleRepository.save(kanbanModule);
            System.out.println("Created Kanban Board module");
        } else {
            kanbanModule = existingModule.get();
            System.out.println("Kanban Board module already exists");
        }

        // Add Kanban Board module to all products that don't have it
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            Optional<ProductModule> existingPM = productModuleRepository.findByProductAndModule(product, kanbanModule);
            if (!existingPM.isPresent()) {
                ProductModule pm = new ProductModule();
                pm.setProduct(product);
                pm.setModule(kanbanModule);
                pm.setIsEnabled(true);
                productModuleRepository.save(pm);
                System.out.println("Added Kanban Board module to product: " + product.getProductName());
            }
        }
    }
}