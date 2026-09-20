package com.garage.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("MECHANIC")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Mechanic extends Employee {
    
    @OneToMany(mappedBy = "performedBy")
    private List<Inspection> inspections = new ArrayList<>();
    
    @OneToMany(mappedBy = "performedBy")
    private List<Repair> repairs = new ArrayList<>();
}