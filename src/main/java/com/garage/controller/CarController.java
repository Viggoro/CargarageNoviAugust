package com.garage.controller;

import com.garage.model.Car;
import com.garage.model.Employee;
import com.garage.service.CarService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {
    
    private final CarService carService;
    
    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        return ResponseEntity.ok(carService.findAll());
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<Car>> getAllCarsPaged(Pageable pageable) {
        return ResponseEntity.ok(carService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.findById(id));
    }
    
    @GetMapping("/license-plate/{licensePlate}")
    public ResponseEntity<Car> getCarByLicensePlate(@PathVariable String licensePlate) {
        return ResponseEntity.ok(carService.findByLicensePlate(licensePlate));
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Car>> getCarsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(carService.findByCustomerId(customerId));
    }
    
    @PostMapping
    public ResponseEntity<Car> createCar(
            @Valid @RequestBody CreateCarRequest request,
            @AuthenticationPrincipal Employee employee) {
        Car car = carService.createCar(
                request.getCustomerId(),
                request.getLicensePlate(),
                request.getBrand(),
                request.getModel(),
                request.getYear(),
                request.getVin(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarRequest request,
            @AuthenticationPrincipal Employee employee) {
        Car car = carService.updateCar(
                id,
                request.getLicensePlate(),
                request.getBrand(),
                request.getModel(),
                request.getYear(),
                request.getVin(),
                employee
        );
        return ResponseEntity.ok(car);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        carService.deleteCar(id, employee);
        return ResponseEntity.noContent().build();
    }
    
    @Data
    public static class CreateCarRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;
        
        @NotBlank(message = "License plate is required")
        private String licensePlate;
        
        @NotBlank(message = "Brand is required")
        private String brand;
        
        @NotBlank(message = "Model is required")
        private String model;
        
        private Integer year;
        private String vin;
    }
    
    @Data
    public static class UpdateCarRequest {
        private String licensePlate;
        private String brand;
        private String model;
        private Integer year;
        private String vin;
    }
}