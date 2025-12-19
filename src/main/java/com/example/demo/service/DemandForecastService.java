package com.example.demo.service;

import com.example.demo.entity.DemandForecast;

public interface DemandForecastService {
    DemandForecast createForecast(DemandForecast forecast);
    DemandForecast getForecast(long storeId, long productId);
}