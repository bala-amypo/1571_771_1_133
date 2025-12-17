package com.example.demo.entity;

public class Store {
    private long id;
     private String storeName;
     private  String address;
     private String region;
     private boolean active;
    public Store(long id,String storeName, String address,  String region,boolean active) {
        this.id=id;
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
    public void setRegioString(String region) {
        this.region = region;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
  
}
