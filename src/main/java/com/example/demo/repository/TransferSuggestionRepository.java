package com.example.demo.repository;

import com.example.demo.entity.TransferSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferSuggestionRepository extends JpaRepository<TransferSuggestion, Long> {

    // Query method for source store
    @Query("SELECT ts FROM TransferSuggestion ts WHERE ts.sourceStore.id = :storeId")
    List<TransferSuggestion> findBySourceStoreId(@Param("storeId") Long storeId);

    List<TransferSuggestion> findByProduct_Id(Long productId);
}