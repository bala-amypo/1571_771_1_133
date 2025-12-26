package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.demo.entity.DemandForecast;
import com.example.demo.service.DemandForecastService;

@RestController
@RequestMapping("/api/forecasts")

public class DemandForecastController {

    @Autowired
    DemandForecastService demandForecastService;

    @PostMapping
    public DemandForecast createForecast(@RequestBody DemandForecast forecast) {
        return demandForecastService.createForecast(forecast);
    }

    @GetMapping("/store/{storeId}/product/{productId}")
    public DemandForecast getForecast(@PathVariable long storeId, @PathVariable long productId) {
        return demandForecastService.getForecast(storeId, productId);
    }
}