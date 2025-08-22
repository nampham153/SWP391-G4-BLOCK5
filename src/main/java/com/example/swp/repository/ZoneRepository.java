package com.example.swp.repository;

import com.example.swp.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ZoneRepository extends JpaRepository<Zone, Integer> {
 
    List<Zone> findByStorage_Storageid(int storageId);

    @Query("SELECT z FROM Zone z WHERE z.storage.storageid = :storageId")
    List<Zone> findAllByStorageId(@Param("storageId") int storageId);
}
