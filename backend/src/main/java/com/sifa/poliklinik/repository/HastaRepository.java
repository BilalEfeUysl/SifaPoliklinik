package com.sifa.poliklinik.repository;

import com.sifa.poliklinik.model.Hasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HastaRepository extends JpaRepository<Hasta, Long> {
    Optional<Hasta> findByTcKimlik(String tcKimlik);
    boolean existsByTcKimlik(String tcKimlik);
    List<Hasta> findByAdContainingIgnoreCaseOrSoyadContainingIgnoreCase(String ad, String soyad);

    @Query("SELECT h FROM Hasta h WHERE LOWER(h.ad) LIKE LOWER(CONCAT('%', :arama, '%')) " +
           "OR LOWER(h.soyad) LIKE LOWER(CONCAT('%', :arama, '%')) " +
           "OR h.tcKimlik LIKE CONCAT('%', :arama, '%')")
    List<Hasta> aramaYap(@Param("arama") String arama);
}
