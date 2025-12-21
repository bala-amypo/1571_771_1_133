package com.example.demo.repository;

import com.example.demo.entity.TransferSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferSuggestionRepository
        extends JpaRepository<TransferSuggestion, Long> {

    List<TransferSuggestion> findBySourceStoreId(Long storeId);

    List<TransferSuggestion> findByProduct_Id(Long productId);
}
