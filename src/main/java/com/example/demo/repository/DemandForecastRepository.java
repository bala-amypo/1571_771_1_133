package com.example.demo.repository;

import java.time.LocalDate;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.DemandForecast;
import com.example.demo.entity.Product;
import com.example.demo.entity.Store;

public interface DemandForecastRepository extends JpaRepository<DemandForecast, Long> {
    Optional<DemandForecast> findByStoreAndProductAndForecastDateAfter(Store store, Product product, LocalDate date);

    DemandForecast save(DemandForecast forecast);
}
