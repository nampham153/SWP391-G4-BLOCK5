package com.example.swp.service.impl;

import com.example.swp.dto.StorageRequest;
import com.example.swp.entity.Storage;
import com.example.swp.repository.StorageRepository;
import com.example.swp.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StorageServiceImpl implements StorageService {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    @Lazy
    private List<Storage> storageList = new ArrayList<>();
    private Storage storage;

    @Override
    public List<Storage> getAll() {
        // Đơn giản hóa: trả về tất cả storage mà không filter
        return storageRepository.findAll();

        // Comment tạm thời filter để tránh lỗi khi chưa có coordinates
        /*
         * return storageRepository.findAll().stream()
         * .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
         * .collect(Collectors.toList());
         */
    }

    @Override
    public Storage createStorage(@Valid StorageRequest storageRequest) {
        Storage storage = new Storage();
        storage.setStoragename(storageRequest.getStoragename());
        storage.setAddress(storageRequest.getAddress());
        storage.setCity(storageRequest.getCity());
        storage.setState(storageRequest.getState());
        storage.setArea(storageRequest.getArea());
        storage.setPricePerDay(storageRequest.getPricePerDay());
        storage.setDescription(storageRequest.getDescription());
        storage.setStatus(storageRequest.isStatus());

        if (storageRequest.getImUrl() != null && !storageRequest.getImUrl().isEmpty()) {
            storage.setImUrl(storageRequest.getImUrl());
        }

        // Tạm thời set coordinates mặc định
        storage.setLatitude(10.762622); // Tọa độ TP.HCM
        storage.setLongitude(106.660172);

        return storageRepository.save(storage);
    }

    @Override
    public Optional<Storage> findByID(int id) {
        return storageRepository.findById(id);
    }

    @Override
    public Storage updateStorage(StorageRequest storageRequest, Storage storage) {
        storage.setStoragename(storageRequest.getStoragename());
        storage.setAddress(storageRequest.getAddress());
        storage.setCity(storageRequest.getCity());
        storage.setState(storageRequest.getState());
        storage.setStatus(storageRequest.isStatus());
        storage.setArea(storageRequest.getArea());
        storage.setPricePerDay(storageRequest.getPricePerDay());
        storage.setDescription(storageRequest.getDescription());

        if (storageRequest.getImUrl() != null && !storageRequest.getImUrl().isEmpty()) {
            storage.setImUrl(storageRequest.getImUrl());
        }

        return storageRepository.save(storage);
    }

    @Override
    public void save(Storage storage) {
        storageRepository.save(storage);
    }

    @Override
    public void deleteStorageById(int id) {
        storageRepository.deleteById(id);
    }

    @Override
    public long countAvailableStorages() {
        return storageRepository.countByStatus(true);
    }

    @Override
    public long countRentedStorages() {
        return storageRepository.countByStatus(false);
    }

    @Override
    public List<Storage> findAvailableStorages(
            LocalDate startDate, LocalDate endDate,
            Double minArea, Double minPrice,
            Double maxPrice, String nameKeyword, String city) {
        // Đơn giản hóa: trả về kết quả mà không filter coordinates
        return storageRepository.findAvailableStorages(
                startDate, endDate, minArea, minPrice, maxPrice, nameKeyword, city);

    }

    @Override
    public List<String> findAllCities() {
        // Debug: Lấy tất cả storage để xem city values
        List<Storage> allStorages = storageRepository.findAll();
        System.out.println("=== DEBUG: All storages count: " + allStorages.size() + " ===");

        Set<String> uniqueCities = new HashSet<>();
        for (Storage storage : allStorages) {
            String city = storage.getCity();
            if (city != null && !city.trim().isEmpty()) {
                uniqueCities.add(city.trim());
                System.out.println("Storage ID: " + storage.getStorageid() + ", City: '" + city + "' (length: "
                        + city.length() + ")");
            }
        }
        System.out.println("Unique cities from manual check: " + uniqueCities);

        // Gọi query gốc
        List<String> cities = storageRepository.findAllCities();
        System.out.println("StorageService.findAllCities() - Found " + cities.size() + " cities");
        System.out.println("Cities from query: " + cities);
        return cities;
    }

    @Override
    public void updateStatusBasedOnAvailability(int storageId, LocalDate startDate, LocalDate endDate) {
        Optional<Storage> storageOpt = storageRepository.findById(storageId);
        if (storageOpt.isPresent()) {
            Storage storage = storageOpt.get();

            storageRepository.save(storage);
        }
    }

    @Override
    public Page<Storage> getAllStorages(Pageable pageable) {
        return storageRepository.findAll(pageable);
    }

    @Override
    public Page<Storage> getStoragesWithFilters(String storageName, String city, String status, Pageable pageable) {
        Boolean statusBoolean = null;
        if (status != null && !status.trim().isEmpty()) {
            statusBoolean = Boolean.parseBoolean(status);
        }
        
        return storageRepository.findStoragesWithFilters(
            storageName != null && !storageName.trim().isEmpty() ? storageName.trim() : null,
            city != null && !city.trim().isEmpty() ? city.trim() : null,
            statusBoolean,
            pageable
        );
    }
}