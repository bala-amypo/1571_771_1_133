package com.example.Multi.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;  
import com.example.Multi.Model.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
       Optional<Store> findByStoreName(String storeName);
}
