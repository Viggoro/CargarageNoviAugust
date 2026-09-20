package com.garage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repairs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Repair extends CarActivity {
    
    @ManyToMany
    @JoinTable(
        name = "repair_repair_actions",
        joinColumns = @JoinColumn(name = "repair_id"),
        inverseJoinColumns = @JoinColumn(name = "repair_action_id")
    )
    private List<RepairAction> actions = new ArrayList<>();
    
    @OneToMany(mappedBy = "repair", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepairPart> parts = new ArrayList<>();
    
    @OneToMany(mappedBy = "repair", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomRepairAction> customActions = new ArrayList<>();
    
    private Double totalCost = 0.0;
    
    private LocalDateTime completedAt;
    
    @OneToOne(mappedBy = "repair", cascade = CascadeType.ALL)
    private Receipt receipt;
    
    public void addAction(RepairAction action) {
        actions.add(action);
    }
    
    public void addCustomAction(CustomRepairAction customAction) {
        customActions.add(customAction);
        customAction.setRepair(this);
    }
    
    public void addPart(Part part, int quantity) {
        RepairPart repairPart = new RepairPart();
        repairPart.setRepair(this);
        repairPart.setPart(part);
        repairPart.setQuantity(quantity);
        parts.add(repairPart);
    }
    
    public double calculateTotalCost() {
        double actionsCost = actions.stream()
            .mapToDouble(RepairAction::getPrice)
            .sum();
        
        double customActionsCost = customActions.stream()
            .mapToDouble(CustomRepairAction::getPrice)
            .sum();
        
        double partsCost = parts.stream()
            .mapToDouble(RepairPart::getSubtotal)
            .sum();
        
        this.totalCost = actionsCost + customActionsCost + partsCost;
        return this.totalCost;
    }
}