package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.DemandForecast;
import com.example.demo.entity.Product;
import com.example.demo.entity.Store;

public interface DemandForecastRepository extends JpaRepository<DemandForecast, Long> {

    List<DemandForecast> findByStoreAndProductAndForecastDateAfter(
            Store store,
            Product product,
            LocalDate date
    );

    List<DemandForecast> findByStore_Id(Long storeId); // Added missing method
}