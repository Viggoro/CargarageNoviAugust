package com.garage.repository;

import com.garage.model.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByLicensePlate(String licensePlate);
    Optional<Car> findByVin(String vin);
    List<Car> findByOwnerId(Long ownerId);
    Page<Car> findByOwnerId(Long ownerId, Pageable pageable);
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByVin(String vin);
}