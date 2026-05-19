package com.sifa.poliklinik.repository;

import com.sifa.poliklinik.model.Klinik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KlinikRepository extends JpaRepository<Klinik, Long> {
    Optional<Klinik> findByAd(String ad);
    boolean existsByAd(String ad);
}
