package com.example.swp.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.swp.entity.Storage;
import com.example.swp.entity.Zone;
import com.example.swp.repository.StorageRepository;
import com.example.swp.repository.ZoneRepository;
import com.example.swp.service.ZoneService;

@Service
public class ZoneServiceImpl implements ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Override
    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    @Override
    public List<Zone> getZonesByStorageId(int storageId) {
        return zoneRepository.findAllByStorageId(storageId);
    }

    @Override
    public Optional<Zone> getZoneById(int id) {
        return zoneRepository.findById(id);
    }

    @Override
    public Zone createZone(Zone zone) {
        // Validate zone area before creating
        if (!validateZoneArea(zone.getStorage().getStorageid(), zone.getZoneArea())) {
            throw new IllegalArgumentException("Diện tích zone vượt quá diện tích còn lại của kho");
        }
        return zoneRepository.save(zone);
    }

    @Override
    public Zone updateZone(Zone zone) {
        // Validate zone area before updating
        if (!validateZoneArea(zone.getStorage().getStorageid(), zone.getZoneArea())) {
            throw new IllegalArgumentException("Diện tích zone vượt quá diện tích còn lại của kho");
        }
        return zoneRepository.save(zone);
    }

    @Override
    public void deleteZone(int id) {
        zoneRepository.deleteById(id);
    }

    @Override
    public double getTotalZoneAreaByStorageId(int storageId) {
        List<Zone> zones = zoneRepository.findAllByStorageId(storageId);
        return zones.stream()
                .filter(zone -> zone.getZoneArea() != null)
                .mapToDouble(Zone::getZoneArea)
                .sum();
    }

    @Override
    public boolean validateZoneArea(int storageId, double newZoneArea) {
        Optional<Storage> storageOpt = storageRepository.findById(storageId);
        if (storageOpt.isEmpty()) {
            return false;
        }

        Storage storage = storageOpt.get();
        double storageArea = storage.getArea();
        double totalExistingZoneArea = getTotalZoneAreaByStorageId(storageId);
        
        // Kiểm tra: tổng diện tích zone (bao gồm zone mới) phải <= diện tích kho
        return (totalExistingZoneArea + newZoneArea) <= storageArea;
    }

    @Override
    public int countZonesByStorageId(int storageId) {
        List<Zone> zones = zoneRepository.findAllByStorageId(storageId);
        return zones.size();
    }
}
