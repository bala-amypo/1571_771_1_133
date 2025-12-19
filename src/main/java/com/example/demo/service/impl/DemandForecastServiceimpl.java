package com.example.demo.service.impl;

import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.DemandForecastRepository;
import com.example.demo.service.DemandForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class DemandForecastServiceimpl implements DemandForecastService {

    @Autowired
    private DemandForecastRepository repository;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Override
    public DemandForecast createForecast(DemandForecast forecast) {
        if (!forecast.getForecastDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Forecast date must be in the future");
        }
        return repository.save(forecast);
    }

    @Override
    public DemandForecast getForecast(long storeId, long productId) {
        Store store = storeService.getStoreById(storeId);
        Product product = productService.getProductById(productId);
        return repository.findByStoreAndProductAndForecastDateAfter(store, product, LocalDate.now())
                .orElseThrow(() -> new BadRequestException("No forecast found"));
    }
}