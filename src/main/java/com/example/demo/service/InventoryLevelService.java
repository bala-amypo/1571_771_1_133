
package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.InventoryLevel;

public interface InventoryLevelService {
    InventoryLevel updateInventory(long storeId, long productId, int quantity);
    InventoryLevel getInventory(long storeId, long productId);
    List<InventoryLevel> getInventoryByStore(long storeId);
}
