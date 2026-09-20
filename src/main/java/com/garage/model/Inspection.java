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
@Table(name = "inspections")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Inspection extends CarActivity {
    
    @Column(columnDefinition = "TEXT")
    private String findings;
    
    private String inspectionResult;
    
    @ElementCollection
    @CollectionTable(name = "inspection_defects", joinColumns = @JoinColumn(name = "inspection_id"))
    @Column(name = "defect")
    private List<String> defects = new ArrayList<>();
    
    private Boolean approved;
    
    private LocalDateTime completedAt;
    
    @OneToOne(mappedBy = "inspection", cascade = CascadeType.ALL)
    private Receipt receipt;
    
    public void addDefect(String defect) {
        defects.add(defect);
    }
}