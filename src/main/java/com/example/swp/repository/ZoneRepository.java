package com.example.swp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.swp.entity.Zone;

public interface ZoneRepository extends JpaRepository<Zone, Integer> {
    List<Zone> findByStorage_Storageid(Integer storageId);

    @Query("SELECT z FROM Zone z WHERE z.storage.storageid = :storageId")
    List<Zone> findAllByStorageId(@Param("storageId") int storageId);
}
