package com.example.swp.service;

import java.util.List;
import java.util.Optional;

import com.example.swp.entity.Zone;

public interface ZoneService {
    List<Zone> getAllZones();
    List<Zone> getZonesByStorageId(int storageId);
    Optional<Zone> getZoneById(int id);
    Zone createZone(Zone zone);
    Zone updateZone(Zone zone);
    void deleteZone(int id);
    double getTotalZoneAreaByStorageId(int storageId);
    boolean validateZoneArea(int storageId, double newZoneArea);
}
