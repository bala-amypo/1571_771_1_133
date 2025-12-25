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
import java.util.List;

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
        // Validate forecast date is in the future
        if (forecast.getForecastDate() == null ||
            !forecast.getForecastDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Forecast date must be in the future");
        }

        // Validate predicted demand is non-negative
        if (forecast.getPredictedDemand() < 0) {
            throw new BadRequestException("Predicted demand must be >= 0");
        }

        return demandForecastRepository.save(forecast);
    }

    @Override
    public DemandForecast getForecast(long storeId, long productId) {
        Store store = storeService.getStoreById(storeId);
        Product product = productService.getProductById(productId);

        List<DemandForecast> forecasts = demandForecastRepository.findByStoreAndProductAndForecastDateAfter(
                store, product, LocalDate.now()
        );

        if (forecasts.isEmpty()) {
            throw new BadRequestException("No forecast found");
        }

        return forecasts.get(0);
    }

    @Override
    public List<DemandForecast> getForecastsForStore(Long storeId) {
        // Validate store exists
        storeService.getStoreById(storeId);
        return demandForecastRepository.findByStore_Id(storeId);
    }
}