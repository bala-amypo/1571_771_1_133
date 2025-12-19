package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.InventoryLevel;
import com.example.demo.entity.TransferSuggestion;

public interface TransferSuggestionRepository extends JpaRepository<TransferSuggestion, Long> {
    List<TransferSuggestion> findBySourceStoreId(long storeId);
    List<TransferSuggestion> findByTargetStoreId(long storeId);
    Optional<InventoryLevel> findAll(long id);
    Optional<InventoryLevel> findById(long id);
}
