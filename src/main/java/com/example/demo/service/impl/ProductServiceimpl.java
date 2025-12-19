package com.example.demo.service.impl;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entity.DemandForecast;
import com.example.demo.entity.Product;
import com.example.demo.entity.Store;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.DemandForecastRepository;
import com.example.demo.service.DemandForecastService;
import com.example.demo.service.ProductService;
import com.example.demo.service.StoreService;

@Service
public class DemandForecastServiceimpl implements DemandForecastService {

    @Autowired
    private DemandForecastRepository demandForecastRepository;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Override
    public DemandForecast createForecast(DemandForecast forecast) {
        if (!forecast.getForecastDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Forecast date must be in the future");
        }
        if (forecast.getPredictedDemand() < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        return demandForecastRepository.save(forecast);
    }

    @Override
    public DemandForecast getForecast(long storeId, long productId) {
        Store store = storeService.getStoreById(storeId);
        Product product = productService.getProductById(productId);
        return demandForecastRepository.findByStoreAndProductAndForecastDateAfter(store, product, LocalDate.now())
                .orElseThrow(() -> new BadRequestException("No forecast found"));
    }
}