package com.example.demo.service.impl;

import com.example.demo.entity.DemandForecast;
import com.example.demo.entity.Product;
import com.example.demo.entity.Store;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.DemandForecastRepository;
import com.example.demo.service.DemandForecastService;
import com.example.demo.service.ProductService;
import com.example.demo.service.StoreService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DemandForecastServiceimpl implements DemandForecastService {

    private final DemandForecastRepository demandForecastRepository;
    private final StoreService storeService;
    private final ProductService productService;

    public DemandForecastServiceimpl(DemandForecastRepository demandForecastRepository,
                                     StoreService storeService,
                                     ProductService productService) {
        this.demandForecastRepository = demandForecastRepository;
        this.storeService = storeService;
        this.productService = productService;
    }

    @Override
    public DemandForecast createForecast(DemandForecast forecast) {
        if (!forecast.getForecastDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Forecast date must be in the future");
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