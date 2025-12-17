package com.example.Multi.Service;

import java.util.List;
import com.example.Multi.Model.Store;

public interface StoreService {

    Store createStore(Store store);

    List<Store> getStore();

    Store getById(long id);
}
