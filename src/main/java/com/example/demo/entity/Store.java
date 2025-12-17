package com.example.Multi.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "store", uniqueConstraints = {
        @UniqueConstraint(columnNames = "store_name")
})
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "store_name", nullable = false, unique = true)
    private String storeName;

    private String address;
    private String region;

    @Column(nullable = false)
    private boolean active = true;

    // Required by JPA
    public Store() {}

    public Store(long id, String storeName, String address, String region, boolean active) {
        this.id = id;
        this.storeName = storeName;
        this.address = address;
        this.region = region;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getAddress() {
        return address;
    }

    public String getRegion() {
        return region;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
