package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.Car;
import com.garage.model.Customer;
import com.garage.model.Employee;
import com.garage.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {
    
    private final CarRepository carRepository;
    private final CustomerService customerService;
    private final AuditLogService auditLogService;
    
    public Car findById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
    }
    
    public Car findByLicensePlate(String licensePlate) {
        return carRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with license plate: " + licensePlate));
    }
    
    public List<Car> findAll() {
        return carRepository.findAll();
    }
    
    public Page<Car> findAll(Pageable pageable) {
        return carRepository.findAll(pageable);
    }
    
    public List<Car> findByCustomerId(Long customerId) {
        return carRepository.findByOwnerId(customerId);
    }
    
    public Page<Car> findByCustomerId(Long customerId, Pageable pageable) {
        return carRepository.findByOwnerId(customerId, pageable);
    }
    
    @Transactional
    public Car createCar(Long customerId, String licensePlate, String brand, String model, 
                         Integer year, String vin, Employee createdBy) {
        if (carRepository.existsByLicensePlate(licensePlate)) {
            throw new BadRequestException("Car with this license plate already exists");
        }
        if (vin != null && carRepository.existsByVin(vin)) {
            throw new BadRequestException("Car with this VIN already exists");
        }
        
        Customer customer = customerService.findById(customerId);
        
        Car car = new Car();
        car.setOwner(customer);
        car.setLicensePlate(licensePlate);
        car.setBrand(brand);
        car.setModel(model);
        car.setYear(year);
        car.setVin(vin);
        
        Car savedCar = carRepository.save(car);
        
        auditLogService.log("CREATE", createdBy, "Car", savedCar.getId(), 
                "Created car: " + licensePlate + " for customer: " + customer.getFirstName() + " " + customer.getLastName());
        
        return savedCar;
    }
    
    @Transactional
    public Car updateCar(Long id, String licensePlate, String brand, String model, 
                         Integer year, String vin, Employee updatedBy) {
        Car car = findById(id);
        
        if (licensePlate != null && !licensePlate.equals(car.getLicensePlate())) {
            if (carRepository.existsByLicensePlate(licensePlate)) {
                throw new BadRequestException("Car with this license plate already exists");
            }
            car.setLicensePlate(licensePlate);
        }
        
        if (vin != null && !vin.equals(car.getVin())) {
            if (carRepository.existsByVin(vin)) {
                throw new BadRequestException("Car with this VIN already exists");
            }
            car.setVin(vin);
        }
        
        if (brand != null) car.setBrand(brand);
        if (model != null) car.setModel(model);
        if (year != null) car.setYear(year);
        
        Car savedCar = carRepository.save(car);
        
        auditLogService.log("UPDATE", updatedBy, "Car", savedCar.getId(), 
                "Updated car: " + car.getLicensePlate());
        
        return savedCar;
    }
    
    @Transactional
    public void deleteCar(Long id, Employee deletedBy) {
        Car car = findById(id);
        carRepository.delete(car);
        
        auditLogService.log("DELETE", deletedBy, "Car", id, 
                "Deleted car: " + car.getLicensePlate());
    }
}