package com.garage.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("BACK_OFFICE")
@NoArgsConstructor
public class BackOfficeEmployee extends Employee {
}