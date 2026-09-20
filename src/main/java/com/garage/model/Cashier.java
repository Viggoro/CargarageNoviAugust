package com.garage.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("CASHIER")
@NoArgsConstructor
public class Cashier extends Employee {
}