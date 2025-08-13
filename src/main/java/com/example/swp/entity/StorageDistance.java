package com.example.swp.entity;

public class StorageDistance {
    private Storage storage;
    private double distance;

    public StorageDistance(Storage storage, double distance) {
        this.storage = storage;
        this.distance = distance;
    }

    public Storage getStorage() { return storage; }
    public double getDistance() { return distance; }
}
