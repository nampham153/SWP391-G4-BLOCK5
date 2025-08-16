package com.example.swp.service;

import com.example.swp.dto.StorageRequest;
import com.example.swp.entity.Storage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public interface StorageService {
     List<Storage> getAll();
     Storage createStorage(StorageRequest storageRequest);

     Optional<Storage> findByID(int id);

     Storage updateStorage(StorageRequest storageRequest, Storage storage);

    void save(Storage storage);

    void deleteStorageById(int id);
    List<Storage> findAvailableStorages(
            LocalDate startDate,
            LocalDate endDate,
            Double minArea,
            Double minPrice,
            Double maxPrice,
            String nameKeyword,
            String city);

    List<String> findAllCities();


    long countAvailableStorages(); // còn trống (status == true)
    long countRentedStorages();    // đang thuê (status == false)
    void updateStatusBasedOnAvailability(int storageId, LocalDate startDate, LocalDate endDate);


}
